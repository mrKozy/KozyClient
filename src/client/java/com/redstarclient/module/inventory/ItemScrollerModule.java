// ☭ Red Star Client — ItemScrollerModule
package com.redstarclient.module.inventory;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;

/** Allows fast item movement with scroll wheel in container screens. */
public class ItemScrollerModule extends Module {

    private final ImBoolean shiftForStack = new ImBoolean(true);
    private final ImBoolean ctrlForAll    = new ImBoolean(true);

    public ItemScrollerModule() {
        super("ItemScroller", "Fast item moving with scroll wheel in inventories.", ModuleCategory.INVENTORY, -1);
    }

    public boolean isShiftForStack() { return shiftForStack.get(); }
    public boolean isCtrlForAll()    { return ctrlForAll.get(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Shift+Scroll = Move Stack##is", shiftForStack);
        ImGui.checkbox("Ctrl+Scroll = Move All Same##is", ctrlForAll);
        ImGui.textDisabled("Scroll over items in container screens.");
    }
}
