// KozyClient — TextureLoader
package com.redstarclient.render;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.opengl.GL30;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;

public final class TextureLoader {

    private static int logoTextureId = -1;
    private static int logoWidth = 0;
    private static int logoHeight = 0;

    // URL -> GL texture ID cache for album art etc.
    private static final ConcurrentHashMap<String, Integer> urlTextureCache = new ConcurrentHashMap<>();
    // Textures pending upload from background thread (url -> raw PNG bytes)
    private static final ConcurrentHashMap<String, byte[]> pendingUploads   = new ConcurrentHashMap<>();

    private TextureLoader() {}

    public static int getLogoTextureId() {
        if (logoTextureId == -1) {
            loadLogoTexture();
        }
        return logoTextureId;
    }

    public static int getLogoWidth() { return logoWidth; }
    public static int getLogoHeight() { return logoHeight; }

    /**
     * Returns the GL texture ID for an image URL, or 0 if not yet loaded.
     * Downloads in background on first call; uploads to GL on subsequent render calls.
     * Call every frame until it returns a non-zero ID.
     */
    public static int getUrlTextureId(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) return 0;

        // Check if already uploaded
        Integer existing = urlTextureCache.get(imageUrl);
        if (existing != null) return existing;

        // Check if bytes are ready for upload (must happen on render thread)
        byte[] pendingBytes = pendingUploads.get(imageUrl);
        if (pendingBytes != null) {
            pendingUploads.remove(imageUrl);
            int texId = uploadPngBytes(pendingBytes);
            if (texId > 0) {
                urlTextureCache.put(imageUrl, texId);
            }
            return texId;
        }

        // Start background download
        urlTextureCache.put(imageUrl, 0); // mark as "downloading"
        Thread dl = new Thread(() -> {
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(imageUrl).openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                if (conn.getResponseCode() == 200) {
                    byte[] bytes = conn.getInputStream().readAllBytes();
                    pendingUploads.put(imageUrl, bytes);
                }
                conn.disconnect();
            } catch (Throwable ignored) {}
        }, "KozyClient-TextureDL");
        dl.setDaemon(true);
        dl.start();

        return 0;
    }

    /** Evict a cached URL texture (call when track changes). */
    public static void evictUrlTexture(String imageUrl) {
        if (imageUrl == null) return;
        Integer texId = urlTextureCache.remove(imageUrl);
        pendingUploads.remove(imageUrl);
        if (texId != null && texId > 0) {
            GL30.glDeleteTextures(texId);
        }
    }

    private static int uploadPngBytes(byte[] bytes) {
        try {
            NativeImage image = NativeImage.read(NativeImage.Format.RGBA, java.nio.ByteBuffer.wrap(bytes));
            int w = image.getWidth();
            int h = image.getHeight();
            ByteBuffer buf = image.getPixelBytes();

            int texId = GL30.glGenTextures();
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, texId);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, w, h, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, buf);
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

            image.close();
            return texId;
        } catch (Throwable t) {
            return 0;
        }
    }

    private static synchronized void loadLogoTexture() {
        if (logoTextureId != -1) return;

        try (InputStream in = TextureLoader.class.getResourceAsStream("/assets/redstarclient/logo.png")) {
            if (in == null) {
                System.err.println("[KozyClient] Could not find /assets/redstarclient/logo.png");
                return;
            }
            NativeImage image = NativeImage.read(in);
            logoWidth = image.getWidth();
            logoHeight = image.getHeight();
            ByteBuffer bytes = image.getPixelBytes();

            int texId = GL30.glGenTextures();
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, texId);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MIN_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_MAG_FILTER, GL30.GL_LINEAR);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_S, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexParameteri(GL30.GL_TEXTURE_2D, GL30.GL_TEXTURE_WRAP_T, GL30.GL_CLAMP_TO_EDGE);
            GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, logoWidth, logoHeight, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, bytes);
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, 0);

            logoTextureId = texId;
            System.out.println("[KozyClient] Uploaded Logo Texture: ID=" + texId + " (" + logoWidth + "x" + logoHeight + ")");
            image.close();
        } catch (Throwable t) {
            System.err.println("[KozyClient] Failed to load logo texture: " + t.getMessage());
        }
    }
}
