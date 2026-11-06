// ☭ Red Star Client — SpiderModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;

/** Climb any wall surface by applying upward velocity on horizontal collision. */
public class SpiderModule extends Module {

    private final ImFloat climbSpeed = new ImFloat(0.2f);

    public SpiderModule() {
        super("Spider", "Climb any wall like a spider.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        if (mc.player.horizontalCollision && !mc.player.onGround()) {
            mc.player.setDeltaMovement(
                    mc.player.getDeltaMovement().x,
                    climbSpeed.get(),
                    mc.player.getDeltaMovement().z
            );
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Climb Speed##spi", climbSpeed.getData(), 0.1f, 0.4f);
    }
}
