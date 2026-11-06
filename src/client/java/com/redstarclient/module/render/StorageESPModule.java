// ☭ Red Star Client — StorageESPModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;

/**
 * Highlights storage containers (chests, shulkers, furnaces, etc.) through walls with 3D boxes.
 */
public class StorageESPModule extends Module {

    private final ImBoolean chests     = new ImBoolean(true);
    private final ImBoolean shulkers   = new ImBoolean(true);
    private final ImBoolean enderChest = new ImBoolean(true);
    private final ImBoolean furnaces   = new ImBoolean(true);
    private final ImBoolean hoppers    = new ImBoolean(true);

    public StorageESPModule() {
        super("StorageESP", "Highlights storage containers through walls with 3D glowing boxes.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        int pcx = mc.player.getBlockX() >> 4;
        int pcz = mc.player.getBlockZ() >> 4;
        int dist = Math.min(mc.options.renderDistance().get(), 6);

        for (int cx = pcx - dist; cx <= pcx + dist; cx++) {
            for (int cz = pcz - dist; cz <= pcz + dist; cz++) {
                LevelChunk chunk = mc.level.getChunkSource().getChunk(cx, cz, false);
                if (chunk == null) continue;

                java.util.List<BlockEntity> blockEntities;
                try {
                    blockEntities = new java.util.ArrayList<>(chunk.getBlockEntities().values());
                } catch (Throwable t) {
                    continue;
                }

                for (BlockEntity be : blockEntities) {
                    if (be.isRemoved()) continue;
                    Block b = be.getBlockState().getBlock();
                    int color = getColor(b);
                    if (color != 0) {
                        BlockPos pos = be.getBlockPos();
                        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                                            pos.getX() + 1.0, pos.getY() + 1.0, pos.getZ() + 1.0);
                        RenderUtils.drawBox3D(poseStack, camera, box, color);
                    }
                }
            }
        }
    }

    private int getColor(Block b) {
        if (chests.get() && (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST || b == Blocks.BARREL))
            return RenderUtils.SOVIET_GOLD;
        if (shulkers.get() && (b instanceof net.minecraft.world.level.block.ShulkerBoxBlock || b.defaultBlockState().is(BlockTags.SHULKER_BOXES)))
            return RenderUtils.SOVIET_RED;
        if (enderChest.get() && b == Blocks.ENDER_CHEST)
            return RenderUtils.toARGB(0.70f, 0.15f, 0.95f, 1.0f); // purple
        if (furnaces.get() && (b == Blocks.FURNACE || b == Blocks.BLAST_FURNACE || b == Blocks.SMOKER))
            return RenderUtils.toARGB(1.0f, 0.55f, 0.05f, 1.0f); // orange
        if (hoppers.get() && (b == Blocks.HOPPER || b == Blocks.DROPPER || b == Blocks.DISPENSER || b == Blocks.CRAFTER || b == Blocks.BREWING_STAND))
            return RenderUtils.toARGB(0.40f, 0.80f, 0.90f, 1.0f); // teal/cyan
        return 0;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Chests & Barrels##sesp", chests);
        ImGui.checkbox("Shulker Boxes##sesp",    shulkers);
        ImGui.checkbox("Ender Chests##sesp",     enderChest);
        ImGui.checkbox("Furnaces##sesp",         furnaces);
        ImGui.checkbox("Hoppers & Dispensers##sesp", hoppers);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("chests", chests.get());
        cfg.putSetting("shulkers", shulkers.get());
        cfg.putSetting("enderChest", enderChest.get());
        cfg.putSetting("furnaces", furnaces.get());
        cfg.putSetting("hoppers", hoppers.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        chests.set(cfg.getSetting("chests", chests.get()));
        shulkers.set(cfg.getSetting("shulkers", shulkers.get()));
        enderChest.set(cfg.getSetting("enderChest", enderChest.get()));
        furnaces.set(cfg.getSetting("furnaces", furnaces.get()));
        hoppers.set(cfg.getSetting("hoppers", hoppers.get()));
    }
}
