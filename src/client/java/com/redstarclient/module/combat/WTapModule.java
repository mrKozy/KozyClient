// ☭ Red Star Client — WTapModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;

/**
 * W-Tap:
 * Automatically resets sprint immediately upon landing an attack hit
 * to deal 100% maximum knockback on every single strike.
 */
public class WTapModule extends Module {

    private final ImInt resetTicks = new ImInt(2);
    private int tickCounter = 0;
    private boolean resetting = false;

    public WTapModule() {
        super("W-Tap", "Setzt Sprint nach jedem Treffer kurz zurueck fuer maximalen Rueckstoss (Knockback).", ModuleCategory.COMBAT, -1);
    }

    public void onAttackLanded() {
        if (!isEnabled()) return;
        resetting = true;
        tickCounter = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (!resetting || mc.player == null) return;

        tickCounter++;
        if (tickCounter <= resetTicks.get()) {
            // Briefly stop sprinting to reset sprint knockback momentum
            mc.player.setSprinting(false);
        } else {
            resetting = false;
            if (mc.options.keyUp.isDown()) {
                mc.player.setSprinting(true);
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Reset-Dauer (Ticks)##wt", resetTicks.getData(), 1, 4);
        ImGui.spacing();
        ImGui.textDisabled("Garantiert ersten Sprint-Hit Knockback bei jedem Schlag.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("resetTicks", resetTicks.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        resetTicks.set(cfg.getSetting("resetTicks", resetTicks.get()));
    }
}
