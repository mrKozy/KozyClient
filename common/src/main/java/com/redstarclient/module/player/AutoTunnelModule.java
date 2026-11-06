// ☭ Red Star Client — AutoTunnelModule
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Automatically mines a 3x3 area (9 blocks total) centered on the block broken,
 * creating an instant tunnel or clearing a 3x3 face like a mining hammer.
 */
public class AutoTunnelModule extends Module {

    private final ImBoolean respectHardness = new ImBoolean(true);
    private final ImBoolean skipFluids     = new ImBoolean(true);

    private boolean isExcavating = false;

    public AutoTunnelModule() {
        super("AutoTunnel", "Mines a 3x3 area centered on broken blocks (Mining Hammer).", ModuleCategory.PLAYER, -1);
    }

    public void onBlockBroken(BlockPos centerPos, Direction hitFace) {
        if (!isEnabled() || isExcavating) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.gameMode == null) return;

        Direction face = hitFace;
        if (face == null) {
            if (mc.hitResult instanceof BlockHitResult bhr) {
                face = bhr.getDirection();
            } else {
                face = mc.player.getDirection();
            }
        }

        List<BlockPos> positions = get3x3Positions(centerPos, face);

        isExcavating = true;
        try {
            for (BlockPos target : positions) {
                if (target.equals(centerPos)) continue;

                BlockState state = mc.level.getBlockState(target);
                if (state.isAir()) continue;
                if (skipFluids.get() && (state.is(Blocks.WATER) || state.is(Blocks.LAVA))) continue;
                if (respectHardness.get() && state.getDestroySpeed(mc.level, target) < 0) continue; // Skip bedrock

                // Break surrounding block
                if (mc.getConnection() != null) {
                    mc.getConnection().send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK,
                            target,
                            face
                    ));
                    mc.getConnection().send(new ServerboundPlayerActionPacket(
                            ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK,
                            target,
                            face
                    ));
                }
                mc.gameMode.destroyBlock(target);
            }
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] AutoTunnel error: " + t.getMessage());
        } finally {
            isExcavating = false;
        }
    }

    private List<BlockPos> get3x3Positions(BlockPos center, Direction face) {
        List<BlockPos> list = new ArrayList<>(9);
        int cx = center.getX();
        int cy = center.getY();
        int cz = center.getZ();

        if (face == Direction.UP || face == Direction.DOWN) {
            // Horizontal floor/ceiling face (X and Z)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    list.add(new BlockPos(cx + dx, cy, cz + dz));
                }
            }
        } else if (face == Direction.NORTH || face == Direction.SOUTH) {
            // Facing North/South (X and Y)
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    list.add(new BlockPos(cx + dx, cy + dy, cz));
                }
            }
        } else {
            // Facing East/West (Z and Y)
            for (int dz = -1; dz <= 1; dz++) {
                for (int dy = -1; dy <= 1; dy++) {
                    list.add(new BlockPos(cx, cy + dy, cz + dz));
                }
            }
        }

        return list;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textDisabled("Breaks 9 blocks (3x3) whenever you mine the center block.");
        ImGui.checkbox("Respect Bedrock / Hardness##at", respectHardness);
        ImGui.checkbox("Skip Water / Lava Blocks##at", skipFluids);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("respectHardness", respectHardness.get());
        cfg.putSetting("skipFluids", skipFluids.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        respectHardness.set(cfg.getSetting("respectHardness", respectHardness.get()));
        skipFluids.set(cfg.getSetting("skipFluids", skipFluids.get()));
    }
}
