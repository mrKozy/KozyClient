// KozyClient — SpotifyUtils
package com.redstarclient.util;

import com.mojang.blaze3d.platform.NativeImage;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 100% pure-Java Spotify media detector and multi-tier album cover art loader.
 * Queries running Spotify process window titles without native crashes.
 * Automatically resolves and downloads album cover art via iTunes & Deezer APIs.
 */
public final class SpotifyUtils {

    private static volatile String currentArtist   = "The Red Army Choir";
    private static volatile String currentTrack    = "The Sacred War";
    private static volatile boolean isPlaying      = true;
    private static volatile boolean detectedReal   = false;
    private static volatile boolean mockMode       = true;

    private static volatile String coverArtUrl     = null;
    private static volatile String lastCoverQuery  = "";

    // Loaded native cover art image awaiting upload to Minecraft texture manager
    private static volatile NativeImage pendingNativeImage = null;
    private static volatile boolean hasNewImage = false;

    private static Thread scannerThread = null;
    private static volatile boolean scannerRunning = false;

    private SpotifyUtils() {}

    /**
     * Starts the lightweight background Spotify scanner thread and cover art fetcher.
     */
    public static synchronized void init() {
        if (scannerRunning) return;
        scannerRunning = true;

        scannerThread = new Thread(() -> {
            while (scannerRunning) {
                try {
                    scanSpotify();

                    String query = currentArtist + " - " + currentTrack;
                    if (!query.equals(lastCoverQuery) && !currentTrack.isEmpty()) {
                        lastCoverQuery = query;
                        fetchAndLoadCover(currentArtist, currentTrack);
                    }
                    Thread.sleep(1200L);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable ignored) {}
            }
        }, "KozyClient-SpotifyScanner");
        scannerThread.setDaemon(true);
        scannerThread.start();
    }

    /**
     * Multi-tier album art resolver:
     * 1. iTunes: Full Artist + Track
     * 2. iTunes: Clean Track (without feats or sub-dashes)
     * 3. Deezer: Full query
     * 4. Deezer: Clean track
     * 5. iTunes: Artist
     */
    private static void fetchAndLoadCover(String artist, String track) {
        List<String> queries = new ArrayList<>();
        if (artist != null && !artist.isEmpty() && !track.isEmpty()) {
            queries.add(artist + " " + track);
        }

        // Clean track name (remove " - ...", " (feat...", " [prod...")
        String cleanTrack = track;
        if (cleanTrack.contains(" - ")) {
            cleanTrack = cleanTrack.substring(0, cleanTrack.indexOf(" - ")).trim();
        }
        if (cleanTrack.contains(" (")) {
            cleanTrack = cleanTrack.substring(0, cleanTrack.indexOf(" (")).trim();
        }
        if (cleanTrack.contains(" [")) {
            cleanTrack = cleanTrack.substring(0, cleanTrack.indexOf(" [")).trim();
        }

        if (!cleanTrack.isEmpty() && !cleanTrack.equals(track)) {
            if (artist != null && !artist.isEmpty()) {
                queries.add(artist + " " + cleanTrack);
            }
            queries.add(cleanTrack);
        } else {
            queries.add(track);
        }

        if (artist != null && !artist.isEmpty()) {
            queries.add(artist);
        }

        String resolvedUrl = null;

        for (String q : queries) {
            resolvedUrl = queryItunes(q);
            if (resolvedUrl != null) break;

            resolvedUrl = queryDeezer(q);
            if (resolvedUrl != null) break;
        }

        if (resolvedUrl != null) {
            coverArtUrl = resolvedUrl;
            downloadNativeImage(resolvedUrl);
        }
    }

    private static String queryItunes(String q) {
        try {
            String encoded = URLEncoder.encode(q, "UTF-8");
            URL url = new URL("https://itunes.apple.com/search?term=" + encoded + "&media=music&limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2500);
            conn.setReadTimeout(2500);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                String json = sb.toString();
                int idx = json.indexOf("\"artworkUrl100\":");
                if (idx >= 0) {
                    int start = json.indexOf('"', idx + 16) + 1;
                    int end   = json.indexOf('"', start);
                    if (start > 0 && end > start) {
                        return json.substring(start, end).replace("100x100bb", "300x300bb");
                    }
                }
            }
            conn.disconnect();
        } catch (Throwable ignored) {}
        return null;
    }

    private static String queryDeezer(String q) {
        try {
            String encoded = URLEncoder.encode(q, "UTF-8");
            URL url = new URL("https://api.deezer.com/search?q=" + encoded + "&limit=1");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(2500);
            conn.setReadTimeout(2500);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                String json = sb.toString();
                int idx = json.indexOf("\"cover_medium\":");
                if (idx >= 0) {
                    int start = json.indexOf('"', idx + 15) + 1;
                    int end   = json.indexOf('"', start);
                    if (start > 0 && end > start) {
                        return json.substring(start, end).replace("\\/", "/");
                    }
                }
            }
            conn.disconnect();
        } catch (Throwable ignored) {}
        return null;
    }

    private static void downloadNativeImage(String imageUrl) {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                try (InputStream in = conn.getInputStream()) {
                    NativeImage img = NativeImage.read(in);
                    if (img != null) {
                        pendingNativeImage = img;
                        hasNewImage = true;
                    }
                }
            }
            conn.disconnect();
        } catch (Throwable ignored) {}
    }

    public static boolean hasNewImage() {
        return hasNewImage;
    }

    public static synchronized NativeImage consumeNewImage() {
        if (!hasNewImage) return null;
        hasNewImage = false;
        NativeImage img = pendingNativeImage;
        pendingNativeImage = null;
        return img;
    }

    public static synchronized void stop() {
        scannerRunning = false;
        if (scannerThread != null) {
            scannerThread.interrupt();
            scannerThread = null;
        }
    }

    /**
     * Executes 'tasklist.exe /v /fo csv /fi "IMAGENAME eq Spotify.exe"'
     * and extracts the 9th column (Window Title) safely without any native code.
     */
    private static void scanSpotify() {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    "tasklist.exe", "/v", "/fo", "csv", "/fi", "IMAGENAME eq Spotify.exe"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean foundPlaying = false;
            boolean foundSpotify = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("\"Spotify.exe\"")) continue;

                    foundSpotify = true;

                    int lastQuote = line.lastIndexOf('"');
                    if (lastQuote > 0) {
                        int prevQuote = line.lastIndexOf('"', lastQuote - 1);
                        if (prevQuote >= 0) {
                            String title = line.substring(prevQuote + 1, lastQuote).trim();

                            if (!title.isEmpty() && !title.equals("N/A") && !title.equals("OleMainThreadWndName")) {
                                if (title.contains(" - ") && !title.startsWith("Spotify")) {
                                    int sep = title.indexOf(" - ");
                                    currentArtist = title.substring(0, sep).trim();
                                    currentTrack  = title.substring(sep + 3).trim();
                                    isPlaying     = true;
                                    detectedReal  = true;
                                    foundPlaying  = true;
                                    break;
                                } else if (title.equalsIgnoreCase("Spotify") ||
                                           title.equalsIgnoreCase("Spotify Free") ||
                                           title.equalsIgnoreCase("Spotify Premium")) {
                                    isPlaying     = false;
                                    detectedReal  = true;
                                }
                            }
                        }
                    }
                }
            }

            process.waitFor();

            if (!foundPlaying && !foundSpotify) {
                detectedReal = false;
            }
        } catch (Throwable ignored) {
            detectedReal = false;
        }
    }

    public static boolean isAvailable() {
        return detectedReal || mockMode;
    }

    public static boolean hasTrack() {
        return (detectedReal || mockMode) && currentTrack != null && !currentTrack.isEmpty();
    }

    public static boolean isPlaying() {
        return isPlaying;
    }

    public static void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public static String getArtist() {
        return currentArtist;
    }

    public static void setArtist(String artist) {
        currentArtist = artist;
    }

    public static String getTrackName() {
        return currentTrack;
    }

    public static void setTrackName(String track) {
        currentTrack = track;
    }

    public static boolean isMockMode() {
        return mockMode;
    }

    public static void setMockMode(boolean mock) {
        mockMode = mock;
    }

    public static boolean isRealDetected() {
        return detectedReal;
    }

    public static String getCurrentTrack() {
        if (!hasTrack()) return null;
        if (currentArtist != null && !currentArtist.isEmpty()) {
            return currentArtist + " - " + currentTrack;
        }
        return currentTrack;
    }

    public static String getCoverArtUrl() {
        return coverArtUrl;
    }

    public static void triggerCoverReload() {
        lastCoverQuery = "";
    }
}
