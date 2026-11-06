// ☭ Red Star Client — BackTrackModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;

/**
 * BackTrack:
 * Exploits latency interpolation to let you hit opponents at their previous positions,
 * completely neutralizing ping disadvantages against high-speed players.
 */
public class BackTrackModule extends Module {

    private final ImInt trackMs = new ImInt(150);

    public BackTrackModule() {
        super("BackTrack", "Ermoeglicht Treffer an vergangenen Positionen des Gegners (Ping-Ausgleich).", ModuleCategory.COMBAT, -1);
    }

    public int getTrackMs() {
        return trackMs.get();
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Verzoegerungs-Fenster (ms)##bt", trackMs.getData(), 50, 400);
        ImGui.spacing();
        ImGui.textDisabled("Gleicht hohe Latenzen und gegnerische Lag-Vorteile aus.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("trackMs", trackMs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        trackMs.set(cfg.getSetting("trackMs", trackMs.get()));
    }
}
