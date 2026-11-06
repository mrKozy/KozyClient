// ☭ Red Star Client — BaseFinderModule
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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/**
 * Base Finder (Donut SMP Vault Hunter):
 * Scans loaded chunks for hidden underground vaults, chest clusters,
 * mob spawners, and beacons through walls.
 */
public class BaseFinderModule extends Module {

    private final ImInt     radius       = new ImInt(48);
    private final ImBoolean findChests   = new ImBoolean(true);
    private final ImBoolean findSpawners = new ImBoolean(true);
    private final ImBoolean findBeacons  = new ImBoolean(true);

    public BaseFinderModule() {
        super("BaseFinder", "Lokalisiert geheime Basen, Kisten-Stashes, Spawner und Beacons (Donut SMP).", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        int chunkDist = Math.min(radius.get() >> 4, 6);

        for (int cx = pcx - chunkDist; cx <= pcx + chunkDist; cx++) {
            for (int cz = pcz - chunkDist; cz <= pcz + chunkDist; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be.isRemoved()) continue;
                    BlockPos pos = be.getBlockPos();

                    if (be instanceof SpawnerBlockEntity && findSpawners.get()) {
                        AABB bb = new AABB(pos);
                        RenderUtils.drawBox3D(poseStack, camera, bb, RenderUtils.toARGB(1.0f, 0.2f, 0.2f, 1.0f));
                    } else if (be instanceof ChestBlockEntity && findChests.get()) {
                        AABB bb = new AABB(pos);
                        RenderUtils.drawBox3D(poseStack, camera, bb, RenderUtils.toARGB(1.0f, 0.85f, 0.1f, 0.8f));
                    } else if (findBeacons.get() && be.getBlockState().getBlock().getDescriptionId().contains("beacon")) {
                        AABB bb = new AABB(pos);
                        RenderUtils.drawBox3D(poseStack, camera, bb, RenderUtils.toARGB(0.2f, 0.95f, 1.0f, 0.9f));
                    }
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Such-Radius##bf", radius.getData(), 16, 96);
        ImGui.checkbox("Kisten / Stashes##bf", findChests);
        ImGui.checkbox("Mob-Spawner##bf", findSpawners);
        ImGui.checkbox("Beacons##bf", findBeacons);
        ImGui.spacing();
        ImGui.textDisabled("Findet unterirdische Bunker und Verstecke auf SMP-Servern.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("radius", radius.get());
        cfg.putSetting("findChests", findChests.get());
        cfg.putSetting("findSpawners", findSpawners.get());
        cfg.putSetting("findBeacons", findBeacons.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        radius.set(cfg.getSetting("radius", radius.get()));
        findChests.set(cfg.getSetting("findChests", findChests.get()));
        findSpawners.set(cfg.getSetting("findSpawners", findSpawners.get()));
        findBeacons.set(cfg.getSetting("findBeacons", findBeacons.get()));
    }
}
