// ☭ Red Star Client — CameraClipModule (from Wurst 7)
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;

/**
 * CameraClip (from Wurst 7):
 * Prevents the third-person camera (F5) from being pushed forward by walls and blocks.
 */
public class CameraClipModule extends Module {

    private final ImFloat distance = new ImFloat(4.0f);

    public CameraClipModule() {
        super("CameraClip", "Laesst die Third-Person-Kamera (F5) durch Waende und Bloecke gleiten (Wurst 7).", ModuleCategory.RENDER, -1);
    }

    public float getDistance() {
        return distance.get();
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Kamera-Distanz##cclip", distance.getData(), 2.0f, 12.0f, "%.1fm");
        ImGui.spacing();
        ImGui.textDisabled("Verhindert das Heranzoomen der Kamera an Waenden in der 3rd-Person-Perspektive.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("distance", distance.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        distance.set(cfg.getSetting("distance", distance.get()));
    }
}
