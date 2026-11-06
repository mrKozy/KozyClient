// ☭ Red Star Client — MathUtils
package com.redstarclient.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Mathematical helpers: trajectories, interpolation, and angle wrapping.
 */
public final class MathUtils {

    private MathUtils() {}

    /**
     * Calculates the pitch adjustment (degrees) needed to hit a target at the given
     * horizontal distance with a bow, accounting for arrow gravity.
     *
     * @param distance horizontal distance to target (blocks)
     * @param gravity  arrow gravity (default 0.05)
     * @param velocity initial arrow velocity (default 3.0)
     * @return {yaw_offset, pitch} — yaw offset is always 0 (caller applies its own yaw)
     */
    public static float[] calculateBowTrajectory(double distance, float gravity, float velocity) {
        // Simplified arc calculation: compensate for drop
        double drop  = 0.5 * gravity * (distance / velocity) * (distance / velocity);
        double angle = Math.toDegrees(Math.atan2(drop, distance));
        float  pitch = (float)(-angle); // negative = looking up
        return new float[]{ 0f, pitch };
    }

    /** Linear interpolation between two doubles. */
    public static double interpolate(double prev, double current, float tickDelta) {
        return prev + (current - prev) * tickDelta;
    }

    /** Wraps an angle in degrees to the range -180..180. */
    public static float wrapDegrees(float degrees) {
        degrees %= 360f;
        if (degrees >= 180f)  degrees -= 360f;
        if (degrees < -180f)  degrees += 360f;
        return degrees;
    }

    /** Smooth angular interpolation (handles wraparound). */
    public static float lerpAngle(float from, float to, float factor) {
        float diff = wrapDegrees(to - from);
        return from + diff * factor;
    }

    /** Returns the interpolated render position of an entity at the given tick delta. */
    public static Vec3 interpolateEntityPos(Entity entity, float tickDelta) {
        return entity.getPosition(tickDelta);
    }
}
