// ☭ Red Star Client — BunnyHopModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;

/** Automatically chains jumps on landing while preserving and boosting momentum. */
public class BunnyHopModule extends Module {

    private final ImFloat airBoost = new ImFloat(1.05f);

    public BunnyHopModule() {
        super("BunnyHop", "Continuously jumps upon landing to build momentum.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        boolean moving = mc.options.keyUp.isDown() || mc.options.keyDown.isDown() ||
                         mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();

        if (moving && mc.player.onGround() && !mc.player.isShiftKeyDown() && !mc.player.isInWater()) {
            mc.player.jumpFromGround();
            if (airBoost.get() > 1.0f) {
                var vel = mc.player.getDeltaMovement();
                mc.player.setDeltaMovement(vel.x * airBoost.get(), vel.y, vel.z * airBoost.get());
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Air Boost##bhop", airBoost.getData(), 1.0f, 1.3f);
        ImGui.textDisabled("Auto-jumps on ground contact.");
    }
}
