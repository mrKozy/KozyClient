// ☭ Red Star Client — AntiCobwebModule
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * AntiCobweb:
 * Negates movement speed penalties and stuck effects when trapped inside cobwebs.
 * Works via WebBlockMixin cancelling entityInside() and clearing stuckSpeedMultiplier.
 */
public class AntiCobwebModule extends Module {

    private final ImFloat speedMultiplier = new ImFloat(1.0f);

    public AntiCobwebModule() {
        super("AntiCobweb", "Verhindert Feststecken und Verlangsamung in Spinnennetzen.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || !isEnabled()) return;

        // Clear any active stuck multiplier
        if (mc.player.stuckSpeedMultiplier != Vec3.ZERO) {
            mc.player.stuckSpeedMultiplier = Vec3.ZERO;
        }

        BlockPos playerPos = mc.player.blockPosition();
        boolean inCobweb = mc.level.getBlockState(playerPos).is(Blocks.COBWEB) ||
                           mc.level.getBlockState(playerPos.above()).is(Blocks.COBWEB);

        if (inCobweb && speedMultiplier.get() > 1.0f) {
            Vec3 vel = mc.player.getDeltaMovement();
            double factor = speedMultiplier.get();
            mc.player.setDeltaMovement(vel.x * factor, vel.y, vel.z * factor);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Speed Boost##acob", speedMultiplier.getData(), 1.0f, 3.0f, "%.1fx");
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "WebBlock collision bypass active (walk through cobwebs like air).");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("speedMultiplier", speedMultiplier.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        speedMultiplier.set(cfg.getSetting("speedMultiplier", speedMultiplier.get()));
    }
}
