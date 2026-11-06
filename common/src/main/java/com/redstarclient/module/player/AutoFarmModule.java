// ☭ Red Star Client — AutoFarmModule (from Wurst 7)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * AutoFarm (from Wurst 7):
 * Automatically harvests ripe crops (wheat, carrots, potatoes, beetroots, nether wart)
 * and replants seeds instantly.
 */
public class AutoFarmModule extends Module {

    private final ImInt     radius      = new ImInt(4);
    private final ImBoolean autoReplant = new ImBoolean(true);

    private long lastActionTime = 0L;

    public AutoFarmModule() {
        super("AutoFarm", "Erntet automatisch reife Feldfruechte und saet sofort nach (Wurst 7).", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastActionTime < 120) return; // Action delay

        BlockPos playerPos = mc.player.blockPosition();
        int r = radius.get();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    BlockState state = mc.level.getBlockState(pos);

                    // 1. Harvest ripe crops
                    if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                        lastActionTime = now;
                        mc.gameMode.destroyBlock(pos);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        return;
                    } else if (state.is(Blocks.NETHER_WART) && state.getValue(NetherWartBlock.AGE) >= 3) {
                        lastActionTime = now;
                        mc.gameMode.destroyBlock(pos);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                        return;
                    }

                    // 2. Replant on empty farmland
                    if (autoReplant.get() && state.isAir()) {
                        BlockState below = mc.level.getBlockState(pos.below());
                        if (below.is(Blocks.FARMLAND)) {
                            int seedSlot = findSeed(mc);
                            if (seedSlot != -1) {
                                lastActionTime = now;
                                int prev = mc.player.getInventory().selected;
                                mc.player.getInventory().selected = seedSlot;

                                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos.below()), Direction.UP, pos.below(), false);
                                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                                mc.player.swing(InteractionHand.MAIN_HAND);

                                mc.player.getInventory().selected = prev;
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    private int findSeed(Minecraft mc) {
        Item[] seeds = { Items.WHEAT_SEEDS, Items.CARROT, Items.POTATO, Items.BEETROOT_SEEDS };
        for (Item seed : seeds) {
            int slot = InventoryUtils.findItemInHotbar(mc.player, seed);
            if (slot != -1) return slot;
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Ernte-Radius##afm", radius.getData(), 2, 6);
        ImGui.checkbox("Automatisch nachsaeen (Replant)##afm", autoReplant);
        ImGui.spacing();
        ImGui.textDisabled("Automatisiert die Feldarbeit fuer Weizen, Karotten, Kartoffeln und Netherwarzen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("radius", radius.get());
        cfg.putSetting("autoReplant", autoReplant.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        radius.set(cfg.getSetting("radius", radius.get()));
        autoReplant.set(cfg.getSetting("autoReplant", autoReplant.get()));
    }
}
