// ☭ Red Star Client — SafeWalkModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;

/**
 * Prevents falling off block edges.
 * Actual behavior is enforced by the canClipAtLedge() mixin.
 */
public class SafeWalkModule extends Module {

    public SafeWalkModule() {
        super("SafeWalk", "Prevents falling off block edges.", ModuleCategory.MOVEMENT, -1);
    }

    @Override public void renderImGuiConfig() {
        ImGui.textDisabled("Prevents falling off block edges.");
        ImGui.textDisabled("Behavior applied via canClipAtLedge() mixin.");
    }
}
