// ☭ Red Star Client — VelocityModule
package com.redstarclient.module.combat;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;

/**
 * Reduces knockback received from hits.
 * Integrates with AntiKickModule to prevent GrimAC VelocityA bans.
 */
public class VelocityModule extends Module {

    private final ImFloat horizontal = new ImFloat(0f);   // 0 % = full cancel
    private final ImFloat vertical   = new ImFloat(100f); // 100 % = unchanged

    public VelocityModule() {
        super("Velocity", "Reduces knockback received from attacks.", ModuleCategory.COMBAT, -1);
    }

    /** Returns the horizontal knockback multiplier (0.0 – 1.0). */
    public float getHorizontalFactor() {
        float raw = horizontal.get() / 100f;
        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafeVelocity()) {
            return antiKick.getSafeHorizontalKb(raw);
        }
        return raw;
    }

    /** Returns the vertical knockback multiplier (0.0 – 1.0). */
    public float getVerticalFactor() {
        return vertical.get() / 100f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Horizontal %##vel", horizontal.getData(), 0f, 100f, "%.0f%%");
        ImGui.sliderFloat("Vertical %##vel",   vertical.getData(),   0f, 100f, "%.0f%%");
        ImGui.textDisabled("0% = full cancel, 100% = unchanged.");

        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafeVelocity()) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "AntiKick Protection: Knockback safely clamped to avoid Grim flags.");
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("horizontal", horizontal.get());
        cfg.putSetting("vertical", vertical.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        horizontal.set(cfg.getSetting("horizontal", horizontal.get()));
        vertical.set(cfg.getSetting("vertical", vertical.get()));
    }
}
