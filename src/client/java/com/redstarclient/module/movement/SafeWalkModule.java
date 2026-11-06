// ☭ Red Star Client — SafeWalkModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;

/**
 * Prevents falling off block edges.
 * Actual behavior is enforced by PlayerMixin hooking isStayingOnGroundSurface().
 */
public class SafeWalkModule extends Module {

    public SafeWalkModule() {
        super("SafeWalk", "Prevents falling off block edges without needing to sneak.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Edge protection active (isStayingOnGroundSurface).");
        ImGui.textDisabled("Prevents falling off edges automatically as if sneaking.");
    }
}
