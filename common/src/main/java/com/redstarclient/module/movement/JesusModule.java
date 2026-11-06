// ☭ Red Star Client — JesusModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import net.minecraft.client.Minecraft;

/**
 * Walk on water (and lava) surfaces.
 * The canStandOnFluid() mixin does the heavy lifting.
 */
public class JesusModule extends Module {

    public JesusModule() {
        super("Jesus", "Walk on water and lava surfaces.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        // Keep player above fluid surface
        if (mc.player.isInWater() || mc.player.isInLava()) {
            if (mc.player.getDeltaMovement().y < 0) {
                mc.player.setDeltaMovement(
                        mc.player.getDeltaMovement().x,
                        0.04,
                        mc.player.getDeltaMovement().z
                );
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textDisabled("Walk on water and lava surfaces.");
        ImGui.textDisabled("Works via canStandOnFluid() hook.");
    }
}
