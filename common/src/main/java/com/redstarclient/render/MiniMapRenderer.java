// ☭ Red Star Client — MiniMapRenderer
package com.redstarclient.render;

import com.redstarclient.util.BlockUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Scans chunks around the player and caches top-block colors for the minimap.
 * Updated incrementally — only re-scans when the player moves 4+ blocks.
 */
public class MiniMapRenderer {

    private int[]     mapColors;
    private BlockPos  lastScanCenter;
    private boolean   dirty = true;
    private int       lastRadius = 0;

    public void update(ClientLevel world, BlockPos center, int radius) {
        if (!dirty && lastScanCenter != null && radius == lastRadius) {
            double dx = center.getX() - lastScanCenter.getX();
            double dz = center.getZ() - lastScanCenter.getZ();
            if (dx * dx + dz * dz < 16) return; // Within 4 blocks — skip
        }

        int diameter = radius * 2 + 1;
        mapColors    = new int[diameter * diameter];
        lastRadius   = radius;

        int idx = 0;
        for (int z = center.getZ() - radius; z <= center.getZ() + radius; z++) {
            for (int x = center.getX() - radius; x <= center.getX() + radius; x++) {
                mapColors[idx++] = getTopBlockColor(world, x, z);
            }
        }

        lastScanCenter = center;
        dirty = false;
    }

    private int getTopBlockColor(ClientLevel world, int x, int z) {
        for (int y = world.getMaxY(); y >= world.getMinY(); y--) {
            BlockPos pos   = new BlockPos(x, y, z);
            BlockState state = world.getBlockState(pos);
            if (!BlockUtils.isAir(state)) {
                return BlockUtils.getBlockColor(state);
            }
        }
        return 0xFF111111; // void / bedrock = dark
    }

    public int[] getMapColors() { return mapColors; }
    public void markDirty()     { dirty = true; }
}
