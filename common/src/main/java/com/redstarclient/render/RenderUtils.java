// ☭ Red Star Client — RenderUtils
package com.redstarclient.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Shared rendering helpers for ESP boxes, lines, HUD text and health bars.
 * All 3D helpers use RenderType.lines() with depth testing disabled so outlines
 * and boxes render clearly through walls.
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

    // ── Dedicated Line Overlay Pipeline ──────────────────────────────────────

    private static final ByteBufferBuilder BYTE_BUILDER = new ByteBufferBuilder(65536);
    private static final MultiBufferSource.BufferSource BUFFER_SOURCE = MultiBufferSource.immediate(BYTE_BUILDER);

    public static final RenderType OVERLAY_LINES = new RenderType(
        "redstar_lines",
        DefaultVertexFormat.POSITION_COLOR_NORMAL,
        VertexFormat.Mode.LINES,
        1536,
        false,
        false,
        () -> {
            RenderType.lines().setupRenderState();
            RenderSystem.disableDepthTest();
        },
        () -> {
            RenderType.lines().clearRenderState();
            RenderSystem.enableDepthTest();
        }
    ) {};

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
    public static void drawTextWithBackground(GuiGraphics graphics, String text,
                                              int x, int y, int textColor, int bgColor) {
        Font font = Minecraft.getInstance().font;
        int w = font.width(text);
        graphics.fill(x - 2, y - 2, x + w + 2, y + font.lineHeight + 2, bgColor);
        graphics.drawString(font, text, x, y, textColor, false);
    }

    /** Draws a horizontal health bar (green → yellow → red). */
    public static void drawHealthBar(GuiGraphics graphics,
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
        Vec3 cam = camera.getPosition();

        float a = ((argbColor >> 24) & 0xFF) / 255.0f;
        float r = ((argbColor >> 16) & 0xFF) / 255.0f;
        float g = ((argbColor >>  8) & 0xFF) / 255.0f;
        float b = ((argbColor)       & 0xFF) / 255.0f;

        VertexConsumer vc = BUFFER_SOURCE.getBuffer(OVERLAY_LINES);
        ShapeRenderer.renderLineBox(poseStack, vc, box.move(-cam.x, -cam.y, -cam.z), r, g, b, a);
        BUFFER_SOURCE.endBatch(OVERLAY_LINES);
    }

    /**
     * Draw a 3D line between two world positions (through walls).
     */
    public static void drawLine3D(PoseStack poseStack, Camera camera,
                                   Vec3 from, Vec3 to, int argbColor) {
        Vec3 cam = camera.getPosition();

        float a = ((argbColor >> 24) & 0xFF) / 255.0f;
        float r = ((argbColor >> 16) & 0xFF) / 255.0f;
        float g = ((argbColor >>  8) & 0xFF) / 255.0f;
        float b = ((argbColor)       & 0xFF) / 255.0f;

        float x1 = (float)(from.x - cam.x);
        float y1 = (float)(from.y - cam.y);
        float z1 = (float)(from.z - cam.z);
        float x2 = (float)(to.x   - cam.x);
        float y2 = (float)(to.y   - cam.y);
        float z2 = (float)(to.z   - cam.z);

        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float nx = len > 0 ? dx / len : 1;
        float ny = len > 0 ? dy / len : 0;
        float nz = len > 0 ? dz / len : 0;

        VertexConsumer vc = BUFFER_SOURCE.getBuffer(OVERLAY_LINES);
        PoseStack.Pose pose = poseStack.last();
        vc.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        vc.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        BUFFER_SOURCE.endBatch(OVERLAY_LINES);
    }

    /**
     * Draws a 2D billboard bounding box rectangle around the entity facing the camera (through walls).
     */
    public static void drawBox2D(PoseStack poseStack, Camera camera, Entity entity, int argbColor, float tickDelta) {
        Vec3 cam = camera.getPosition();
        Vec3 pos = entity.getPosition(tickDelta).subtract(cam);
        float hw = entity.getBbWidth() / 2.0f;
        float h  = entity.getBbHeight();

        float a = ((argbColor >> 24) & 0xFF) / 255.0f;
        float r = ((argbColor >> 16) & 0xFF) / 255.0f;
        float g = ((argbColor >>  8) & 0xFF) / 255.0f;
        float b = ((argbColor)       & 0xFF) / 255.0f;

        poseStack.pushPose();
        poseStack.translate(pos.x, pos.y, pos.z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));

        VertexConsumer vc = BUFFER_SOURCE.getBuffer(OVERLAY_LINES);
        PoseStack.Pose pose = poseStack.last();

        // 4 lines of the billboard rectangle
        // Bottom: (-hw, 0, 0) -> (hw, 0, 0)
        vc.addVertex(pose, -hw, 0, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
        vc.addVertex(pose,  hw, 0, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
        // Top: (-hw, h, 0) -> (hw, h, 0)
        vc.addVertex(pose, -hw, h, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
        vc.addVertex(pose,  hw, h, 0).setColor(r, g, b, a).setNormal(pose, 1, 0, 0);
        // Left: (-hw, 0, 0) -> (-hw, h, 0)
        vc.addVertex(pose, -hw, 0, 0).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        vc.addVertex(pose, -hw, h, 0).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        // Right: (hw, 0, 0) -> (hw, h, 0)
        vc.addVertex(pose, hw, 0, 0).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);
        vc.addVertex(pose, hw, h, 0).setColor(r, g, b, a).setNormal(pose, 0, 1, 0);

        BUFFER_SOURCE.endBatch(OVERLAY_LINES);
        poseStack.popPose();
    }

    // ── Legacy stubs (kept for binary compat) ────────────────────────────────

    /** @deprecated Use {@link #drawBox3D(PoseStack, Camera, AABB, int)} */
    @Deprecated public static void drawBox3D(GuiGraphics g, AABB box, int color, float td) {}
    /** @deprecated Use {@link #drawLine3D(PoseStack, Camera, Vec3, Vec3, int)} */
    @Deprecated public static void drawLine3D(GuiGraphics g, Vec3 from, Vec3 to, int color, float td) {}
    /** @deprecated Use {@link #drawBox2D(PoseStack, Camera, Entity, int, float)} */
    @Deprecated public static void drawBox2D(GuiGraphics g, Entity e, int color, float td) {}
}
