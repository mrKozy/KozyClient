// ☭ Red Star Client — BowAimbotModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.MathUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;

/**
 * Auto-aims bow with real charge trajectory calculation, FOV cone targeting,
 * line-of-sight checks, and smooth humanized interpolation (no snapping).
 */
public class BowAimbotModule extends Module {

    private final ImFloat   aimSpeed    = new ImFloat(4.5f);
    private final ImFloat   fov         = new ImFloat(65.0f);
    private final ImFloat   range       = new ImFloat(45.0f);
    private final ImBoolean playersOnly = new ImBoolean(true);
    private final ImBoolean predict     = new ImBoolean(true);

    public BowAimbotModule() {
        super("BowAimbot", "Smoothly aims bow at targets within FOV with real trajectory physics.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.player.isUsingItem()) return;
        if (!(mc.player.getUseItem().getItem() instanceof BowItem)) return;

        // Calculate real arrow velocity from current draw time (0.0 to 1.0 charge, max 3.0 speed)
        int useTicks = mc.player.getTicksUsingItem();
        float charge = BowItem.getPowerForTime(useTicks);
        if (charge < 0.15f) return; // Don't snap before bow begins pulling
        float velocity = charge * 3.0f;

        // Target acquisition inside FOV cone
        Entity bestTarget = null;
        double bestAngle = Double.MAX_VALUE;
        float maxFovHalf = fov.get() / 2.0f;
        float maxRange = range.get();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le)) continue;
            if (!le.isAlive() || le.isDeadOrDying()) continue;

            if (playersOnly.get()) {
                if (!(e instanceof Player)) continue;
            } else {
                if (!(e instanceof Player) && !(e instanceof Monster)) continue;
            }

            if (mc.player.distanceTo(e) > maxRange) continue;
            if (!PlayerUtils.canSeeEntity(mc.player, e)) continue;

            float[] rots = PlayerUtils.getRotationsToEntity(mc.player, e);
            float dYaw = Math.abs(MathUtils.wrapDegrees(rots[0] - mc.player.getYRot()));
            float dPitch = Math.abs(rots[1] - mc.player.getXRot());
            double angle = Math.sqrt(dYaw * dYaw + dPitch * dPitch);

            if (angle <= maxFovHalf && angle < bestAngle) {
                bestAngle = angle;
                bestTarget = e;
            }
        }

        if (bestTarget == null) return;

        // Calculate trajectory compensation
        double distance = PlayerUtils.getDistanceTo(mc.player, bestTarget);
        float[] baseRots = PlayerUtils.getRotationsToEntity(mc.player, bestTarget);

        // Motion prediction: adjust position based on target delta movement
        float targetYaw = baseRots[0];
        if (predict.get() && bestTarget.getDeltaMovement().lengthSqr() > 0.001) {
            double timeInFlight = distance / velocity;
            double predX = bestTarget.getX() + bestTarget.getDeltaMovement().x * timeInFlight;
            double predZ = bestTarget.getZ() + bestTarget.getDeltaMovement().z * timeInFlight;
            double dx = predX - mc.player.getX();
            double dz = predZ - mc.player.getZ();
            targetYaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        }

        float[] traj = MathUtils.calculateBowTrajectory(distance, 0.05f, velocity);
        float targetPitch = baseRots[1] + traj[1];

        // Smooth humanized interpolation (no violent snaps)
        float factor = Math.min(1.0f, aimSpeed.get() * 0.045f);
        float smoothYaw = MathUtils.lerpAngle(mc.player.getYRot(), targetYaw, factor);
        float smoothPitch = MathUtils.lerpAngle(mc.player.getXRot(), targetPitch, factor);

        PlayerUtils.setRotation(mc.player, smoothYaw, Math.max(-90f, Math.min(90f, smoothPitch)));
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Aim Speed##ba", aimSpeed.getData(), 1.0f, 10.0f, "%.1fx");
        ImGui.sliderFloat("FOV Angle##ba", fov.getData(), 20.0f, 120.0f, "%.0f°");
        ImGui.sliderFloat("Max Range##ba", range.getData(), 10.0f, 64.0f, "%.0fm");
        ImGui.checkbox("Players Only##ba", playersOnly);
        ImGui.checkbox("Predict Motion##ba", predict);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("aimSpeed", aimSpeed.get());
        cfg.putSetting("fov", fov.get());
        cfg.putSetting("range", range.get());
        cfg.putSetting("playersOnly", playersOnly.get());
        cfg.putSetting("predict", predict.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        aimSpeed.set(cfg.getSetting("aimSpeed", aimSpeed.get()));
        fov.set(cfg.getSetting("fov", fov.get()));
        range.set(cfg.getSetting("range", range.get()));
        playersOnly.set(cfg.getSetting("playersOnly", playersOnly.get()));
        predict.set(cfg.getSetting("predict", predict.get()));
    }
}
