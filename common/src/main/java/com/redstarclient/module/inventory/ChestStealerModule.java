// ☭ Red Star Client — ChestStealerModule
package com.redstarclient.module.inventory;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;

/** Automatically loots all items from opened chests into player inventory. */
public class ChestStealerModule extends Module {

    private final ImInt delayMs = new ImInt(50);
    private final ImBoolean autoClose = new ImBoolean(true);

    private long lastTakeTime = 0L;

    public ChestStealerModule() {
        super("ChestStealer", "Automatically loots containers at high speed.", ModuleCategory.INVENTORY, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        if (!(mc.screen instanceof AbstractContainerScreen<?> screen)) return;
        if (!(screen.getMenu() instanceof ChestMenu chestMenu)) return;

        long now = System.currentTimeMillis();
        if (now - lastTakeTime < delayMs.get()) return;

        int numRows = chestMenu.getRowCount();
        int chestSlots = numRows * 9;
        boolean empty = true;

        for (int i = 0; i < chestSlots; i++) {
            ItemStack stack = chestMenu.getSlot(i).getItem();
            if (!stack.isEmpty()) {
                empty = false;
                mc.gameMode.handleInventoryMouseClick(
                        chestMenu.containerId,
                        i,
                        0,
                        ClickType.QUICK_MOVE,
                        mc.player
                );
                lastTakeTime = now;
                return;
            }
        }

        if (empty && autoClose.get()) {
            mc.player.closeContainer();
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Delay (ms)##cs", delayMs.getData(), 10, 200);
        ImGui.checkbox("Auto-Close Chest##cs", autoClose);
    }
}
