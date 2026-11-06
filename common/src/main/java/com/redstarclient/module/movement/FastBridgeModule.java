// ☭ Red Star Client — FastBridgeModule (Eagle / Godbridge)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.BlockUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

/**
 * FastBridge (Eagle / Legit Godbridge):
 * Automatically presses Sneak right as the player reaches the block edge,
 * and releases Sneak as soon as a solid block is underneath.
 * Allows effortless, legit-looking fast bridging.
 */
public class FastBridgeModule extends Module {

    private final ImBoolean onlyLookingDown   = new ImBoolean(true);
    private final ImBoolean onlyHoldingBlocks = new ImBoolean(true);

    private boolean wasSneaked = false;

    public FastBridgeModule() {
        super("FastBridge", "Legit Eagle/FastBridge: Duckt automatisch an Blockkanten fuer superschnelles Brueckenbauen ohne Absturz.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        // Check if player is on ground
        if (!mc.player.onGround()) {
            releaseSneak(mc);
            return;
        }

        // Optional check: only when looking down (pitch >= 45 degrees)
        if (onlyLookingDown.get() && mc.player.getXRot() < 45.0f) {
            releaseSneak(mc);
            return;
        }

        // Optional check: only when holding placeable blocks in main hand
        if (onlyHoldingBlocks.get()) {
            ItemStack held = mc.player.getMainHandItem();
            if (held.isEmpty() || !(held.getItem() instanceof BlockItem)) {
                releaseSneak(mc);
                return;
            }
        }

        // Check block directly under player's feet
        BlockPos below = BlockUtils.getBlockBelow(mc.player);
        boolean isAirOrReplaceable = BlockUtils.isReplaceable(mc.level.getBlockState(below));

        if (isAirOrReplaceable) {
            mc.options.keyShift.setDown(true);
            wasSneaked = true;
        } else {
            if (wasSneaked) {
                mc.options.keyShift.setDown(false);
                wasSneaked = false;
            }
        }
    }

    private void releaseSneak(Minecraft mc) {
        if (wasSneaked) {
            if (mc.options != null && mc.options.keyShift != null) {
                mc.options.keyShift.setDown(false);
            }
            wasSneaked = false;
        }
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            releaseSneak(mc);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Nur beim Nach-Unten-Schauen aktivieren (> 45° Pitch)##fb", onlyLookingDown);
        ImGui.checkbox("Nur aktivieren wenn Bloecke in der Hand gehalten werden##fb", onlyHoldingBlocks);
        ImGui.spacing();
        ImGui.textDisabled("Perfektes Eagle/Godbridge: Tastatur-Sneak wird an jeder Blockkante automatisch gesetzt.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("onlyLookingDown", onlyLookingDown.get());
        cfg.putSetting("onlyHoldingBlocks", onlyHoldingBlocks.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        onlyLookingDown.set(cfg.getSetting("onlyLookingDown", onlyLookingDown.get()));
        onlyHoldingBlocks.set(cfg.getSetting("onlyHoldingBlocks", onlyHoldingBlocks.get()));
    }
}
