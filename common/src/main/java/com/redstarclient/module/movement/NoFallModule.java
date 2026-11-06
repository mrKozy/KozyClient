// ☭ Red Star Client — NoFallModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;

/**
 * Cancels all fall damage.
 * Actual cancellation is in the causeFallDamage() mixin.
 */
public class NoFallModule extends Module {

    public NoFallModule() {
        super("NoFall", "Cancels all fall damage.", ModuleCategory.MOVEMENT, -1);
    }

    @Override public void renderImGuiConfig() {
        ImGui.textDisabled("Cancels all fall damage.");
        ImGui.textDisabled("Behavior applied via causeFallDamage() mixin.");
    }
}
