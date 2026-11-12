// KozyClient — TunnelESPModule
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Tunnel ESP:
 * Scans underground layers around the player and down to bedrock to detect player-mined
 * tunnels (1x2 strip mines, 2x2 highways, branch mines, rail tunnels) and renders
 * bright glowing cyan wireframes through walls.
 */
public class TunnelESPModule extends Module {

    private final ImInt     scanRadius   = new ImInt(24);
    private final ImInt     minY         = new ImInt(-60);
    private final ImInt     maxY         = new ImInt(40);
    private final ImBoolean drawTracers  = new ImBoolean(true);
    private final ImBoolean connectPath  = new ImBoolean(true);

    private final List<AABB> tunnelBoxes = new ArrayList<>();
    private final List<Vec3> tunnelPoints = new ArrayList<>();
    private long lastScanTime = 0;

    public TunnelESPModule() {
        super("TunnelESP", "Erkennt unterirdische Spielertunnel, Stripmines & geheime Gänge bis zum Bedrock.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        long now = System.currentTimeMillis();
        if (now - lastScanTime > 1200) {
            scanTunnels(mc.level, mc.player.blockPosition());
            lastScanTime = now;
        }

        int cyanBox   = RenderUtils.toARGB(0.0f, 0.95f, 1.0f, 0.90f);
        int cyanFill  = RenderUtils.toARGB(0.0f, 0.80f, 1.0f, 0.12f);
        int lineColor = RenderUtils.toARGB(0.2f, 1.0f, 0.85f, 1.0f);

        // Render each tunnel bounding box through walls
        net.minecraft.gizmos.GizmoStyle style = net.minecraft.gizmos.GizmoStyle.strokeAndFill(cyanBox, 1.8f, cyanFill);
        for (AABB box : tunnelBoxes) {
            try {
                net.minecraft.gizmos.Gizmos.cuboid(box, style).setAlwaysOnTop();
            } catch (Throwable ignored) {}
        }

        // Draw connected path between consecutive tunnel points
        if (connectPath.get() && tunnelPoints.size() > 1) {
            for (int i = 0; i < tunnelPoints.size() - 1; i++) {
                Vec3 p1 = tunnelPoints.get(i);
                Vec3 p2 = tunnelPoints.get(i + 1);
                if (p1.distanceToSqr(p2) < 25.0) { // within 5 blocks
                    try {
                        net.minecraft.gizmos.Gizmos.line(p1, p2, lineColor, 2.0f).setAlwaysOnTop();
                    } catch (Throwable ignored) {}
                }
            }
        }

        // Draw tracer line from player eye to nearest tunnel
        if (drawTracers.get() && !tunnelPoints.isEmpty()) {
            Vec3 eye = mc.player.getEyePosition(tickDelta);
            Vec3 nearest = tunnelPoints.get(0);
            double minDist = eye.distanceToSqr(nearest);
            for (Vec3 pt : tunnelPoints) {
                double d = eye.distanceToSqr(pt);
                if (d < minDist) {
                    minDist = d;
                    nearest = pt;
                }
            }
            try {
                net.minecraft.gizmos.Gizmos.line(eye, nearest, cyanBox, 2.0f).setAlwaysOnTop();
            } catch (Throwable ignored) {}
        }
    }

    private boolean isPassable(BlockState s) {
        if (s.isAir()) return true;
        Block b = s.getBlock();
        return b == Blocks.TORCH || b == Blocks.WALL_TORCH ||
               b == Blocks.SOUL_TORCH || b == Blocks.SOUL_WALL_TORCH ||
               b == Blocks.REDSTONE_TORCH || b == Blocks.REDSTONE_WALL_TORCH ||
               b == Blocks.RAIL || b == Blocks.POWERED_RAIL || b == Blocks.DETECTOR_RAIL || b == Blocks.ACTIVATOR_RAIL ||
               b == Blocks.REDSTONE_WIRE || b == Blocks.LADDER || b == Blocks.LEVER ||
               b == Blocks.TRIPWIRE || b == Blocks.LANTERN || b == Blocks.SOUL_LANTERN;
    }

    private boolean isSolid(BlockState s) {
        return !s.isAir() && s.blocksMotion();
    }

    private void scanTunnels(Level level, BlockPos playerPos) {
        tunnelBoxes.clear();
        tunnelPoints.clear();

        int r = Math.min(32, Math.max(12, scanRadius.get()));
        int py = playerPos.getY();
        int yBottom = Math.max(-64, Math.min(minY.get(), py - 18));
        int yTop = Math.min(64, Math.max(maxY.get(), py + 10));

        int px = playerPos.getX();
        int pz = playerPos.getZ();

        for (int x = px - r; x <= px + r; x++) {
            for (int z = pz - r; z <= pz + r; z++) {
                // Quick horizontal distance check
                int dx = x - px;
                int dz = z - pz;
                if ((dx * dx + dz * dz) > (r * r)) continue;

                for (int y = yBottom; y <= yTop; y++) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s0 = level.getBlockState(p);

                    // Check for 1x2 tunnel cavity
                    if (isPassable(s0)) {
                        BlockState s1 = level.getBlockState(p.above());
                        if (isPassable(s1)) {
                            // Must have solid floor and solid ceiling
                            BlockState floor = level.getBlockState(p.below());
                            BlockState roof  = level.getBlockState(p.above(2));

                            if (isSolid(floor) && isSolid(roof)) {
                                // Check if walls enclose the passage
                                boolean northSolid = isSolid(level.getBlockState(p.north()));
                                boolean southSolid = isSolid(level.getBlockState(p.south()));
                                boolean eastSolid  = isSolid(level.getBlockState(p.east()));
                                boolean westSolid  = isSolid(level.getBlockState(p.west()));

                                boolean isTunnel = (northSolid && southSolid) || (eastSolid && westSolid);
                                // Also allow corners / turns in tunnels
                                if (!isTunnel && (northSolid || southSolid) && (eastSolid || westSolid)) {
                                    isTunnel = true;
                                }

                                if (isTunnel) {
                                    AABB box = new AABB(p.getX() + 0.05, p.getY() + 0.05, p.getZ() + 0.05,
                                                        p.getX() + 0.95, p.getY() + 1.95, p.getZ() + 0.95);
                                    tunnelBoxes.add(box);
                                    tunnelPoints.add(box.getCenter());

                                    y++; // Skip the head block so we don't duplicate
                                    if (tunnelBoxes.size() >= 120) return; // Prevent performance drops
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Scan-Reichweite (Radius)##tun", scanRadius.getData(), 12, 32);
        ImGui.sliderInt("Min Y (Tiefe / Bedrock)##tun", minY.getData(), -64, 0);
        ImGui.sliderInt("Max Y (Höhe)##tun", maxY.getData(), 0, 64);
        ImGui.checkbox("Tracer zum nächsten Tunnel##tun", drawTracers);
        ImGui.checkbox("Tunnel-Pfad verbinden (Linie)##tun", connectPath);
        ImGui.spacing();
        ImGui.textDisabled("Erkennt 1x2 Gänge & Stollen unter der Erde (Cyan Wireframe + Tracer).");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("scanRadius", scanRadius.get());
        cfg.putSetting("minY", minY.get());
        cfg.putSetting("maxY", maxY.get());
        cfg.putSetting("drawTracers", drawTracers.get());
        cfg.putSetting("connectPath", connectPath.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        scanRadius.set(cfg.getSetting("scanRadius", scanRadius.get()));
        minY.set(cfg.getSetting("minY", minY.get()));
        maxY.set(cfg.getSetting("maxY", maxY.get()));
        drawTracers.set(cfg.getSetting("drawTracers", drawTracers.get()));
        connectPath.set(cfg.getSetting("connectPath", connectPath.get()));
    }
}
