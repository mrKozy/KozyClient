// KozyClient — SusChunkESPModule
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
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Sus Chunk ESP (Base & Vault Cluster Finder):
 * Scans loaded chunks down to bedrock for high concentrations of containers & tile entities
 * (chests, barrels, hoppers, shulkers, spawners, beacons, crafters) indicating hidden
 * underground player bases and secret stashes. Highlights chunks and stash rooms through walls.
 */
public class SusChunkESPModule extends Module {

    private final ImInt     scanRadius     = new ImInt(8);
    private final ImInt     thresholdScore = new ImInt(6); // Accessible threshold for smaller bases
    private final ImBoolean tightBox       = new ImBoolean(true);
    private final ImBoolean chunkOutline   = new ImBoolean(true);
    private final ImBoolean drawTracers    = new ImBoolean(true);
    private final ImBoolean drawStats      = new ImBoolean(true);
    private final ImBoolean notifyChat     = new ImBoolean(true);
    private final ImBoolean notifySound    = new ImBoolean(true);

    private final Set<Long> notifiedChunks = new HashSet<>();
    private long lastClearTime = 0;

    public SusChunkESPModule() {
        super("SusChunkESP", "Erkennt verdächtige Chunk-Cluster mit vielen Kisten, Hoppern & Spawnern bis zum Bedrock.", ModuleCategory.RENDER, -1);
    }

    private static class ChunkReport {
        int chests = 0;
        int barrels = 0;
        int enderChests = 0;
        int shulkers = 0;
        int furnaces = 0;
        int hoppers = 0;
        int spawners = 0;
        int beacons = 0;
        int dispensers = 0;
        int deepslateCount = 0; // Containers below y=0 (near bedrock)

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        void includePos(BlockPos p) {
            minX = Math.min(minX, p.getX());
            minY = Math.min(minY, p.getY());
            minZ = Math.min(minZ, p.getZ());
            maxX = Math.max(maxX, p.getX());
            maxY = Math.max(maxY, p.getY());
            maxZ = Math.max(maxZ, p.getZ());

            if (p.getY() <= 0) {
                deepslateCount++;
            }
        }

        int calculateScore() {
            // Extra weight (+4) for hidden containers located deep underground below y=0 near bedrock!
            return (chests * 2) + (barrels * 2) + (enderChests * 3) + (shulkers * 8) +
                   (furnaces * 2) + (hoppers * 3) + (spawners * 12) +
                   (beacons * 15) + (dispensers * 2) + (deepslateCount * 4);
        }

        int totalContainers() {
            return chests + barrels + enderChests + shulkers + hoppers + furnaces + dispensers;
        }
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        long now = System.currentTimeMillis();
        if (now - lastClearTime > 60000) {
            notifiedChunks.clear();
            lastClearTime = now;
        }

        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        int rad = Math.min(scanRadius.get(), 12);

        Vec3 bestCenter = null;
        int highestScore = 0;

        for (int cx = pcx - rad; cx <= pcx + rad; cx++) {
            for (int cz = pcz - rad; cz <= pcz + rad; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                List<BlockEntity> blockEntities;
                try {
                    blockEntities = new ArrayList<>(chunk.getBlockEntities().values());
                } catch (Throwable t) {
                    continue;
                }

                if (blockEntities.isEmpty()) continue;

                ChunkReport rep = new ChunkReport();

                for (BlockEntity be : blockEntities) {
                    if (be.isRemoved()) continue;
                    BlockPos p = be.getBlockPos();

                    if (be instanceof ChestBlockEntity) {
                        rep.chests++;
                        rep.includePos(p);
                    } else if (be instanceof BarrelBlockEntity) {
                        rep.barrels++;
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
                if (score >= thresholdScore.get() && (rep.totalContainers() > 0 || rep.spawners > 0)) {
                    int color;
                    if (score < 20) {
                        color = RenderUtils.toARGB(1.0f, 0.85f, 0.1f, 1.0f); // Gold
                    } else if (score < 50) {
                        color = RenderUtils.toARGB(1.0f, 0.45f, 0.05f, 1.0f); // Vivid Orange
                    } else {
                        color = RenderUtils.toARGB(1.0f, 0.1f, 0.35f, 1.0f); // Neon Magenta / Red (Mega Base)
                    }

                    // 1. Draw tight bounding box around the container stash
                    if (tightBox.get() && rep.minX <= rep.maxX) {
                        AABB stashBox = new AABB(rep.minX - 0.25, rep.minY - 0.25, rep.minZ - 0.25,
                                                 rep.maxX + 1.25, rep.maxY + 1.25, rep.maxZ + 1.25);
                        net.minecraft.gizmos.GizmoStyle stashStyle = net.minecraft.gizmos.GizmoStyle.strokeAndFill(
                                color, 2.0f, (color & 0x00FFFFFF) | 0x30000000);
                        try {
                            net.minecraft.gizmos.Gizmos.cuboid(stashBox, stashStyle).setAlwaysOnTop();
                        } catch (Throwable ignored) {}

                        Vec3 center = stashBox.getCenter();
                        if (score > highestScore) {
                            highestScore = score;
                            bestCenter = center;
                        }
                    }

                    // 2. Draw 16x16 full chunk column outline so it's impossible to miss
                    if (chunkOutline.get()) {
                        double x0 = cx * 16.0;
                        double z0 = cz * 16.0;
                        double x1 = x0 + 16.0;
                        double z1 = z0 + 16.0;
                        double y0 = Math.max(-64.0, (double) chunk.getMinY());
                        double y1 = Math.min(320.0, (double) chunk.getMaxY());

                        AABB chunkBox = new AABB(x0, y0, z0, x1, y1, z1);
                        net.minecraft.gizmos.GizmoStyle chunkStyle = net.minecraft.gizmos.GizmoStyle.stroke(color, 1.5f);
                        try {
                            net.minecraft.gizmos.Gizmos.cuboid(chunkBox, chunkStyle).setAlwaysOnTop();
                        } catch (Throwable ignored) {}

                        if (bestCenter == null || score > highestScore) {
                            bestCenter = new Vec3((x0 + x1) * 0.5, (y0 + y1) * 0.5, (z0 + z1) * 0.5);
                            highestScore = score;
                        }
                    }

                    // 3. 3D Billboard text stats over the chunk
                    if (drawStats.get() && rep.minX <= rep.maxX) {
                        String stats = String.format("SUS BASE [Score: %d] (%d Kisten, %d Hopper, %d Bedrock-Stash)",
                                score, rep.chests + rep.shulkers + rep.barrels, rep.hoppers, rep.deepslateCount);
                        try {
                            BlockPos bpos = BlockPos.containing((rep.minX + rep.maxX) * 0.5, rep.maxY + 1.5, (rep.minZ + rep.maxZ) * 0.5);
                            net.minecraft.gizmos.Gizmos.billboardTextOverBlock(stats, bpos, color, RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.80f), 1.1f)
                                    .setAlwaysOnTop();
                        } catch (Throwable ignored) {}
                    }

                    // Notification trigger with chunk pos key
                    long chunkKey = (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
                    if (notifiedChunks.add(chunkKey)) {
                        triggerSusNotification(cx, cz, score, rep.totalContainers(), rep.deepslateCount);
                    }
                }
            }
        }

        // Draw tracer to the highest-scoring base
        if (drawTracers.get() && bestCenter != null) {
            Vec3 eye = mc.player.getEyePosition(tickDelta);
            try {
                net.minecraft.gizmos.Gizmos.line(eye, bestCenter, RenderUtils.toARGB(1.0f, 0.2f, 0.2f, 1.0f), 2.2f)
                        .setAlwaysOnTop();
            } catch (Throwable ignored) {}
        }
    }

    private void triggerSusNotification(int cx, int cz, int score, int containerCount, int deepslateCount) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (notifySound.get() && mc.level != null) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.4f, false);
        }

        if (notifyChat.get()) {
            int blockX = (cx << 4) + 8;
            int blockZ = (cz << 4) + 8;
            String msg = "§6§l[SUS CHUNK GEFUNDEN] §eBasis bei X: " + blockX + ", Z: " + blockZ +
                    " §7(Score: §f" + score + "§7, §c" + containerCount + " Container§7" +
                    (deepslateCount > 0 ? ", §d" + deepslateCount + " bei Bedrock§7" : "") + ")!";
            mc.player.sendSystemMessage(Component.literal(msg));
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Such-Radius (Chunks)##sus", scanRadius.getData(), 2, 12);
        ImGui.sliderInt("Verdachts-Schwelle (Score)##sus", thresholdScore.getData(), 3, 30);
        ImGui.checkbox("Exakte Raum-Box (Tight Box)##sus", tightBox);
        ImGui.checkbox("Voller Chunk-Rahmen (16x16)##sus", chunkOutline);
        ImGui.checkbox("Tracer-Linie zur größten Basis##sus", drawTracers);
        ImGui.checkbox("Cluster-Statistik als 3D-Text##sus", drawStats);
        ImGui.separator();
        ImGui.checkbox("Chat-Benachrichtigung bei Fund##sus", notifyChat);
        ImGui.checkbox("Sound-Effekt bei Fund##sus", notifySound);
        ImGui.spacing();
        ImGui.textDisabled("Scannt bis Y=-64 (Bedrock). Container unter Y=0 erhalten Extra-Score.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("scanRadius", scanRadius.get());
        cfg.putSetting("thresholdScore", thresholdScore.get());
        cfg.putSetting("tightBox", tightBox.get());
        cfg.putSetting("chunkOutline", chunkOutline.get());
        cfg.putSetting("drawTracers", drawTracers.get());
        cfg.putSetting("drawStats", drawStats.get());
        cfg.putSetting("notifyChat", notifyChat.get());
        cfg.putSetting("notifySound", notifySound.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        scanRadius.set(cfg.getSetting("scanRadius", scanRadius.get()));
        thresholdScore.set(cfg.getSetting("thresholdScore", thresholdScore.get()));
        tightBox.set(cfg.getSetting("tightBox", tightBox.get()));
        chunkOutline.set(cfg.getSetting("chunkOutline", chunkOutline.get()));
        drawTracers.set(cfg.getSetting("drawTracers", drawTracers.get()));
        drawStats.set(cfg.getSetting("drawStats", drawStats.get()));
        notifyChat.set(cfg.getSetting("notifyChat", notifyChat.get()));
        notifySound.set(cfg.getSetting("notifySound", notifySound.get()));
    }
}
