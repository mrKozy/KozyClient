// ☭ Red Star Client — ParkourModule (from Wurst 7)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/**
 * Parkour Module (from Wurst 7):
 * Automatically jumps at the exact edge of a block so you never miss a jump.
 */
public class ParkourModule extends Module {

    private final ImBoolean onlyForward = new ImBoolean(true);

    public ParkourModule() {
        super("Parkour", "Springt automatisch an der Block-Kante fuer fehlerfreie Parkour-Spruenge (Wurst 7).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        // Player must be on the ground, not sneaking, and moving
        if (!mc.player.onGround() || mc.player.isShiftKeyDown()) return;

        if (onlyForward.get() && !mc.options.keyUp.isDown()) return;

        Vec3 velocity = mc.player.getDeltaMovement();
        double speed = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
        if (speed < 0.05) return;

        // Predict position 0.35 blocks ahead in direction of motion
        double lookAhead = 0.35;
        double nextX = mc.player.getX() + (velocity.x / speed) * lookAhead;
        double nextZ = mc.player.getZ() + (velocity.z / speed) * lookAhead;

        BlockPos blockBelowAhead = BlockPos.containing(nextX, mc.player.getY() - 0.5, nextZ);

        if (mc.level.getBlockState(blockBelowAhead).isAir()) {
            // Edge reached, initiate jump!
            mc.player.jumpFromGround();
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Nur beim Vorwaertslaufen springen##pk", onlyForward);
        ImGui.spacing();
        ImGui.textDisabled("Perfekt fuer Parkour-Maps, Bedwars-Rushes und Sprunghilfen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("onlyForward", onlyForward.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        onlyForward.set(cfg.getSetting("onlyForward", onlyForward.get()));
    }
}
