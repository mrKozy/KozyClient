// â˜­ Red Star Client â€” AutoToolModule
package com.redstarclient.module.inventory;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/** Automatically selects the best tool for the block being mined. */
public class AutoToolModule extends Module {

    private final ImBoolean respectDurability = new ImBoolean(true);
    private final ImInt     minDurability     = new ImInt(10);

    public AutoToolModule() {
        super("AutoTool", "Auto-selects the best tool for the targeted block.", ModuleCategory.INVENTORY, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (!mc.gameMode.isDestroying()) return;

        if (mc.hitResult == null || mc.hitResult.getType() != HitResult.Type.BLOCK) return;

        BlockPos pos = ((BlockHitResult) mc.hitResult).getBlockPos();
        BlockState state = mc.level.getBlockState(pos);

        int bestSlot = InventoryUtils.findBestToolForBlock(mc.player, state);

        if (respectDurability.get()) {
            var stack = mc.player.getInventory().getItem(bestSlot);
            if (stack.isDamageableItem() && stack.getMaxDamage() - stack.getDamageValue() < minDurability.get()) {
                return; // Don't use almost-broken tools
            }
        }

        mc.player.getInventory().setSelectedSlot(bestSlot);
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Respect Durability##at", respectDurability);
        ImGui.sliderInt("Min Durability##at", minDurability.getData(), 1, 50);
    }
}
