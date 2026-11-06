// ☭ Red Star Client — RenderUtils
package com.redstarclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared rendering helpers for ESP boxes, lines, HUD text and health bars.
 */
public final class RenderUtils {

    // ── Color constants ───────────────────────────────────────────────────────

    public static final int SOVIET_RED    = toARGB(0.72f, 0.05f, 0.05f, 1.0f);
    public static final int SOVIET_GOLD   = toARGB(0.92f, 0.76f, 0.13f, 1.0f);
    public static final int HOSTILE_RED   = toARGB(1.0f,  0.2f,  0.2f,  1.0f);
    public static final int PASSIVE_GREEN = toARGB(0.2f,  1.0f,  0.2f,  1.0f);
    public static final int PLAYER_GOLD   = toARGB(1.0f,  0.85f, 0.0f,  1.0f);
    public static final int WHITE         = toARGB(1.0f,  1.0f,  1.0f,  1.0f);
    public static final int BG_DARK_RED   = toARGB(0.07f, 0.02f, 0.02f, 0.80f);

    private RenderUtils() {}

    // ── Color conversion ─────────────────────────────────────────────────────

    /** Pack RGBA floats (0–1) into a packed ARGB int. */
    public static int toARGB(float r, float g, float b, float a) {
        int ai = (int)(a * 255) & 0xFF;
        int ri = (int)(r * 255) & 0xFF;
        int gi = (int)(g * 255) & 0xFF;
        int bi = (int)(b * 255) & 0xFF;
        return (ai << 24) | (ri << 16) | (gi << 8) | bi;
    }

    // ── HUD text rendering ───────────────────────────────────────────────────

    /** Draws text with a semi-transparent background box behind it. */
    public static void drawTextWithBackground(GuiGraphicsExtractor graphics, String text,
                                              int x, int y, int textColor, int bgColor) {
        Font font = Minecraft.getInstance().font;
        int w = font.width(text);
        graphics.fill(x - 2, y - 2, x + w + 2, y + font.lineHeight + 2, bgColor);
        graphics.text(font, text, x, y, textColor, false);
    }

    /** Draws a horizontal health bar (green → yellow → red). */
    public static void drawHealthBar(GuiGraphicsExtractor graphics,
                                     int x, int y, int width,
                                     float health, float maxHealth) {
        float ratio = maxHealth > 0 ? Math.min(health / maxHealth, 1f) : 0f;
        graphics.fill(x, y, x + width, y + 4, toARGB(0.1f, 0.1f, 0.1f, 0.8f));
        int r = (int)(255 * (1f - ratio));
        int g = (int)(255 * ratio);
        int color = (0xFF << 24) | (r << 16) | (g << 8);
        graphics.fill(x, y, x + (int)(width * ratio), y + 4, color);
        // Border
        graphics.fill(x,             y,     x + width, y + 1,     WHITE);
        graphics.fill(x,             y + 3, x + width, y + 4,     WHITE);
        graphics.fill(x,             y,     x + 1,     y + 4,     WHITE);
        graphics.fill(x + width - 1, y,     x + width, y + 4,     WHITE);
    }

    // ── 3D World-space rendering (through walls) ─────────────────────────────

    /**
     * Draw a wireframe AABB box in world space using RenderType.lines() with depth test disabled.
     *
     * @param poseStack  From the world render context
     * @param camera     Active camera (to convert world → camera-relative coords)
     * @param box        World-space bounding box
     * @param argbColor  Packed ARGB colour
     */
    public static void drawBox3D(PoseStack poseStack, Camera camera, AABB box, int argbColor) {
        drawBox3D(poseStack, camera, box, argbColor, 2.0f, 0.20f);
    }

    public static void drawBox3D(PoseStack poseStack, Camera camera, AABB box, int argbColor, float strokeWidth, float fillOpacity) {
        if (box == null) return;
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;

        try {
            int fillAlpha = Math.min(255, Math.max(0, (int) (fillOpacity * 255.0f)));
            int fillColor = (fillAlpha << 24) | (argbColor & 0x00FFFFFF);

            net.minecraft.gizmos.GizmoStyle style = (fillAlpha > 0)
                    ? net.minecraft.gizmos.GizmoStyle.strokeAndFill(argbColor, strokeWidth, fillColor)
                    : net.minecraft.gizmos.GizmoStyle.stroke(argbColor, strokeWidth);
            net.minecraft.gizmos.Gizmos.cuboid(box, style).setAlwaysOnTop();
        } catch (Throwable ignored) {}
    }

    /**
     * Draw a 3D line between two world positions (through walls).
     */
    public static void drawLine3D(PoseStack poseStack, Camera camera,
                                   Vec3 from, Vec3 to, int argbColor) {
        if (from == null || to == null) return;
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;

        try {
            net.minecraft.gizmos.Gizmos.line(from, to, argbColor, 2.0f).setAlwaysOnTop();
        } catch (Throwable ignored) {}
    }

    /**
     * Draws a 2D billboard bounding box rectangle around the entity facing the camera (through walls).
     */
    public static void drawBox2D(PoseStack poseStack, Camera camera, Entity entity, int argbColor, float tickDelta) {
        if (entity == null) return;
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;

        drawBox3D(poseStack, camera, entity.getBoundingBox(), argbColor);
    }

    // ── Legacy stubs (kept for binary compat) ────────────────────────────────

    /** @deprecated Use {@link #drawBox3D(PoseStack, Camera, AABB, int)} */
    @Deprecated public static void drawBox3D(GuiGraphicsExtractor g, AABB box, int color, float td) {}
    /** @deprecated Use {@link #drawLine3D(PoseStack, Camera, Vec3, Vec3, int)} */
    @Deprecated public static void drawLine3D(GuiGraphicsExtractor g, Vec3 from, Vec3 to, int color, float td) {}
    /** @deprecated Use {@link #drawBox2D(PoseStack, Camera, Entity, int, float)} */
    @Deprecated public static void drawBox2D(GuiGraphicsExtractor g, Entity e, int color, float td) {}
}
