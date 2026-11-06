// ☭ Red Star Client — ESPRenderer
package com.redstarclient.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * ESP-specific rendering utilities, dispatching based on the selected mode.
 */
public final class ESPRenderer {

    public enum ESPMode {
        BOX_2D("2D Box"),
        BOX_3D("3D Box"),
        GLOW  ("Glow");

        public final String label;
        ESPMode(String label) { this.label = label; }
    }

    private ESPRenderer() {}

    /**
     * Render an entity in the given ESP mode using world-space PoseStack + Camera.
     */
    public static void renderEntity3D(PoseStack poseStack, Camera camera,
                                      Entity entity, ESPMode mode, int color, float tickDelta) {
        switch (mode) {
            case BOX_2D -> RenderUtils.drawBox2D(poseStack, camera, entity, color, tickDelta);
            case BOX_3D -> RenderUtils.drawBox3D(poseStack, camera, entity.getBoundingBox(), color);
            case GLOW   -> applyGlow(entity);
        }
    }

    /**
     * Returns a color based on entity type:
     * hostile → red, passive → green, player → gold.
     */
    public static int getEntityColor(Entity entity) {
        if (entity instanceof Player)  return RenderUtils.PLAYER_GOLD;
        if (entity instanceof Monster) return RenderUtils.HOSTILE_RED;
        if (entity instanceof Animal)  return RenderUtils.PASSIVE_GREEN;
        return RenderUtils.SOVIET_RED;
    }

    private static void applyGlow(Entity entity) {
        entity.setGlowingTag(true);
    }
}
