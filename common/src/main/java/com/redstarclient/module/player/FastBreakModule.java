// ☭ Red Star Client — FastBreakModule (from Wurst 7)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

/**
 * FastBreak (from Wurst 7):
 * Eliminates block breaking delay and accelerates mining progress.
 */
public class FastBreakModule extends Module {

    private final ImBoolean removeDelay     = new ImBoolean(true);
    private final ImFloat   speedMultiplier = new ImFloat(1.35f);

    public FastBreakModule() {
        super("FastBreak", "Entfernt die Verzoegerung beim Abbauen von Bloecken fuer hoehere Geschwindigkeit (Wurst 7).", ModuleCategory.PLAYER, -1);
    }

    public boolean isRemoveDelay() {
        return isEnabled() && removeDelay.get();
    }

    public float getSpeedMultiplier() {
        return isEnabled() ? speedMultiplier.get() : 1.0f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Abbau-Verzoegerung entfernen (Zero Delay)##fbrk", removeDelay);
        ImGui.sliderFloat("Geschwindigkeits-Multiplikator##fbrk", speedMultiplier.getData(), 1.0f, 2.5f, "%.2fx");
        ImGui.spacing();
        ImGui.textDisabled("Spart wertvolle Zeit beim Minen und Bloecke-Zerstoeren.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("removeDelay", removeDelay.get());
        cfg.putSetting("speedMultiplier", speedMultiplier.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        removeDelay.set(cfg.getSetting("removeDelay", removeDelay.get()));
        speedMultiplier.set(cfg.getSetting("speedMultiplier", speedMultiplier.get()));
    }
}
