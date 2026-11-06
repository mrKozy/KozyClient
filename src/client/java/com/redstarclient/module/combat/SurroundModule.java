// â˜­ Red Star Client â€” SurroundModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Surround Module (Feet Defense):
 * Automatically surrounds player feet with Obsidian or Ender Chests (North, South, East, West)
 * to neutralize incoming End Crystal blast damage from opponents.
 */
public class SurroundModule extends Module {

    private final ImBoolean centerPlayer    = new ImBoolean(true);
    private final ImBoolean disableOnJump   = new ImBoolean(false);
    private final ImBoolean onlyOnGround    = new ImBoolean(true);
    private final ImBoolean antiCheatMode   = new ImBoolean(true);
    private final ImBoolean silentRotations = new ImBoolean(true);
    private final ImInt     delayMs         = new ImInt(65);

    private long lastPlaceTime = 0L;

    public SurroundModule() {
        super("Surround", "Platziert Obsidian/Ender-Chests um die Fuesse des Spielers fuer maximalen Explosionsschutz.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Auto-Center in block to avoid getting stuck or pushed out
        if (centerPlayer.get() && mc.player.onGround()) {
            double cx = Math.floor(mc.player.getX()) + 0.5;
            double cz = Math.floor(mc.player.getZ()) + 0.5;
            mc.player.setPos(cx, mc.player.getY(), cz);
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (onlyOnGround.get() && !mc.player.onGround()) return;
        if (disableOnJump.get() && mc.player.getDeltaMovement().y > 0.1) {
            setEnabled(false);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastPlaceTime < delayMs.get()) return;

        // Find obsidian / crying obsidian / ender chest in hotbar
        int blockSlot = findDefenseBlock(mc);
        if (blockSlot == -1 && !isHoldingDefenseBlock(mc)) return;

        BlockPos feet = mc.player.blockPosition();
        BlockPos[] offsets = {
                feet.north(),
                feet.south(),
                feet.east(),
                feet.west()
        };

        for (BlockPos targetPos : offsets) {
            BlockState state = mc.level.getBlockState(targetPos);
            if (state.isAir() || state.canBeReplaced()) {
                lastPlaceTime = now;

                int prev = mc.player.getInventory().getSelectedSlot();
                InteractionHand hand = isHoldingDefenseBlock(mc) ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;

                if (hand == InteractionHand.MAIN_HAND && blockSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(blockSlot);
                }

                Vec3 hitVec = Vec3.atCenterOf(targetPos);

                if (silentRotations.get()) {
                    float[] rots = PlayerUtils.getRotationsToVec(mc.player.getEyePosition(1f), hitVec);
                    PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
                }

                BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, targetPos, false);
                mc.gameMode.useItemOn(mc.player, hand, hit);
                mc.player.swing(hand);

                if (hand == InteractionHand.MAIN_HAND && blockSlot != -1) {
                    mc.player.getInventory().setSelectedSlot(prev);
                }

                // In Anti-Cheat mode, place 1 block per delay cycle to prevent FastPlace kicks
                if (antiCheatMode.get()) {
                    return;
                }
            }
        }
    }

    private int findDefenseBlock(Minecraft mc) {
        int slot = InventoryUtils.findItemInHotbar(mc.player, Items.OBSIDIAN);
        if (slot != -1) return slot;
        slot = InventoryUtils.findItemInHotbar(mc.player, Items.CRYING_OBSIDIAN);
        if (slot != -1) return slot;
        return InventoryUtils.findItemInHotbar(mc.player, Items.ENDER_CHEST);
    }

    private boolean isHoldingDefenseBlock(Minecraft mc) {
        return mc.player.getOffhandItem().is(Items.OBSIDIAN) ||
               mc.player.getOffhandItem().is(Items.CRYING_OBSIDIAN) ||
               mc.player.getOffhandItem().is(Items.ENDER_CHEST);
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("In Blockmitte zentrieren (Center)##sur", centerPlayer);
        ImGui.checkbox("GrimAC / AntiCheat Safe Pacing##sur", antiCheatMode);
        ImGui.checkbox("Silent Rotations##sur", silentRotations);
        ImGui.checkbox("Nur auf dem Boden aktiv##sur", onlyOnGround);
        ImGui.checkbox("Deaktivieren beim Springen##sur", disableOnJump);
        ImGui.sliderInt("Platzier-Verzoegerung (ms)##sur", delayMs.getData(), 20, 200);
        ImGui.spacing();
        ImGui.textDisabled("Baut automatisch einen 4-Block Obsidian-Bunker um die Fuesse.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("centerPlayer", centerPlayer.get());
        cfg.putSetting("disableOnJump", disableOnJump.get());
        cfg.putSetting("onlyOnGround", onlyOnGround.get());
        cfg.putSetting("antiCheatMode", antiCheatMode.get());
        cfg.putSetting("silentRotations", silentRotations.get());
        cfg.putSetting("delayMs", delayMs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        centerPlayer.set(cfg.getSetting("centerPlayer", centerPlayer.get()));
        disableOnJump.set(cfg.getSetting("disableOnJump", disableOnJump.get()));
        onlyOnGround.set(cfg.getSetting("onlyOnGround", onlyOnGround.get()));
        antiCheatMode.set(cfg.getSetting("antiCheatMode", antiCheatMode.get()));
        silentRotations.set(cfg.getSetting("silentRotations", silentRotations.get()));
        delayMs.set(cfg.getSetting("delayMs", delayMs.get()));
    }
}
