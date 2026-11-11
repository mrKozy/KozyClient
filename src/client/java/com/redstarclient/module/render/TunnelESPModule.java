// Red Star Client — TunnelESPModule
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

/**
 * Tunnel ESP:
 * Scans underground layers down to bedrock to detect player-mined tunnels (1x2 strip mines,
 * branch highways, portal tunnels, base passages) and highlights them through walls.
 */
public class TunnelESPModule extends Module {

    private final ImInt     radius       = new ImInt(32);
    private final ImInt     minY         = new ImInt(-58);
    private final ImInt     maxY         = new ImInt(30);
    private final ImBoolean drawTracers  = new ImBoolean(false);

    private final List<AABB> tunnelBoxes = new ArrayList<>();
    private long lastScanTime = 0;

    public TunnelESPModule() {
        super("TunnelESP", "Erkennt unterirdische Spielertunnel, Stripmines & geheime Gänge bis zum Bedrock.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastScanTime > 1500) {
            scanTunnels(mc.level, mc.player.blockPosition());
            lastScanTime = now;
        }

        int color = RenderUtils.toARGB(0.1f, 0.9f, 1.0f, 0.85f); // Bright cyan
        for (AABB box : tunnelBoxes) {
            RenderUtils.drawBox3D(poseStack, camera, box, color, 1.5f, 0.08f);
        }

        if (drawTracers.get() && !tunnelBoxes.isEmpty()) {
            AABB nearest = tunnelBoxes.get(0);
            net.minecraft.gizmos.Gizmos.line(mc.player.getEyePosition(tickDelta), nearest.getCenter(), color, 1.5f)
                    .setAlwaysOnTop();
        }
    }

    private void scanTunnels(Level level, BlockPos playerPos) {
        tunnelBoxes.clear();
        int r = Math.min(48, radius.get());
        int y0 = Math.max(-64, minY.get());
        int y1 = Math.min(60, maxY.get());

        int px = playerPos.getX();
        int pz = playerPos.getZ();

        // Sample underground blocks in 2-block vertical steps
        for (int x = px - r; x <= px + r; x += 3) {
            for (int z = pz - r; z <= pz + r; z += 3) {
                for (int y = y0; y <= y1; y += 2) {
                    BlockPos p = new BlockPos(x, y, z);
                    BlockState s0 = level.getBlockState(p);

                    // Check for 1x2 vertical air cavity
                    if (s0.isAir()) {
                        BlockState s1 = level.getBlockState(p.above());
                        if (s1.isAir()) {
                            // Check solid floor
                            BlockState floor = level.getBlockState(p.below());
                            if (floor.isSolid()) {
                                // Check if walls are enclosed (typical player tunnel)
                                boolean northSouthWall = level.getBlockState(p.north()).isSolid() && level.getBlockState(p.south()).isSolid();
                                boolean eastWestWall = level.getBlockState(p.east()).isSolid() && level.getBlockState(p.west()).isSolid();

                                if (northSouthWall || eastWestWall) {
                                    AABB box = new AABB(p.getX(), p.getY(), p.getZ(),
                                                        p.getX() + 1.0, p.getY() + 2.0, p.getZ() + 1.0);
                                    tunnelBoxes.add(box);
                                    if (tunnelBoxes.size() > 80) return; // Prevent excessive draw calls
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
        ImGui.sliderInt("Scan-Reichweite (Blöcke)##tun", radius.getData(), 16, 48);
        ImGui.sliderInt("Min Y (Tiefe / Bedrock)##tun", minY.getData(), -64, 0);
        ImGui.sliderInt("Max Y (Höhe)##tun", maxY.getData(), 0, 64);
        ImGui.checkbox("Tracer zum nächsten Tunnel##tun", drawTracers);
        ImGui.spacing();
        ImGui.textDisabled("Findet 1x2 Gänge & Stollen unter der Erde, die zu geheimen Basen führen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("radius", radius.get());
        cfg.putSetting("minY", minY.get());
        cfg.putSetting("maxY", maxY.get());
        cfg.putSetting("drawTracers", drawTracers.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        radius.set(cfg.getSetting("radius", radius.get()));
        minY.set(cfg.getSetting("minY", minY.get()));
        maxY.set(cfg.getSetting("maxY", maxY.get()));
        drawTracers.set(cfg.getSetting("drawTracers", drawTracers.get()));
    }
}
