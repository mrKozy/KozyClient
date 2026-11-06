// ☭ Red Star Client — AutoFishModule
package com.redstarclient.module.player;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.FishingHook;

/** Automatically reels in and recasts the fishing rod when a bite is detected. */
public class AutoFishModule extends Module {

    private final ImInt recastDelayMs = new ImInt(300);
    private long lastCatchTime = 0L;
    private boolean awaitingRecast = false;

    public AutoFishModule() {
        super("AutoFish", "Automatically catches fish and recasts rod.", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        FishingHook hook = mc.player.fishing;
        long now = System.currentTimeMillis();

        if (awaitingRecast) {
            if (now - lastCatchTime >= recastDelayMs.get()) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                awaitingRecast = false;
            }
            return;
        }

        if (hook != null) {
            // Check if fish bit (bobber pulled down significantly)
            if (hook.getDeltaMovement().y < -0.05 && Math.abs(hook.getDeltaMovement().x) < 0.01) {
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                awaitingRecast = true;
                lastCatchTime = now;
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Recast Delay (ms)##af", recastDelayMs.getData(), 100, 1000);
        ImGui.textDisabled("Hold a fishing rod in main hand.");
    }
}
