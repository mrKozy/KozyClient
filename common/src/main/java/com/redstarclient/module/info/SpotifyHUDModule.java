// ☭ Red Star Client — SpotifyHUDModule
package com.redstarclient.module.info;

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
import net.minecraft.client.gui.GuiGraphics;

/**
 * Displays the currently playing Spotify song on the HUD with album/track info,
 * play/pause status, and animated audio wave bars.
 * Supports mouse drag and drop in the HUD Editor.
 */
public class SpotifyHUDModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(200);
    private final ImBoolean demoMode = new ImBoolean(true);
    private final ImBoolean showWaveform = new ImBoolean(true);
    private final ImString customTrack = new ImString("The Sacred War", 128);
    private final ImString customArtist = new ImString("The Red Army Choir", 128);

    private int lastWidth = 190;
    private int lastHeight = 28;

    public SpotifyHUDModule() {
        super("SpotifyHUD", "Shows currently playing Spotify track with stylish HUD player.", ModuleCategory.INFO, -1);
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
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        SpotifyUtils.setMockMode(demoMode.get());
        if (!SpotifyUtils.hasTrack()) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        String artist = SpotifyUtils.getArtist();
        String track  = SpotifyUtils.getTrackName();
        boolean playing = SpotifyUtils.isPlaying();

        String statusIcon = playing ? "▶" : "⏸";
        String mainText = statusIcon + "  " + (artist != null ? artist + " - " : "") + track;
        String titlePrefix = "☭ SPOTIFY ";

        int textWidth = font.width(titlePrefix + mainText);
        int cardWidth = Math.max(textWidth + 24, 180);
        int cardHeight = 26;

        this.lastWidth = cardWidth;
        this.lastHeight = cardHeight;

        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();

        int x = Math.max(0, Math.min(posX.get(), screenWidth - cardWidth));
        int y = Math.max(0, Math.min(posY.get(), screenHeight - cardHeight));

        // 1. Draw Card Background & Glassmorphic Borders
        graphics.fill(x, y, x + cardWidth, y + cardHeight, RenderUtils.toARGB(0.08f, 0.02f, 0.02f, 0.88f));
        graphics.fill(x, y, x + 3, y + cardHeight, RenderUtils.SOVIET_GOLD);
        graphics.fill(x + 3, y, x + cardWidth, y + 1, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.40f));
        graphics.fill(x + 3, y + cardHeight - 1, x + cardWidth, y + cardHeight, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.30f));
        graphics.fill(x + cardWidth - 1, y, x + cardWidth, y + cardHeight, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.30f));

        // 2. Draw Text (Gold Header + Crisp Track Info)
        int textX = x + 8;
        int textY = y + 4;
        graphics.drawString(font, titlePrefix, textX, textY, RenderUtils.SOVIET_RED, false);
        int prefixW = font.width(titlePrefix);
        graphics.drawString(font, mainText, textX + prefixW, textY, RenderUtils.SOVIET_GOLD, false);

        // 3. Draw Mini Animated Visualizer Waves (when playing)
        if (showWaveform.get()) {
            int waveX = x + 8;
            int waveY = y + 16;
            long time = System.currentTimeMillis();

            for (int i = 0; i < 18; i++) {
                float barHeight = 2.0f;
                if (playing) {
                    double phase = (time / 180.0) + (i * 0.45);
                    barHeight = (float) (2.0 + Math.abs(Math.sin(phase)) * 5.0);
                }
                int barColor = (i % 2 == 0) ? RenderUtils.SOVIET_GOLD : RenderUtils.SOVIET_RED;
                graphics.fill(waveX + (i * 4), (int)(waveY + 6 - barHeight), waveX + (i * 4) + 2, waveY + 6, barColor);
            }
        }
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
            ImGui.textDisabled("Showing Demo Track (turn on Spotify & play any song to auto-detect)");
        }

        ImGui.separator();
        ImGui.spacing();

        if (ImGui.checkbox("Demo Mode (Always visible)##sp", demoMode)) {
            SpotifyUtils.setMockMode(demoMode.get());
        }
        ImGui.checkbox("Animated Waveform##sp", showWaveform);

        ImGui.sliderInt("Position X##sp", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##sp", posY.getData(), 0, 800);

        if (demoMode.get()) {
            ImGui.spacing();
            ImGui.textDisabled("Custom Demo Track:");
            if (ImGui.inputText("Artist##sp", customArtist)) {
                SpotifyUtils.setArtist(customArtist.get());
            }
            if (ImGui.inputText("Track##sp", customTrack)) {
                SpotifyUtils.setTrackName(customTrack.get());
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
