// ☭ Red Star Client — StepModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;

/** Increases the step height so the player can walk up blocks without jumping. */
public class StepModule extends Module {

    private static final float VANILLA_STEP = 0.6f;
    private final ImFloat stepHeight = new ImFloat(1.0f);

    public StepModule() {
        super("Step", "Walk up full blocks without jumping.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        // NOTE: maxUpStep field removed in MC 1.21.4 - this module is currently non-functional
        // TODO: Use mixins or alternative approach to modify step height
    }

    @Override
    public void onDisable() {
        // NOTE: maxUpStep field removed in MC 1.21.4
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Step Height##step", stepHeight.getData(), 0.6f, 2.5f);
    }
}
