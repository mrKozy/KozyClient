// ☭ Red Star Client — TextureLoader
package com.redstarclient.render;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.opengl.GL30;

import java.io.InputStream;
import java.nio.ByteBuffer;

public final class TextureLoader {

    private static int logoTextureId = -1;
    private static int logoWidth = 0;
    private static int logoHeight = 0;

    private TextureLoader() {}

    public static int getLogoTextureId() {
        if (logoTextureId == -1) {
            loadLogoTexture();
        }
        return logoTextureId;
    }

    public static int getLogoWidth() { return logoWidth; }
    public static int getLogoHeight() { return logoHeight; }

    private static synchronized void loadLogoTexture() {
        if (logoTextureId != -1) return;

        try (InputStream in = TextureLoader.class.getResourceAsStream("/assets/redstarclient/logo.png")) {
            if (in == null) {
                System.err.println("[Red Star Client] Could not find /assets/redstarclient/logo.png");
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
            System.out.println("[Red Star Client] Uploaded Logo Texture: ID=" + texId + " (" + logoWidth + "x" + logoHeight + ")");
            image.close();
        } catch (Throwable t) {
            System.err.println("[Red Star Client] Failed to load logo texture: " + t.getMessage());
        }
    }
}
