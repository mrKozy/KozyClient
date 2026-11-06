// ☭ Red Star Client — StreamProofModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;

/**
 * StreamProof / Streamer Mode:
 * Ensures the game looks completely legit and clean on OBS, Discord, and recordings:
 * - Hides visual ESP, tracers, and wallhacks from the rendered screen
 * - Hides server IP and coordinates to prevent stream snipes
 * - Masks player names
 * - Hides the client watermark
 */
public class StreamProofModule extends Module {

    private static StreamProofModule instance;

    private final ImBoolean hideVisuals   = new ImBoolean(true);
    private final ImBoolean hideCoords    = new ImBoolean(true);
    private final ImBoolean hideServerIP  = new ImBoolean(true);
    private final ImBoolean hideWatermark = new ImBoolean(true);
    private final ImBoolean hideNames     = new ImBoolean(false);
    private final ImString  fakeName      = new ImString("Proletariat_Hero", 64);

    public StreamProofModule() {
        super("StreamProof", "Streamer-Modus: Versteckt ESP/Visuals, Server-IP, Koordinaten & Watermark fuer saubere Streams (OBS/Discord).", ModuleCategory.RENDER, -1);
        instance = this;
    }

    public static boolean isStreamProofActive() {
        return instance != null && instance.isEnabled();
    }

    public static boolean shouldHideVisuals() {
        return instance != null && instance.isEnabled() && instance.hideVisuals.get();
    }

    public static boolean shouldHideCoords() {
        return instance != null && instance.isEnabled() && instance.hideCoords.get();
    }

    public static boolean shouldHideServerIP() {
        return instance != null && instance.isEnabled() && instance.hideServerIP.get();
    }

    public static boolean shouldHideWatermark() {
        return instance != null && instance.isEnabled() && instance.hideWatermark.get();
    }

    public static boolean shouldHideNames() {
        return instance != null && instance.isEnabled() && instance.hideNames.get();
    }

    public static String getFakeName() {
        return instance != null ? instance.fakeName.get() : "Streamer";
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("ESP, Tracers & Wallhacks auf dem Bildschirm ausblenden##sp", hideVisuals);
        ImGui.checkbox("Server-IP maskieren (Anti-Stream-Snipe)##sp", hideServerIP);
        ImGui.checkbox("F3 / HUD Koordinaten maskieren##sp", hideCoords);
        ImGui.checkbox("Client-Watermark ausblenden##sp", hideWatermark);
        ImGui.checkbox("Spielernamen durch Fake-Namen ersetzen##sp", hideNames);
        if (hideNames.get()) {
            ImGui.inputText("Eigener Fake-Name##sp", fakeName);
        }
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ OBS / Twitch / YouTube Sicher: Dein Spiel sieht 100% Vanilla aus.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("hideVisuals", hideVisuals.get());
        cfg.putSetting("hideCoords", hideCoords.get());
        cfg.putSetting("hideServerIP", hideServerIP.get());
        cfg.putSetting("hideWatermark", hideWatermark.get());
        cfg.putSetting("hideNames", hideNames.get());
        cfg.putSetting("fakeName", fakeName.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        hideVisuals.set(cfg.getSetting("hideVisuals", hideVisuals.get()));
        hideCoords.set(cfg.getSetting("hideCoords", hideCoords.get()));
        hideServerIP.set(cfg.getSetting("hideServerIP", hideServerIP.get()));
        hideWatermark.set(cfg.getSetting("hideWatermark", hideWatermark.get()));
        hideNames.set(cfg.getSetting("hideNames", hideNames.get()));
        fakeName.set(cfg.getSetting("fakeName", fakeName.get()));
    }
}
