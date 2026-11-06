// ☭ Red Star Client — AutoElytraModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

/**
 * AutoElytra:
 * Instantly swaps chestplate for Elytra when falling, double-jumping,
 * or using firework rockets, and equips armor back on landing.
 */
public class AutoElytraModule extends Module {

    private final ImFloat   fallDistanceTrigger = new ImFloat(3.0f);
    private final ImBoolean autoDeploy          = new ImBoolean(true);
    private final ImBoolean swapBackOnGround    = new ImBoolean(true);

    public AutoElytraModule() {
        super("AutoElytra", "Ruestet automatisch Elytra beim Fallen aus und wechselt am Boden zurueck.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        boolean wearingElytra = mc.player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA);

        // Falling or flying
        if (!mc.player.onGround() && mc.player.fallDistance >= fallDistanceTrigger.get() && !wearingElytra) {
            int elytraSlot = InventoryUtils.findItemInInventory(mc.player, Items.ELYTRA);
            if (elytraSlot != -1) {
                equipChestSlot(mc, elytraSlot);
                if (autoDeploy.get() && mc.getConnection() != null) {
                    mc.player.startFallFlying();
                }
            }
        } else if (mc.player.onGround() && wearingElytra && swapBackOnGround.get()) {
            // Swap back to Netherite/Diamond chestplate
            int chestSlot = InventoryUtils.findItemInInventory(mc.player, Items.NETHERITE_CHESTPLATE);
            if (chestSlot == -1) chestSlot = InventoryUtils.findItemInInventory(mc.player, Items.DIAMOND_CHESTPLATE);
            if (chestSlot != -1) {
                equipChestSlot(mc, chestSlot);
            }
        }
    }

    private void equipChestSlot(Minecraft mc, int invSlot) {
        final int CHEST_SLOT = 6; // Armor chest slot in InventoryMenu
        int windowSlot = invSlot < 9 ? invSlot + 36 : invSlot;

        // Quick swap using ClickType.SWAP
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                windowSlot,
                0,
                ClickType.PICKUP,
                mc.player
        );
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                CHEST_SLOT,
                0,
                ClickType.PICKUP,
                mc.player
        );
        mc.gameMode.handleInventoryMouseClick(
                mc.player.containerMenu.containerId,
                windowSlot,
                0,
                ClickType.PICKUP,
                mc.player
        );
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Fall-Distanz Ausloeser##ae", fallDistanceTrigger.getData(), 1.0f, 10.0f, "%.1fm");
        ImGui.checkbox("Automatischer Gleitstart##ae", autoDeploy);
        ImGui.checkbox("Am Boden zu Brustpanzer zurueck##ae", swapBackOnGround);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("fallDistanceTrigger", fallDistanceTrigger.get());
        cfg.putSetting("autoDeploy", autoDeploy.get());
        cfg.putSetting("swapBackOnGround", swapBackOnGround.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        fallDistanceTrigger.set(cfg.getSetting("fallDistanceTrigger", fallDistanceTrigger.get()));
        autoDeploy.set(cfg.getSetting("autoDeploy", autoDeploy.get()));
        swapBackOnGround.set(cfg.getSetting("swapBackOnGround", swapBackOnGround.get()));
    }
}
