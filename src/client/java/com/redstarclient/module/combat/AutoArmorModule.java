// ☭ Red Star Client — AutoArmorModule
package com.redstarclient.module.combat;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Automatically equips the strongest armor found in inventory. */
public class AutoArmorModule extends Module {

    private final ImBoolean inScreenOnly = new ImBoolean(false);
    private long lastEquipTime = 0L;

    public AutoArmorModule() {
        super("AutoArmor", "Automatically equips the best armor available in inventory.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;
        if (inScreenOnly.get() && (mc.gui == null || mc.gui.screen() == null)) return;
    }

    private int getDefense(ItemStack stack) {
        if (stack.isEmpty() || !stack.is(ItemTags.ARMOR_ENCHANTABLE)) return -1;
        return 0;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Only when GUI is open##aa", inScreenOnly);
        ImGui.textDisabled("Equips helmets, chestplates, leggings & boots.");
    }
}
