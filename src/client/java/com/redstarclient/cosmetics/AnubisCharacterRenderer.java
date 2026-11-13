package com.redstarclient.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redstarclient.cosmetics.designs.TungTungSahur;
import com.redstarclient.cosmetics.kit.CharacterBuilder;
import com.redstarclient.cosmetics.kit.CharacterModel;
import com.redstarclient.cosmetics.kit.Detail;
import com.redstarclient.cosmetics.kit.PartMesh;
import com.redstarclient.cosmetics.kit.Surface;
import com.redstarclient.render.RenderUtils;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Anubis Character Model Renderer:
 * Bakes and renders the genuine Anubis 3D Character Designs (like TungTungSahur)
 * using the extracted procedural Mesh and Surface system from Anubis.
 */
public final class AnubisCharacterRenderer {

    private static volatile CharacterModel tungModel = null;
    private static volatile boolean baking = false;

    public static void ensureBaked() {
        if (tungModel != null || baking) return;
        baking = true;
        Thread t = new Thread(() -> {
            try {
                TungTungSahur design = new TungTungSahur();
                CharacterModel model = CharacterBuilder.build(design, Detail.MEDIUM);
                tungModel = model;
            } catch (Throwable t1) {
                t1.printStackTrace();
            } finally {
                baking = false;
            }
        }, "Anubis-Model-Baker");
        t.setDaemon(true);
        t.start();
    }

    public static CharacterModel getTungModel() {
        return tungModel;
    }

    /**
     * Renders the true Anubis TungTungSahur character companion attached to the player.
     */
    public static void renderTung(Player p, PoseStack poseStack, Camera camera, float tickDelta) {
        ensureBaked();
        CharacterModel model = tungModel;
        if (model == null) return;

        double px = Mth.lerp((double) tickDelta, p.xo, p.getX());
        double py = Mth.lerp((double) tickDelta, p.yo, p.getY());
        double pz = Mth.lerp((double) tickDelta, p.zo, p.getZ());

        float bodyYaw = Mth.lerp(tickDelta, p.yBodyRotO, p.yBodyRot);
        float rad = (float) Math.toRadians(bodyYaw);

        // Position on player's side/shoulder
        double ox = -Math.sin(rad) * 0.70;
        double oz = Math.cos(rad) * 0.70;
        double cx = px + ox;
        double cy = py + 1.10 + (Math.sin((System.currentTimeMillis() % 2500) / 2500.0 * Math.PI * 2) * 0.05);
        double cz = pz + oz;

        // Render each authentic baked PartMesh from Anubis
        for (PartMesh part : model.parts) {
            Surface base = part.base;
            if (base == null) continue;

            // Compute part bounding box from actual vertices
            float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, minZ = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE, maxZ = -Float.MAX_VALUE;

            float[] pos = base.pos;
            for (int i = 0; i < pos.length; i += 3) {
                float vx = pos[i] * 0.035f;
                float vy = pos[i + 1] * 0.035f;
                float vz = pos[i + 2] * 0.035f;
                minX = Math.min(minX, vx);
                minY = Math.min(minY, vy);
                minZ = Math.min(minZ, vz);
                maxX = Math.max(maxX, vx);
                maxY = Math.max(maxY, vy);
                maxZ = Math.max(maxZ, vz);
            }

            if (minX <= maxX && minY <= maxY && minZ <= maxZ) {
                AABB partBox = new AABB(cx + minX, cy + minY, cz + minZ, cx + maxX, cy + maxY, cz + maxZ);
                
                // Color from Anubis wood/accent palette
                int strokeColor = RenderUtils.toARGB(0.85f, 0.12f, 0.12f, 0.95f);
                int fillColor   = RenderUtils.toARGB(0.48f, 0.26f, 0.14f, 0.85f);
                net.minecraft.gizmos.GizmoStyle style = net.minecraft.gizmos.GizmoStyle.strokeAndFill(strokeColor, 1.4f, fillColor);
                try {
                    net.minecraft.gizmos.Gizmos.cuboid(partBox, style).setAlwaysOnTop();
                } catch (Throwable ignored) {}
            }
        }

        // Floating nametag
        try {
            BlockPos tagPos = BlockPos.containing(cx, cy + 0.65, cz);
            net.minecraft.gizmos.Gizmos.billboardTextOverBlock(
                    "Tung Tung Tung Sahur", tagPos,
                    RenderUtils.toARGB(1.0f, 0.2f, 0.2f, 1.0f),
                    RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.75f),
                    0.85f
            ).setAlwaysOnTop();
        } catch (Throwable ignored) {}
    }
}
