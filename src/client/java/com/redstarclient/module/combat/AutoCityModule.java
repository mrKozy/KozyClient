// â˜­ Red Star Client â€” AutoCityModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * AutoCity Module:
 * Detects enemy players encased in an Obsidian/Ender Chest surround and
 * automatically targets and mines the closest surrounding block with best tool
 * to expose the enemy to immediate End Crystal blast damage.
 */
public class AutoCityModule extends Module {

    private final ImFloat   range             = new ImFloat(4.5f);
    private final ImBoolean autoSwitchPickaxe = new ImBoolean(true);
    private final ImBoolean silentRotations   = new ImBoolean(true);
    private final ImBoolean antiCheatMode     = new ImBoolean(true);

    private BlockPos miningTarget = null;
    private long lastMineTime = 0L;

    public AutoCityModule() {
        super("AutoCity", "Baut gegnerische Surround-Bloecke automatisch ab, um Gegner fuer Crystals freizulegen.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        float effectiveRange = antiCheatMode.get() ? Math.min(3.8f, range.get()) : range.get();

        Entity target = PlayerUtils.getClosestEntity(mc.player, effectiveRange + 2.0, true);
        if (!(target instanceof Player enemy)) {
            miningTarget = null;
            return;
        }

        BlockPos targetPos = findBestCityBlock(mc, enemy, effectiveRange);
        if (targetPos == null) {
            miningTarget = null;
            return;
        }

        miningTarget = targetPos;

        // Equip best pickaxe
        if (autoSwitchPickaxe.get()) {
            int pickSlot = findPickaxe(mc);
            if (pickSlot != -1 && mc.player.getInventory().getSelectedSlot() != pickSlot) {
                mc.player.getInventory().setSelectedSlot(pickSlot);
            }
        }

        Vec3 hitVec = Vec3.atCenterOf(targetPos);

        if (silentRotations.get()) {
            float[] rots = PlayerUtils.getRotationsToVec(mc.player.getEyePosition(1f), hitVec);
            PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
        }

        long now = System.currentTimeMillis();
        if (now - lastMineTime > 50) {
            lastMineTime = now;
            mc.gameMode.continueDestroyBlock(targetPos, Direction.UP);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    private BlockPos findBestCityBlock(Minecraft mc, Player enemy, float maxReach) {
        BlockPos feet = enemy.blockPosition();
        BlockPos[] surroundOffsets = {
                feet.north(),
                feet.south(),
                feet.east(),
                feet.west()
        };

        BlockPos best = null;
        double minPlayerDist = Double.MAX_VALUE;

        for (BlockPos pos : surroundOffsets) {
            BlockState state = mc.level.getBlockState(pos);
            if (isCityableBlock(state)) {
                double dist = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                if (dist <= maxReach * maxReach && dist < minPlayerDist) {
                    minPlayerDist = dist;
                    best = pos;
                }
            }
        }
        return best;
    }

    private boolean isCityableBlock(BlockState state) {
        return state.is(Blocks.OBSIDIAN) ||
               state.is(Blocks.CRYING_OBSIDIAN) ||
               state.is(Blocks.ENDER_CHEST) ||
               state.is(Blocks.RESPAWN_ANCHOR) ||
               state.is(Blocks.NETHERITE_BLOCK);
    }

    private int findPickaxe(Minecraft mc) {
        int slot = InventoryUtils.findItemInHotbar(mc.player, Items.NETHERITE_PICKAXE);
        if (slot != -1) return slot;
        return InventoryUtils.findItemInHotbar(mc.player, Items.DIAMOND_PICKAXE);
    }

    public BlockPos getMiningTarget() {
        return isEnabled() ? miningTarget : null;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("AntiCheat-Safe Reach (<=3.8m)##city", antiCheatMode);
        ImGui.checkbox("Silent Rotations##city", silentRotations);
        ImGui.checkbox("Auto-Switch zu Spitzhacke##city", autoSwitchPickaxe);
        ImGui.sliderFloat("Reichweite##city", range.getData(), 2.0f, 5.0f, "%.1fm");
        ImGui.spacing();
        if (miningTarget != null) {
            ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "Ziel: " + miningTarget.getX() + ", " + miningTarget.getY() + ", " + miningTarget.getZ());
        } else {
            ImGui.textDisabled("Kein gegnerischer Surround im Radius.");
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("autoSwitchPickaxe", autoSwitchPickaxe.get());
        cfg.putSetting("silentRotations", silentRotations.get());
        cfg.putSetting("antiCheatMode", antiCheatMode.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        autoSwitchPickaxe.set(cfg.getSetting("autoSwitchPickaxe", autoSwitchPickaxe.get()));
        silentRotations.set(cfg.getSetting("silentRotations", silentRotations.get()));
        antiCheatMode.set(cfg.getSetting("antiCheatMode", antiCheatMode.get()));
    }
}
