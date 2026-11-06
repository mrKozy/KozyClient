// ☭ Red Star Client — BlockUtils
package com.redstarclient.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.util.Set;

/**
 * Block scanning and classification utilities.
 */
public final class BlockUtils {

    private BlockUtils() {}

    /** All vanilla ore blocks (overworld + nether + deepslate variants). */
    public static boolean isOre(Block block) {
        return block == Blocks.DIAMOND_ORE        || block == Blocks.DEEPSLATE_DIAMOND_ORE
            || block == Blocks.IRON_ORE           || block == Blocks.DEEPSLATE_IRON_ORE
            || block == Blocks.GOLD_ORE           || block == Blocks.DEEPSLATE_GOLD_ORE
            || block == Blocks.COPPER_ORE         || block == Blocks.DEEPSLATE_COPPER_ORE
            || block == Blocks.COAL_ORE           || block == Blocks.DEEPSLATE_COAL_ORE
            || block == Blocks.LAPIS_ORE          || block == Blocks.DEEPSLATE_LAPIS_ORE
            || block == Blocks.REDSTONE_ORE       || block == Blocks.DEEPSLATE_REDSTONE_ORE
            || block == Blocks.EMERALD_ORE        || block == Blocks.DEEPSLATE_EMERALD_ORE
            || block == Blocks.NETHER_GOLD_ORE    || block == Blocks.NETHER_QUARTZ_ORE
            || block == Blocks.ANCIENT_DEBRIS;
    }

    /** Storage containers that StorageESP should highlight. */
    public static boolean isStorage(Block block) {
        return block == Blocks.CHEST            || block == Blocks.TRAPPED_CHEST
            || block == Blocks.BARREL           || block == Blocks.ENDER_CHEST
            || block == Blocks.HOPPER           || block == Blocks.DROPPER
            || block == Blocks.DISPENSER        || block == Blocks.FURNACE
            || block == Blocks.BLAST_FURNACE    || block == Blocks.SMOKER
            || block.defaultBlockState().is(BlockTags.SHULKER_BOXES);
    }

    /** Default X-Ray visible block set: all ores + chest + spawner. */
    public static Set<Block> getDefaultXRayBlocks() {
        return Set.of(
            Blocks.DIAMOND_ORE,   Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.IRON_ORE,      Blocks.DEEPSLATE_IRON_ORE,
            Blocks.GOLD_ORE,      Blocks.DEEPSLATE_GOLD_ORE,
            Blocks.COPPER_ORE,    Blocks.DEEPSLATE_COPPER_ORE,
            Blocks.COAL_ORE,      Blocks.DEEPSLATE_COAL_ORE,
            Blocks.LAPIS_ORE,     Blocks.DEEPSLATE_LAPIS_ORE,
            Blocks.REDSTONE_ORE,  Blocks.DEEPSLATE_REDSTONE_ORE,
            Blocks.EMERALD_ORE,   Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.NETHER_GOLD_ORE, Blocks.ANCIENT_DEBRIS,
            Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.SPAWNER
        );
    }

    /**
     * Returns a rough ARGB minimap color for a block state.
     * Not exhaustive — covers the most common surface block types.
     */
    public static int getBlockColor(BlockState state) {
        Block b = state.getBlock();
        if (b == Blocks.GRASS_BLOCK)           return 0xFF5AA024;
        if (b == Blocks.DIRT || b == Blocks.COARSE_DIRT) return 0xFF7A5C3A;
        if (b == Blocks.SAND || b == Blocks.SANDSTONE)   return 0xFFE8D59A;
        if (b == Blocks.STONE || b == Blocks.COBBLESTONE) return 0xFF888888;
        if (b == Blocks.GRAVEL)                return 0xFF777777;
        if (b == Blocks.WATER)                 return 0xFF2244CC;
        if (b == Blocks.LAVA)                  return 0xFFCC4400;
        if (b == Blocks.SNOW || b == Blocks.SNOW_BLOCK)  return 0xFFEEEEFF;
        if (b == Blocks.ICE || b == Blocks.PACKED_ICE)   return 0xFF90C0FF;
        if (b == Blocks.BEDROCK)               return 0xFF222222;
        if (b == Blocks.NETHERRACK)            return 0xFF882222;
        if (b == Blocks.SOUL_SAND || b == Blocks.SOUL_SOIL) return 0xFF4A3830;
        if (b == Blocks.OBSIDIAN)              return 0xFF222233;
        if (isOre(b))                          return 0xFFFFFF55; // bright yellow for ores
        if (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE) return 0xFF55FFFF;
        // Leaves / wood / logs → greenish / brown
        if (state.is(BlockTags.LOGS))          return 0xFF5A3A20;
        if (state.is(BlockTags.LEAVES))        return 0xFF256B24;
        // Fallback: dark grey
        return 0xFF666666;
    }

    public static boolean isAir(BlockState state) {
        return state.isAir();
    }

    public static boolean isReplaceable(BlockState state) {
        return state.isAir() || state.getBlock() == Blocks.WATER
            || state.getBlock() == Blocks.LAVA
            || state.getBlock() == Blocks.TALL_GRASS
            || state.getBlock() == Blocks.SHORT_GRASS;
    }

    public static BlockPos getBlockBelow(LocalPlayer player) {
        return player.blockPosition().below();
    }
}
