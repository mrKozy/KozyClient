// ☭ Red Star Client — VelocityModule
package com.redstarclient.module.combat;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;

/**
 * Reduces or resets knockback received from hits.
 * Features:
 * - JumpReset (GrimAC, Vulcan, Polar, Intave bypass): Vanilla physics jump at hurtTime 9.
 * - Custom %: Scaled horizontal and vertical knockback.
 * - Cancel: Zero knockback.
 */
public class VelocityModule extends Module {

    private static final String[] MODES = { "JumpReset (Grim/Vulcan)", "Custom %", "Cancel" };
    private final ImInt mode = new ImInt(0);

    private final ImFloat horizontal = new ImFloat(0f);   // 0 % = full cancel
    private final ImFloat vertical   = new ImFloat(100f); // 100 % = unchanged

    private boolean isFallDamage = false;

    public VelocityModule() {
        super("Velocity", "Reduces or resets knockback received from attacks.", ModuleCategory.COMBAT, -1);
    }

    public boolean isJumpReset() {
        return isEnabled() && mode.get() == 0;
    }

    public boolean isCancel() {
        return isEnabled() && mode.get() == 2;
    }

    public void setFallDamage(boolean fallDamage) {
        this.isFallDamage = fallDamage;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || !isEnabled()) return;

        // JumpReset Mode (LiquidBounce bypass logic)
        if (mode.get() == 0) {
            if (mc.player.hurtTime == 9 && mc.player.onGround() && mc.player.isSprinting() && !isFallDamage) {
                mc.player.jumpFromGround();
            }
        }
    }

    /** Returns the horizontal knockback multiplier (0.0 – 1.0). */
    public float getHorizontalFactor() {
        if (mode.get() == 2) return 0f;
        float raw = horizontal.get() / 100f;
        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafeVelocity()) {
            return antiKick.getSafeHorizontalKb(raw);
        }
        return raw;
    }

    /** Returns the vertical knockback multiplier (0.0 – 1.0). */
    public float getVerticalFactor() {
        if (mode.get() == 2) return 0f;
        return vertical.get() / 100f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Mode##vel", mode, MODES);

        if (mode.get() == 0) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "JumpReset Bypass: 100% legit physics reset on hurtTime 9.");
            ImGui.textDisabled("Bypasses GrimAC 2.3.71+, Vulcan 288, Polar, Matrix, Intave.");
        } else if (mode.get() == 1) {
            ImGui.sliderFloat("Horizontal %##vel", horizontal.getData(), 0f, 100f, "%.0f%%");
            ImGui.sliderFloat("Vertical %##vel",   vertical.getData(),   0f, 100f, "%.0f%%");
            ImGui.textDisabled("0% = full cancel, 100% = unchanged.");

            AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
            if (antiKick != null && antiKick.isSafeVelocity()) {
                ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "AntiKick Protection: Knockback safely clamped to avoid Grim flags.");
            }
        } else {
            ImGui.textColored(0.9f, 0.3f, 0.3f, 1.0f, "Full Cancel: Completely ignores knockback (May flag strict anticheats).");
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("mode", mode.get());
        cfg.putSetting("horizontal", horizontal.get());
        cfg.putSetting("vertical", vertical.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        mode.set(cfg.getSetting("mode", mode.get()));
        horizontal.set(cfg.getSetting("horizontal", horizontal.get()));
        vertical.set(cfg.getSetting("vertical", vertical.get()));
    }
}
