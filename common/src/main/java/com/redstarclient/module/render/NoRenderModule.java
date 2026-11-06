// ☭ Red Star Client — NoRenderModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * Disables intrusive visual overlays: screen fire, pumpkin blur, blindness & darkness.
 */
public class NoRenderModule extends Module {

    private final ImBoolean noFire      = new ImBoolean(true);
    private final ImBoolean noPumpkin   = new ImBoolean(true);
    private final ImBoolean noBlindness = new ImBoolean(true);

    public NoRenderModule() {
        super("NoRender", "Disables screen fire, pumpkin blur, and blindness overlays.", ModuleCategory.RENDER, -1);
    }

    public boolean isNoFire()      { return isEnabled() && noFire.get(); }
    public boolean isNoPumpkin()   { return isEnabled() && noPumpkin.get(); }
    public boolean isNoBlindness() { return isEnabled() && noBlindness.get(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("No Fire Overlay (Lava/Fire)##nr", noFire);
        ImGui.checkbox("No Pumpkin Blur##nr", noPumpkin);
        ImGui.checkbox("No Blindness / Darkness Fog##nr", noBlindness);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("noFire", noFire.get());
        cfg.putSetting("noPumpkin", noPumpkin.get());
        cfg.putSetting("noBlindness", noBlindness.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        noFire.set(cfg.getSetting("noFire", noFire.get()));
        noPumpkin.set(cfg.getSetting("noPumpkin", noPumpkin.get()));
        noBlindness.set(cfg.getSetting("noBlindness", noBlindness.get()));
    }
}
