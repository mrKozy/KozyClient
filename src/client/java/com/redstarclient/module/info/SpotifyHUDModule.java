// KozyClient — SpotifyHUDModule
package com.redstarclient.module.info;

import com.mojang.blaze3d.platform.NativeImage;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.redstarclient.util.SpotifyUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

/**
 * Displays the currently playing Spotify song on the HUD with high-resolution
 * album cover art, play/pause status, and animated audio equalizer wave bars.
 * Beautifully themed in Red Star / KozyClient signature Red & Gold.
 * 100% integrated with interactive HudEditorScreen drag-and-drop.
 */
public class SpotifyHUDModule extends Module implements DraggableHudElement {

    private static final Identifier COVER_ID = Identifier.fromNamespaceAndPath("redstarclient", "spotify_cover");

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(40);
    private final ImBoolean demoMode = new ImBoolean(false);
    private final ImBoolean showWaveform = new ImBoolean(true);
    private final ImString customTrack = new ImString("The Sacred War", 128);
    private final ImString customArtist = new ImString("The Red Army Choir", 128);

    private int lastWidth = 195;
    private int lastHeight = 44;
    private boolean coverLoaded = false;

    public SpotifyHUDModule() {
        super("SpotifyHUD", "Shows currently playing Spotify track with stylish Album Cover Art.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onEnable() {
        SpotifyUtils.init();
        SpotifyUtils.setMockMode(demoMode.get());
    }

    @Override
    public void onDisable() {
        SpotifyUtils.stop();
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        SpotifyUtils.setMockMode(demoMode.get());
        if (!SpotifyUtils.hasTrack()) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        String artist = SpotifyUtils.getArtist();
        String track  = SpotifyUtils.getTrackName();
        boolean playing = SpotifyUtils.isPlaying();

        // 1. Upload new album cover image to Minecraft TextureManager if ready
        if (SpotifyUtils.hasNewImage()) {
            NativeImage img = SpotifyUtils.consumeNewImage();
            if (img != null) {
                try {
                    DynamicTexture dt = new DynamicTexture(() -> "spotify_cover", img);
                    mc.getTextureManager().register(COVER_ID, dt);
                    coverLoaded = true;
                } catch (Throwable ignored) {}
            }
        }

        // 2. Measure layout dimensions
        String statusIcon = playing ? ">" : "||";
        String titlePrefix = "SPOTIFY " + statusIcon + " ";
        String displayTrack = (track != null && !track.isEmpty()) ? track : "Unknown Track";
        if (displayTrack.length() > 26) {
            displayTrack = displayTrack.substring(0, 24) + "...";
        }
        String displayArtist = (artist != null && !artist.isEmpty()) ? artist : "Unknown Artist";
        if (displayArtist.length() > 28) {
            displayArtist = displayArtist.substring(0, 26) + "...";
        }

        int trackWidth = font.width(displayTrack);
        int artistWidth = font.width(displayArtist);
        int maxTextW = Math.max(font.width(titlePrefix), Math.max(trackWidth, artistWidth));

        int coverSize = 34;
        int cardPadding = 5;
        int textOffsetX = cardPadding + coverSize + 6;

        int cardWidth = Math.max(textOffsetX + maxTextW + 8, 190);
        int cardHeight = 44;

        this.lastWidth = cardWidth;
        this.lastHeight = cardHeight;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        int x = Math.max(0, Math.min(posX.get(), screenWidth - cardWidth));
        int y = Math.max(0, Math.min(posY.get(), screenHeight - cardHeight));

        // 3. Draw Background Card (Red/Charcoal theme matching the rest of the HUD)
        graphics.fill(x, y, x + cardWidth, y + cardHeight, RenderUtils.toARGB(0.06f, 0.02f, 0.02f, 0.92f));
        // Left Crimson Accent Bar
        graphics.fill(x, y, x + 3, y + cardHeight, RenderUtils.SOVIET_RED);
        // Outer thin gold & red glassmorphic borders
        graphics.fill(x + 3, y, x + cardWidth, y + 1, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.55f));
        graphics.fill(x + 3, y + cardHeight - 1, x + cardWidth, y + cardHeight, RenderUtils.toARGB(0.72f, 0.05f, 0.05f, 0.45f));
        graphics.fill(x + cardWidth - 1, y, x + cardWidth, y + cardHeight, RenderUtils.toARGB(0.72f, 0.05f, 0.05f, 0.45f));

        // 4. Draw Album Cover Art (34x34 square)
        int cx = x + cardPadding;
        int cy = y + cardPadding;

        if (coverLoaded) {
            try {
                graphics.blit(COVER_ID, cx, cy, cx + coverSize, cy + coverSize, 0.0f, 1.0f, 0.0f, 1.0f);
            } catch (Throwable t) {
                drawCoverPlaceholder(graphics, cx, cy, coverSize);
            }
        } else {
            drawCoverPlaceholder(graphics, cx, cy, coverSize);
        }
        // Border around cover art
        graphics.fill(cx - 1, cy - 1, cx + coverSize + 1, cy, RenderUtils.SOVIET_GOLD);
        graphics.fill(cx - 1, cy + coverSize, cx + coverSize + 1, cy + coverSize + 1, RenderUtils.SOVIET_GOLD);
        graphics.fill(cx - 1, cy, cx, cy + coverSize, RenderUtils.SOVIET_GOLD);
        graphics.fill(cx + coverSize, cy, cx + coverSize + 1, cy + coverSize, RenderUtils.SOVIET_GOLD);

        // 5. Text Info
        int textX = x + textOffsetX;
        int textY = y + 5;

        // Header: SPOTIFY > in Gold
        graphics.text(font, titlePrefix, textX, textY, RenderUtils.SOVIET_GOLD, false);

        // Track Name in crisp White
        graphics.text(font, displayTrack, textX, textY + 11, RenderUtils.WHITE, false);

        // Artist in soft gray/gold
        graphics.text(font, displayArtist, textX, textY + 22, RenderUtils.toARGB(0.75f, 0.75f, 0.78f, 0.90f), false);

        // 6. Mini Animated Equalizer Waveform (Red and Gold alternating bars)
        if (showWaveform.get()) {
            int waveX = textX;
            int waveY = y + 34;
            long time = System.currentTimeMillis();

            for (int i = 0; i < 16; i++) {
                float barHeight = 2.0f;
                if (playing) {
                    double phase = (time / 160.0) + (i * 0.48);
                    barHeight = (float) (2.0 + Math.abs(Math.sin(phase)) * 5.0);
                }
                int barColor = (i % 2 == 0) ? RenderUtils.SOVIET_RED : RenderUtils.SOVIET_GOLD;
                graphics.fill(waveX + (i * 4), (int) (waveY + 6 - barHeight), waveX + (i * 4) + 2, waveY + 6, barColor);
            }
        }
    }

    private void drawCoverPlaceholder(GuiGraphicsExtractor graphics, int cx, int cy, int size) {
        // Dark placeholder square
        graphics.fill(cx, cy, cx + size, cy + size, RenderUtils.toARGB(0.12f, 0.04f, 0.04f, 0.95f));
        // Vinyl disc circle representation
        int midX = cx + size / 2;
        int midY = cy + size / 2;
        graphics.fill(midX - 10, midY - 10, midX + 10, midY + 10, RenderUtils.toARGB(0.20f, 0.06f, 0.06f, 0.95f));
        graphics.fill(midX - 3, midY - 3, midX + 3, midY + 3, RenderUtils.SOVIET_GOLD);
    }

    public boolean isShowWaveform() {
        return showWaveform.get();
    }

    @Override
    public String getHudName() { return "Spotify HUD"; }

    @Override
    public int getHudX() { return posX.get(); }

    @Override
    public int getHudY() { return posY.get(); }

    @Override
    public int getHudWidth() { return lastWidth; }

    @Override
    public int getHudHeight() { return lastHeight; }

    @Override
    public void setHudPos(int x, int y) {
        posX.set(x);
        posY.set(y);
    }

    @Override
    public boolean isHudEnabled() { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        if (SpotifyUtils.isRealDetected()) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "● Live Spotify Connected!");
            ImGui.textDisabled("Now playing: " + SpotifyUtils.getCurrentTrack());
        } else {
            ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "○ Spotify Offline / Not Detected");
            ImGui.textDisabled("Turn on Spotify & play any song to auto-detect");
        }

        ImGui.separator();
        ImGui.spacing();

        if (ImGui.checkbox("Demo Mode (Always visible)##sp", demoMode)) {
            SpotifyUtils.setMockMode(demoMode.get());
            SpotifyUtils.triggerCoverReload();
        }
        ImGui.checkbox("Animated Waveform##sp", showWaveform);

        ImGui.sliderInt("Position X##sp", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##sp", posY.getData(), 0, 800);

        if (demoMode.get()) {
            ImGui.spacing();
            ImGui.textDisabled("Custom Demo Track & Cover:");
            if (ImGui.inputText("Artist##sp", customArtist)) {
                SpotifyUtils.setArtist(customArtist.get());
                SpotifyUtils.triggerCoverReload();
            }
            if (ImGui.inputText("Track##sp", customTrack)) {
                SpotifyUtils.setTrackName(customTrack.get());
                SpotifyUtils.triggerCoverReload();
            }
            if (ImGui.button(SpotifyUtils.isPlaying() ? "Pause##demo" : "Play##demo")) {
                SpotifyUtils.setPlaying(!SpotifyUtils.isPlaying());
            }
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("demoMode", demoMode.get());
        cfg.putSetting("showWaveform", showWaveform.get());
        cfg.putSetting("customTrack", customTrack.get());
        cfg.putSetting("customArtist", customArtist.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        demoMode.set(cfg.getSetting("demoMode", demoMode.get()));
        showWaveform.set(cfg.getSetting("showWaveform", showWaveform.get()));
        customTrack.set(cfg.getSetting("customTrack", customTrack.get()));
        customArtist.set(cfg.getSetting("customArtist", customArtist.get()));
    }
}
