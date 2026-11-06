// ☭ Red Star Client — AncientDebrisFinderModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Ancient Debris Finder:
 * Dedicated scanner for Netherite Ancient Debris ore veins with high-contrast 3D boxes.
 */
public class AncientDebrisFinderModule extends Module {

    private final ImInt radius = new ImInt(24);
    private final List<BlockPos> debrisPositions = new ArrayList<>();
    private long lastScanTime = 0L;

    public AncientDebrisFinderModule() {
        super("AncientDebris", "Findet antiken Schrott (Netherite / Ancient Debris) im Nether durch Waende.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScanTime < 1000) return; // Scan once per second
        lastScanTime = now;

        debrisPositions.clear();
        BlockPos playerPos = mc.player.blockPosition();
        int r = radius.get();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -16; dy <= 16; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos p = playerPos.offset(dx, dy, dz);
                    if (mc.level.getBlockState(p).is(Blocks.ANCIENT_DEBRIS)) {
                        debrisPositions.add(p);
                    }
                }
            }
        }
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        if (debrisPositions.isEmpty()) return;

        int debrisColor = RenderUtils.toARGB(1.0f, 0.45f, 0.05f, 0.95f); // Glowing orange/gold
        for (BlockPos p : debrisPositions) {
            AABB bb = new AABB(p);
            RenderUtils.drawBox3D(poseStack, camera, bb, debrisColor);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Scan-Radius (Bloecke)##ad", radius.getData(), 8, 48);
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "Gefundene Debris: " + debrisPositions.size());
        ImGui.spacing();
        ImGui.textDisabled("Spart hunderte von Betten und TNT beim Netherite-Mining.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("radius", radius.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        radius.set(cfg.getSetting("radius", radius.get()));
    }
}
