// ☭ Red Star Client — MotionBlurModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;

/**
 * Cinematic Motion Blur:
 * Adds smooth frame accumulation blur to fast camera pans.
 */
public class MotionBlurModule extends Module {

    private final ImFloat blurStrength = new ImFloat(0.45f);

    public MotionBlurModule() {
        super("MotionBlur", "Erzeugt kinoreife Bewegungsunschaerfe bei schnellen Kameradrehungen.", ModuleCategory.RENDER, -1);
    }

    public float getBlurStrength() {
        return isEnabled() ? blurStrength.get() : 0.0f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Blur-Staerke##mb", blurStrength.getData(), 0.1f, 0.9f, "%.2f");
        ImGui.spacing();
        ImGui.textDisabled("Simuliert weiche High-FPS Shutter-Effekte fuer PvP-Montagen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("blurStrength", blurStrength.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        blurStrength.set(cfg.getSetting("blurStrength", blurStrength.get()));
    }
}
