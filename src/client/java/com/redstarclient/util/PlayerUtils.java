// ☭ Red Star Client — PlayerUtils
package com.redstarclient.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Player rotation, targeting, and distance utilities.
 */
public final class PlayerUtils {

    private PlayerUtils() {}

    /** Returns {yaw, pitch} rotations to face the given target entity. */
    public static float[] getRotationsToEntity(Entity from, Entity target) {
        Vec3 fromPos   = from.getEyePosition(1f);
        Vec3 targetPos = target.getBoundingBox().getCenter();

        double dx = targetPos.x - fromPos.x;
        double dy = targetPos.y - fromPos.y;
        double dz = targetPos.z - fromPos.z;

        double dist2D = Math.sqrt(dx * dx + dz * dz);
        float  yaw    = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float  pitch  = (float)(-Math.toDegrees(Math.atan2(dy, dist2D)));

        return new float[]{ yaw, pitch };
    }

    /** Euclidean distance between two entities. */
    public static double getDistanceTo(Entity a, Entity b) {
        return a.distanceTo(b);
    }

    /**
     * Finds the closest living entity (or player) within range.
     *
     * @param playersOnly when true, only returns Player entities
     */
    public static Entity getClosestEntity(LocalPlayer player, double range, boolean playersOnly) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        Entity best = null;
        double bestDist = range * range;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == player) continue;
            if (!(e instanceof LivingEntity le)) continue;
            if (!le.isAlive()) continue;
            if (playersOnly && !(e instanceof net.minecraft.world.entity.player.Player)) continue;
            if (com.redstarclient.module.combat.TeamCheckModule.isTeammate(e)) continue;

            double dist = e.distanceToSqr(player);
            if (dist < bestDist) {
                bestDist = dist;
                best = e;
            }
        }
        return best;
    }

    /** Returns the entity currently under the crosshair within range, or null. */
    public static Entity getCrosshairTarget(Minecraft mc, double range) {
        if (mc.hitResult == null) return null;
        if (mc.hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult ehr = (EntityHitResult) mc.hitResult;
            Entity ent = ehr.getEntity();
            if (com.redstarclient.module.combat.TeamCheckModule.isTeammate(ent)) return null;
            return ent;
        }
        return null;
    }

    /** True if there is a clear line-of-sight between player eyes and target. */
    public static boolean canSeeEntity(LocalPlayer player, Entity target) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        Vec3 eyes   = player.getEyePosition(1f);
        Vec3 center = target.getBoundingBox().getCenter();

        HitResult result = mc.level.clip(new ClipContext(
                eyes, center,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        return result.getType() == HitResult.Type.MISS;
    }

    /** Returns entity health if it is a LivingEntity, otherwise 0. */
    public static float getHealth(Entity e) {
        return e instanceof LivingEntity le ? le.getHealth() : 0f;
    }

    /** True if the entity is a living entity that is not the local player and not a teammate. */
    public static boolean isEnemy(Entity e, LocalPlayer player) {
        if (e == player || !(e instanceof LivingEntity)) return false;
        if (com.redstarclient.module.combat.TeamCheckModule.isTeammate(e)) return false;
        return true;
    }

    /** Smoothly set the player's rotation toward a target yaw/pitch. */
    public static void setRotation(LocalPlayer player, float yaw, float pitch) {
        player.setYRot(yaw);
        player.setXRot(pitch);
    }

    /** Returns {yaw, pitch} rotations from eye position to target vector. */
    public static float[] getRotationsToVec(Vec3 fromPos, Vec3 targetPos) {
        double dx = targetPos.x - fromPos.x;
        double dy = targetPos.y - fromPos.y;
        double dz = targetPos.z - fromPos.z;

        double dist2D = Math.sqrt(dx * dx + dz * dz);
        float  yaw    = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float  pitch  = (float)(-Math.toDegrees(Math.atan2(dy, dist2D)));

        return new float[]{ yaw, pitch };
    }

    /** True if there is a clear line-of-sight between player eyes and target vector. */
    public static boolean canSeeVec(LocalPlayer player, Vec3 targetVec) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return false;

        Vec3 eyes = player.getEyePosition(1f);
        HitResult result = mc.level.clip(new ClipContext(
                eyes, targetVec,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        return result.getType() == HitResult.Type.MISS;
    }

    /** Sends a silent server rotation packet without affecting client camera. */
    public static void sendSilentRotation(Minecraft mc, float yaw, float pitch) {
        if (mc.getConnection() != null && mc.player != null) {
            mc.getConnection().send(new net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Rot(
                    yaw, pitch, mc.player.onGround(), mc.player.horizontalCollision
            ));
        }
    }
}
