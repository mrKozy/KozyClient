// ☭ Red Star Client — SpotifyUtils
package com.redstarclient.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 100% pure-Java, crash-free Spotify media detector for Windows.
 * Queries running Spotify process window titles using standard process execution
 * with zero native JNA / JNI memory corruption risk.
 */
public final class SpotifyUtils {

    private static volatile String currentArtist = "The Red Army Choir";
    private static volatile String currentTrack  = "The Sacred War";
    private static volatile boolean isPlaying    = true;
    private static volatile boolean detectedReal = false;
    private static volatile boolean mockMode     = true;

    private static Thread scannerThread = null;
    private static volatile boolean scannerRunning = false;

    private SpotifyUtils() {}

    /**
     * Starts the lightweight background Spotify scanner thread.
     */
    public static synchronized void init() {
        if (scannerRunning) return;
        scannerRunning = true;

        scannerThread = new Thread(() -> {
            while (scannerRunning) {
                try {
                    scanSpotify();
                    Thread.sleep(1500L);
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable ignored) {}
            }
        }, "RedStar-SpotifyScanner");
        scannerThread.setDaemon(true);
        scannerThread.start();
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

                    // Extract the last CSV column (Window Title)
                    // Format: "Spotify.exe","15852","Console","1","...","Running","...","0:00:22","Isak Roen - vampire heart"
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
}
