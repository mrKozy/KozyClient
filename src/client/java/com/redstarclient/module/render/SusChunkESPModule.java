// Red Star Client — SusChunkESPModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Sus Chunk ESP (Base & Vault Cluster Finder):
 * Scans loaded chunks for high concentrations of tile entities (chests, hoppers,
 * shulker boxes, spawners, beacons) indicating hidden player bases and secret vaults.
 */
public class SusChunkESPModule extends Module {

    private final ImInt     scanRadius     = new ImInt(6);
    private final ImInt     thresholdScore = new ImInt(12);
    private final ImBoolean tightBox       = new ImBoolean(true);
    private final ImBoolean drawTracers    = new ImBoolean(true);
    private final ImBoolean drawStats      = new ImBoolean(true);

    public SusChunkESPModule() {
        super("SusChunkESP", "Erkennt verdächtige Chunk-Cluster mit vielen Kisten, Hoppern & Spawnern (Basen-Finder).", ModuleCategory.RENDER, -1);
    }

    private static class ChunkReport {
        int chests = 0;
        int enderChests = 0;
        int shulkers = 0;
        int furnaces = 0;
        int hoppers = 0;
        int spawners = 0;
        int beacons = 0;
        int dispensers = 0;

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        void includePos(BlockPos p) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());
        }

        int calculateScore() {
            return (chests * 2) + (enderChests * 3) + (shulkers * 8) +
                   (furnaces * 2) + (hoppers * 3) + (spawners * 12) +
                   (beacons * 15) + (dispensers * 1);
        }

        int totalContainers() {
            return chests + enderChests + shulkers + hoppers + furnaces;
        }
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        int rad = Math.min(scanRadius.get(), 10);

        ChunkReport bestReport = null;
        Vec3 bestCenter = null;
        int highestScore = 0;

        for (int cx = pcx - rad; cx <= pcx + rad; cx++) {
            for (int cz = pcz - rad; cz <= pcz + rad; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                ChunkReport rep = new ChunkReport();

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.isRemoved()) continue;
                    BlockPos p = be.getBlockPos();

                    if (be instanceof ChestBlockEntity) {
                        rep.chests++;
                        rep.includePos(p);
                    } else if (be instanceof EnderChestBlockEntity) {
                        rep.enderChests++;
                        rep.includePos(p);
                    } else if (be instanceof ShulkerBoxBlockEntity) {
                        rep.shulkers++;
                        rep.includePos(p);
                    } else if (be instanceof AbstractFurnaceBlockEntity) {
                        rep.furnaces++;
                        rep.includePos(p);
                    } else if (be instanceof HopperBlockEntity) {
                        rep.hoppers++;
                        rep.includePos(p);
                    } else if (be instanceof SpawnerBlockEntity) {
                        rep.spawners++;
                        rep.includePos(p);
                    } else if (be instanceof BeaconBlockEntity) {
                        rep.beacons++;
                        rep.includePos(p);
                    } else if (be instanceof DispenserBlockEntity) {
                        rep.dispensers++;
                        rep.includePos(p);
                    }
                }

                int score = rep.calculateScore();
                if (score >= thresholdScore.get() && rep.totalContainers() > 0) {
                    AABB box;
                    if (tightBox.get() && rep.minX <= rep.maxX) {
                        // Tight box around the clustered storage room
                        box = new AABB(rep.minX - 0.2, rep.minY - 0.2, rep.minZ - 0.2,
                                       rep.maxX + 1.2, rep.maxY + 1.2, rep.maxZ + 1.2);
                    } else {
                        // Whole chunk column
                        int y0 = Math.max(-64, chunk.getMinY());
                        int y1 = Math.min(320, chunk.getMaxY());
                        box = new AABB(cx * 16.0, y0, cz * 16.0,
                                       cx * 16.0 + 16.0, y1, cz * 16.0 + 16.0);
                    }

                    // Color gradient based on score
                    int color;
                    if (score < 25) {
                        color = RenderUtils.toARGB(1.0f, 0.85f, 0.1f, 1.0f); // Gold
                    } else if (score < 60) {
                        color = RenderUtils.toARGB(1.0f, 0.45f, 0.05f, 1.0f); // Vivid Orange
                    } else {
                        color = RenderUtils.toARGB(1.0f, 0.1f, 0.35f, 1.0f); // Neon Red/Magenta (Mega Base)
                    }

                    RenderUtils.drawBox3D(poseStack, camera, box, color, 2.0f, 0.15f);

                    Vec3 center = box.getCenter();
                    if (score > highestScore) {
                        highestScore = score;
                        bestReport = rep;
                        bestCenter = center;
                    }

                    if (drawStats.get()) {
                        String stats = String.format("SUS BASE [Score: %d] (%d Kisten, %d Hopper, %d Spawner)",
                                score, rep.chests + rep.shulkers, rep.hoppers, rep.spawners);
                        try {
                            BlockPos bpos = BlockPos.containing(center.x, box.maxY + 0.5, center.z);
                            net.minecraft.gizmos.Gizmos.billboardTextOverBlock(stats, bpos, color, RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.75f), 1.0f)
                                    .setAlwaysOnTop();
                        } catch (Throwable ignored) {}
                    }
                }
            }
        }

        // Draw tracer to the highest-scoring base
        if (drawTracers.get() && bestCenter != null) {
            Vec3 eye = mc.player.getEyePosition(tickDelta);
            net.minecraft.gizmos.Gizmos.line(eye, bestCenter, RenderUtils.toARGB(1.0f, 0.2f, 0.2f, 1.0f), 2.0f)
                    .setAlwaysOnTop();
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Such-Radius (Chunks)##sus", scanRadius.getData(), 2, 12);
        ImGui.sliderInt("Verdachts-Schwelle (Score)##sus", thresholdScore.getData(), 5, 50);
        ImGui.checkbox("Exakte Raum-Box (Tight Box)##sus", tightBox);
        ImGui.checkbox("Tracer-Linie zur größten Basis##sus", drawTracers);
        ImGui.checkbox("Cluster-Statistik als 3D-Text##sus", drawStats);
        ImGui.spacing();
        ImGui.textDisabled("Bewertet Shulker (8), Spawner (12), Beacons (15), Hopper (3) und Kisten (2).");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("scanRadius", scanRadius.get());
        cfg.putSetting("thresholdScore", thresholdScore.get());
        cfg.putSetting("tightBox", tightBox.get());
        cfg.putSetting("drawTracers", drawTracers.get());
        cfg.putSetting("drawStats", drawStats.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        scanRadius.set(cfg.getSetting("scanRadius", scanRadius.get()));
        thresholdScore.set(cfg.getSetting("thresholdScore", thresholdScore.get()));
        tightBox.set(cfg.getSetting("tightBox", tightBox.get()));
        drawTracers.set(cfg.getSetting("drawTracers", drawTracers.get()));
        drawStats.set(cfg.getSetting("drawStats", drawStats.get()));
    }
}
