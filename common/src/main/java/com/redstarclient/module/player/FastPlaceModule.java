// ☭ Red Star Client — FastPlaceModule
package com.redstarclient.module.player;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;

/** Removes block placement delay. Applied via MinecraftClientMixin. */
public class FastPlaceModule extends Module {

    public FastPlaceModule() {
        super("FastPlace", "Removes the delay between block placements.", ModuleCategory.PLAYER, -1);
    }

    @Override public void renderImGuiConfig() {
        ImGui.textDisabled("Sets rightClickDelay to 0 every tick.");
        ImGui.textDisabled("Applied via Minecraft.tick() mixin.");
    }
}
