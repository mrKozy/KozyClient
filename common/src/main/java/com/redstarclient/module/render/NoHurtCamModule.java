// ☭ Red Star Client — NoHurtCamModule
package com.redstarclient.module.render;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;

/** Disables screen shake when the player takes damage. Applied via GameRenderer mixin. */
public class NoHurtCamModule extends Module {

    public NoHurtCamModule() {
        super("NoHurtCam", "Disables screen shake when damaged.", ModuleCategory.RENDER, -1);
    }

    @Override public void renderImGuiConfig() {
        ImGui.textDisabled("Cancels the hurt-camera bob animation.");
        ImGui.textDisabled("Applied via bobHurt() mixin.");
    }
}
