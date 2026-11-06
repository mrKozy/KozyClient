// ☭ Red Star Client — AntiCobwebModule (from Wurst 7)
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
 * AntiCobweb (from Wurst 7):
 * Negates movement speed penalties when trapped inside cobwebs.
 */
public class AntiCobwebModule extends Module {

    private final ImFloat speedMultiplier = new ImFloat(3.2f);

    public AntiCobwebModule() {
        super("AntiCobweb", "Verhindert das Feststecken und die Verlangsamung in Spinnennetzen (Wurst 7).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        boolean inCobweb = mc.level.getBlockState(playerPos).is(Blocks.COBWEB) ||
                           mc.level.getBlockState(playerPos.above()).is(Blocks.COBWEB);

        if (inCobweb) {
            Vec3 vel = mc.player.getDeltaMovement();
            double factor = speedMultiplier.get();

            // Counteract cobweb slowdown
            mc.player.setDeltaMovement(vel.x * factor, vel.y * 1.5, vel.z * factor);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Geschwindigkeits-Kompensation##acob", speedMultiplier.getData(), 1.5f, 5.0f, "%.1fx");
        ImGui.spacing();
        ImGui.textDisabled("Laesst dich ungebremst durch gegnerische Spinnennetze und Fallen laufen.");
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
