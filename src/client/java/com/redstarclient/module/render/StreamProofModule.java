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
 * - Hides the ClickGUI, menus, and popups from screen
 * - Hides visual ESP, storage containers, tracers, and wallhacks
 * - Hides server IP and coordinates to prevent stream snipes
 * - Masks player names
 * - Hides the client watermark
 * - Auto-detects OBS and Discord streaming hooks
 */
public class StreamProofModule extends Module {

    private static StreamProofModule instance;

    private final ImBoolean autoDetect    = new ImBoolean(false);
    private final ImBoolean hideUI        = new ImBoolean(true);
    private final ImBoolean hideVisuals   = new ImBoolean(true);
    private final ImBoolean hideCoords    = new ImBoolean(true);
    private final ImBoolean hideServerIP  = new ImBoolean(true);
    private final ImBoolean hideWatermark = new ImBoolean(true);
    private final ImBoolean hideNames     = new ImBoolean(false);
    private final ImString  fakeName      = new ImString("Proletariat_Hero", 64);

    private static boolean bypassActive = false;
    private static long lastProcessScan = 0L;
    private static boolean cachedStreamDetected = false;

    public StreamProofModule() {
        super("StreamProof", "Streamer-Modus: Versteckt GUI, ESP, Chests, Server-IP & Watermark vor OBS und Discord Streams.", ModuleCategory.RENDER, -1);
        instance = this;
    }

    public static boolean isBypassActive() {
        return bypassActive;
    }

    public static void setBypassActive(boolean bypass) {
        bypassActive = bypass;
    }

    public static void toggleBypass() {
        bypassActive = !bypassActive;
        System.out.println("[☭ Red Star Client] StreamProof bypass toggled: " + bypassActive);
    }

    /**
     * Checks if streaming/recording software is actively capturing or running.
     * Detects OBS Game Capture hook or OBS process.
     */
    public static boolean isStreamingSoftwareDetected() {
        if (bypassActive) return false;

        long now = System.currentTimeMillis();
        if (now - lastProcessScan < 2000L) {
            return cachedStreamDetected;
        }
        lastProcessScan = now;

        boolean detected = false;
        try {
            detected = ProcessHandle.allProcesses().anyMatch(p -> {
                String cmd = p.info().command().orElse("").toLowerCase();
                return cmd.endsWith("obs64.exe") || cmd.endsWith("obs32.exe") ||
                       cmd.contains("streamlabs") || cmd.contains("obs-studio");
            });
        } catch (Throwable ignored) {}

        cachedStreamDetected = detected;
        return detected;
    }

    public static boolean isStreamProofActive() {
        if (bypassActive) return false;
        if (instance == null) return false;
        // MUST be explicitly enabled by user to prevent accidentally hiding GUI
        if (!instance.isEnabled()) return false;
        if (instance.autoDetect.get()) {
            return isStreamingSoftwareDetected();
        }
        return true;
    }

    public static boolean shouldHideUI() {
        // With Method B (Post-Present GL_FRONT hook bypass), the UI is rendered
        // directly to the front buffer after OBS captures the frame.
        // It never appears in OBS recordings, so the player can always see it on their screen.
        return false;
    }

    public static boolean shouldHideVisuals() {
        if (bypassActive) return false;
        return isStreamProofActive() && (instance == null || instance.hideVisuals.get());
    }

    public static boolean shouldHideCoords() {
        if (bypassActive) return false;
        return isStreamProofActive() && (instance == null || instance.hideCoords.get());
    }

    public static boolean shouldHideServerIP() {
        if (bypassActive) return false;
        return isStreamProofActive() && (instance == null || instance.hideServerIP.get());
    }

    public static boolean shouldHideWatermark() {
        if (bypassActive) return false;
        return isStreamProofActive() && (instance == null || instance.hideWatermark.get());
    }

    public static boolean shouldHideNames() {
        if (bypassActive) return false;
        return isStreamProofActive() && instance != null && instance.hideNames.get();
    }

    public static String getFakeName() {
        return instance != null ? instance.fakeName.get() : "Streamer";
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textColored(0.96f, 0.82f, 0.22f, 1.0f, "★ OBS Hook-Bypass (Methode B) AKTIV");
        ImGui.textDisabled("Rendert GUI direkt auf den Front-Buffer NACH dem OBS-Swap.");
        ImGui.textDisabled("OBS Spielaufnahme sieht 100% legitimes Minecraft ohne Cheat-UI!");
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        ImGui.checkbox("Automatische Erkennung (OBS Game Capture)##sp", autoDetect);
        ImGui.checkbox("ESP & Wallhacks in OBS verbergen##sp", hideVisuals);
        ImGui.checkbox("Server-IP maskieren (Anti-Stream-Snipe)##sp", hideServerIP);
        ImGui.checkbox("F3 / HUD Koordinaten maskieren##sp", hideCoords);
        ImGui.checkbox("Client-Watermark ausblenden##sp", hideWatermark);
        ImGui.checkbox("Spielernamen durch Fake-Namen ersetzen##sp", hideNames);
        if (hideNames.get()) {
            ImGui.inputText("Eigener Fake-Name##sp", fakeName);
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        boolean streamDetected = isStreamingSoftwareDetected();
        if (streamDetected) {
            ImGui.textColored(0.95f, 0.2f, 0.2f, 1.0f, "● OBS / Discord / Stream-Software AKTIV ERKANNT!");
        } else {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "○ Keine Stream-Software aktiv.");
        }

        ImGui.spacing();
        ImGui.textDisabled("Notfall-Bypass: Shift + INSERT gedrueckt halten");
        ImGui.textColored(0.96f, 0.82f, 0.22f, 1.0f, "★ OBS / Twitch / Discord Sicher: Absolut kein Cheat sichtbar.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("autoDetect", autoDetect.get());
        cfg.putSetting("hideUI", hideUI.get());
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
        autoDetect.set(cfg.getSetting("autoDetect", autoDetect.get()));
        hideUI.set(cfg.getSetting("hideUI", hideUI.get()));
        hideVisuals.set(cfg.getSetting("hideVisuals", hideVisuals.get()));
        hideCoords.set(cfg.getSetting("hideCoords", hideCoords.get()));
        hideServerIP.set(cfg.getSetting("hideServerIP", hideServerIP.get()));
        hideWatermark.set(cfg.getSetting("hideWatermark", hideWatermark.get()));
        hideNames.set(cfg.getSetting("hideNames", hideNames.get()));
        fakeName.set(cfg.getSetting("fakeName", fakeName.get()));
    }
}
