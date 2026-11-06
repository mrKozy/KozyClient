// ☭ Red Star Client — AntiKickModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.redstarclient.theme.CommunistTheme;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Premium Anti-Kick & GrimAC Protection System.
 * Protects against kicks and bans on servers running modern anti-cheats (GrimAC, Vulcan, Matrix):
 * - Predictive Reach Limiter (<3.0 blocks raycast with sinusoidal jitter)
 * - Safe Knockback Simulation (prevents instant VelocityA flags)
 * - Dynamic Setback Absorber (pauses aggressive movement on rubberband)
 * - Pitch/Yaw angle bounds sanitizer
 * - Safe Action Pacing (prevents FastPlace / FastBreak packet spam)
 * - Real-time HUD Status Indicator
 */
public class AntiKickModule extends Module implements DraggableHudElement {

    private static final String[] PRESETS = { "GrimAC 2.0 (Strict)", "Vulcan / Matrix", "Anarchy / Unrestricted", "Custom / Advanced" };
    private static final String[] SPOOF_BRANDS = { "Vanilla (Safest)", "Lunar Client", "Badlion", "Fabric (Real)" };

    private final ImInt     presetIndex         = new ImInt(0); // Default GrimAC 2.0
    private final ImBoolean brandSpoof          = new ImBoolean(true); // OpSec brand spoofing active
    private final ImInt     brandMode           = new ImInt(0); // Default Vanilla
    private final ImBoolean blockModChannels    = new ImBoolean(true); // OpSec channel protection
    private final ImBoolean knownPacksProtection= new ImBoolean(true); // OpSec known packs handshake protection
    private final ImBoolean keybindProtection   = new ImBoolean(true); // OpSec keybind fingerprint protection
    private final ImBoolean telemetryBlocking   = new ImBoolean(true); // OpSec telemetry blocking
    private final ImBoolean blockPackTracking   = new ImBoolean(true); // OpSec pack tracker protection
    private final ImBoolean clampReach          = new ImBoolean(true);
    private final ImFloat   maxReach            = new ImFloat(2.95f);
    private final ImBoolean safeVelocity        = new ImBoolean(true);
    private final ImFloat   minKbFactor         = new ImFloat(68f); // 68% horizontal knockback
    private final ImBoolean safeActionPacing    = new ImBoolean(true);
    private final ImBoolean panicOnSetback      = new ImBoolean(true);
    private final ImBoolean showHudBadge        = new ImBoolean(true);

    // Draggable HUD coordinates
    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(15);

    // Live Metrics
    private int setbackCooldown = 0;
    private int setbacksAbsorbed = 0;
    private long lastSetbackTime = 0;

    public AntiKickModule() {
        super("AntiKick", "Premium GrimAC bypass & OpSec server kick protection system.", ModuleCategory.COMBAT, -1);
        setEnabled(true); // Default ON
    }

    @Override
    public void onTick(Minecraft mc) {
        if (setbackCooldown > 0) {
            setbackCooldown--;
        }
    }

    /** Called when the player receives a position setback packet from the server. */
    public void notifySetback() {
        setbacksAbsorbed++;
        lastSetbackTime = System.currentTimeMillis();
        if (panicOnSetback.get()) {
            setbackCooldown = 25; // 1.25s safety pause
        }
    }

    public boolean isSafetyPaused() {
        return isEnabled() && setbackCooldown > 0;
    }

    public boolean isClampReach() {
        return isEnabled() && clampReach.get();
    }

    public float getSafeMaxReach() {
        if (!isClampReach()) return 6.0f;
        // Dynamic micro-jitter between (maxReach - 0.04) and maxReach
        double jitter = (Math.sin(System.currentTimeMillis() / 200.0) + 1.0) * 0.02;
        return (float) Math.min(2.98, maxReach.get() - jitter);
    }

    public boolean isSafeVelocity() {
        return isEnabled() && safeVelocity.get();
    }

    public float getSafeHorizontalKb(float requested) {
        if (!isSafeVelocity()) return requested;
        return Math.max(requested, minKbFactor.get() / 100.0f);
    }

    public boolean isSafeActionPacing() {
        return isEnabled() && safeActionPacing.get();
    }

    public boolean isGrimMode() {
        return isEnabled() && (presetIndex.get() == 0 || presetIndex.get() == 3);
    }

    public boolean isBrandSpoof() {
        return isEnabled() && brandSpoof.get();
    }

    public String getSpoofedBrand() {
        if (!isBrandSpoof()) return "fabric";
        return switch (brandMode.get()) {
            case 0 -> "vanilla";
            case 1 -> "lunarclient:v2.18.0";
            case 2 -> "badlion";
            default -> "fabric";
        };
    }

    public boolean isBlockModChannels() {
        return isEnabled() && blockModChannels.get();
    }

    public boolean isKnownPacksProtection() {
        return isEnabled() && knownPacksProtection.get();
    }

    public boolean isKeybindProtection() {
        return isEnabled() && keybindProtection.get();
    }

    public boolean isTelemetryBlocking() {
        return isEnabled() && telemetryBlocking.get();
    }

    public boolean isBlockPackTracking() {
        return isEnabled() && blockPackTracking.get();
    }

    // ── Draggable HUD Badge ───────────────────────────────────────────────────

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        if (!isEnabled() || !showHudBadge.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.screen != null) return;
        Font font = mc.font;

        int x = posX.get();
        int y = posY.get();

        boolean paused = isSafetyPaused();
        String statusText = paused ? "RECOVERING" : "SECURE";
        int statusColor = paused ? RenderUtils.SOVIET_RED : RenderUtils.toARGB(0.35f, 0.95f, 0.45f, 1.0f);

        String title = "🛡️ ANTI-KICK: " + statusText;
        String info = "Reach: " + String.format("%.2f", getSafeMaxReach()) + "m | Absorbed: " + setbacksAbsorbed;

        int tw = Math.max(font.width(title), font.width(info));
        int boxW = tw + 16;
        int boxH = 26;

        // Dark red glassmorphic badge
        graphics.fill(x, y, x + boxW, y + boxH, RenderUtils.toARGB(0.06f, 0.02f, 0.02f, 0.85f));
        graphics.fill(x, y, x + 3, y + boxH, statusColor);
        graphics.fill(x + 3, y, x + boxW, y + 1, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.35f));
        graphics.fill(x + 3, y + boxH - 1, x + boxW, y + boxH, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.25f));
        graphics.fill(x + boxW - 1, y, x + boxW, y + boxH, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.25f));

        graphics.drawString(font, title, x + 7, y + 4, statusColor, false);
        graphics.drawString(font, info, x + 7, y + 14, RenderUtils.SOVIET_GOLD, false);
    }

    @Override public String getHudName() { return "Anti-Kick Status"; }
    @Override public int getHudX() { return posX.get(); }
    @Override public int getHudY() { return posY.get(); }
    @Override public int getHudWidth() { return 155; }
    @Override public int getHudHeight() { return 26; }
    @Override public void setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled() { return isEnabled() && showHudBadge.get(); }

    // ── ImGui Configuration ───────────────────────────────────────────────────

    @Override
    public void renderImGuiConfig() {
        ImGui.textColored(0.35f, 0.95f, 0.45f, 1.0f, "● Enterprise GrimAC 2.0 & OpSec 1.1.7.1 Active");
        ImGui.spacing();

        if (ImGui.combo("Preset Mode##ak", presetIndex, PRESETS)) {
            applyPreset(presetIndex.get());
        }

        ImGui.separator();
        ImGui.checkbox("Show HUD Badge##ak", showHudBadge);
        ImGui.checkbox("Clamp Attack Reach##ak", clampReach);
        if (clampReach.get()) {
            ImGui.sliderFloat("Max Safe Reach##ak", maxReach.getData(), 2.6f, 3.0f, "%.2f blocks");
        }

        ImGui.separator();
        ImGui.checkbox("Legit Knockback Reduction##ak", safeVelocity);
        if (safeVelocity.get()) {
            ImGui.sliderFloat("Min Horizontal KB %##ak", minKbFactor.getData(), 50f, 95f, "%.0f%%");
            ImGui.textDisabled("Grim flags 0% velocity immediately. 65-75% is optimal.");
        }

        ImGui.separator();
        ImGui.checkbox("Safe Interaction Pacing##ak", safeActionPacing);
        ImGui.checkbox("Auto-Mitigate on Setbacks##ak", panicOnSetback);

        ImGui.separator();
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                "🛡️ OpSec 1.1.7.1 Anti-Detection & Privacy Suite:");

        ImGui.checkbox("Enable Client Brand Spoofing##spoof", brandSpoof);
        if (brandSpoof.get()) {
            ImGui.combo("Client Brand##spoof", brandMode, SPOOF_BRANDS);
            ImGui.textDisabled("Fakes client name so servers cannot detect Fabric/custom clients.");
        }

        ImGui.spacing();
        ImGui.checkbox("Block Mod Payload Channels (Hide Fabric)##spoof", blockModChannels);
        ImGui.textDisabled("Drops outgoing fabric:* payloads preventing server channel probing.");

        ImGui.spacing();
        ImGui.checkbox("Known-Packs Handshake Guard (1.20.5-1.21.4)##spoof", knownPacksProtection);
        ImGui.textDisabled("Filters mod IDs in config handshake; server sees only vanilla Minecraft.");

        ImGui.spacing();
        ImGui.checkbox("Keybind Fingerprint Guard##spoof", keybindProtection);
        ImGui.textDisabled("Blocks translation exploit packets probing keybind names.");

        ImGui.spacing();
        ImGui.checkbox("Block Mojang Telemetry & Tracking##spoof", telemetryBlocking);
        ImGui.textDisabled("Disables telemetry sessions and stops telemetry data transmission.");

        ImGui.spacing();
        ImGui.checkbox("ResourcePack Tracking Guard##spoof", blockPackTracking);
        ImGui.textDisabled("Auto-acknowledges forced server packs without downloading URLs or leaking IP.");

        ImGui.separator();
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                "Live Security Telemetry:");
        ImGui.text("Setbacks Absorbed: " + setbacksAbsorbed);
        if (lastSetbackTime > 0) {
            long agoSec = (System.currentTimeMillis() - lastSetbackTime) / 1000;
            ImGui.textDisabled("Last setback: " + agoSec + "s ago");
        }
        if (ImGui.smallButton("Reset Counter##ak")) {
            setbacksAbsorbed = 0;
        }
    }

    private void applyPreset(int index) {
        switch (index) {
            case 0 -> { // GrimAC Strict
                clampReach.set(true);
                maxReach.set(2.95f);
                safeVelocity.set(true);
                minKbFactor.set(68f);
                safeActionPacing.set(true);
                panicOnSetback.set(true);
                brandSpoof.set(true);
                brandMode.set(0);
                blockModChannels.set(true);
                knownPacksProtection.set(true);
                keybindProtection.set(true);
                telemetryBlocking.set(true);
                blockPackTracking.set(true);
            }
            case 1 -> { // Vulcan / Matrix
                clampReach.set(true);
                maxReach.set(2.98f);
                safeVelocity.set(true);
                minKbFactor.set(60f);
                safeActionPacing.set(true);
                panicOnSetback.set(true);
                brandSpoof.set(true);
                brandMode.set(0);
                blockModChannels.set(true);
                knownPacksProtection.set(true);
                keybindProtection.set(true);
                telemetryBlocking.set(true);
                blockPackTracking.set(true);
            }
            case 2 -> { // Anarchy / Unrestricted
                clampReach.set(false);
                maxReach.set(4.5f);
                safeVelocity.set(false);
                minKbFactor.set(0f);
                safeActionPacing.set(false);
                panicOnSetback.set(false);
            }
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("presetIndex", presetIndex.get());
        cfg.putSetting("brandSpoof", brandSpoof.get());
        cfg.putSetting("brandMode", brandMode.get());
        cfg.putSetting("blockModChannels", blockModChannels.get());
        cfg.putSetting("knownPacksProtection", knownPacksProtection.get());
        cfg.putSetting("keybindProtection", keybindProtection.get());
        cfg.putSetting("telemetryBlocking", telemetryBlocking.get());
        cfg.putSetting("blockPackTracking", blockPackTracking.get());
        cfg.putSetting("clampReach", clampReach.get());
        cfg.putSetting("maxReach", maxReach.get());
        cfg.putSetting("safeVelocity", safeVelocity.get());
        cfg.putSetting("minKbFactor", minKbFactor.get());
        cfg.putSetting("safeActionPacing", safeActionPacing.get());
        cfg.putSetting("panicOnSetback", panicOnSetback.get());
        cfg.putSetting("showHudBadge", showHudBadge.get());
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        presetIndex.set(cfg.getSetting("presetIndex", presetIndex.get()));
        brandSpoof.set(cfg.getSetting("brandSpoof", brandSpoof.get()));
        brandMode.set(cfg.getSetting("brandMode", brandMode.get()));
        blockModChannels.set(cfg.getSetting("blockModChannels", blockModChannels.get()));
        knownPacksProtection.set(cfg.getSetting("knownPacksProtection", knownPacksProtection.get()));
        keybindProtection.set(cfg.getSetting("keybindProtection", keybindProtection.get()));
        telemetryBlocking.set(cfg.getSetting("telemetryBlocking", telemetryBlocking.get()));
        blockPackTracking.set(cfg.getSetting("blockPackTracking", blockPackTracking.get()));
        clampReach.set(cfg.getSetting("clampReach", clampReach.get()));
        maxReach.set(cfg.getSetting("maxReach", maxReach.get()));
        safeVelocity.set(cfg.getSetting("safeVelocity", safeVelocity.get()));
        minKbFactor.set(cfg.getSetting("minKbFactor", minKbFactor.get()));
        safeActionPacing.set(cfg.getSetting("safeActionPacing", safeActionPacing.get()));
        panicOnSetback.set(cfg.getSetting("panicOnSetback", panicOnSetback.get()));
        showHudBadge.set(cfg.getSetting("showHudBadge", showHudBadge.get()));
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
    }
}
