// ☭ Red Star Client — CartRefillModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * CartRefill: Restocks TNT Minecarts, normal carts, and rails into hotbar from inventory.
 */
public class CartRefillModule extends Module {

    private final ImInt refillDelayMs = new ImInt(150);
    private long lastRefillTime = 0L;

    public CartRefillModule() {
        super("CartRefill", "Cart PvP: Fuellt TNT-Minecarts, Schienen und Loren automatisch in die Hotbar nach.", ModuleCategory.CART, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefillTime < refillDelayMs.get()) return;

        // Check if TNT cart is missing in hotbar (0-8)
        boolean hasCartInHotbar = false;
        int emptyHotbarSlot = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) {
                emptyHotbarSlot = i;
            } else if (stack.is(Items.TNT_MINECART) || stack.is(Items.MINECART)) {
                hasCartInHotbar = true;
            }
        }

        if (!hasCartInHotbar && emptyHotbarSlot != -1) {
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack stack = mc.player.getInventory().getItem(invSlot);
                if (!stack.isEmpty() && (stack.is(Items.TNT_MINECART) || stack.is(Items.MINECART) || stack.is(Items.RAIL))) {
                    lastRefillTime = now;
                    mc.gameMode.handleContainerInput(
                            mc.player.containerMenu.containerId,
                            invSlot,
                            0,
                            ContainerInput.QUICK_MOVE,
                            mc.player
                    );
                    break;
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Nachfuell-Intervall (ms)##cr", refillDelayMs.getData(), 50, 500);
        ImGui.spacing();
        ImGui.textDisabled("Automatische Versorgung mit TNT-Minecarts und Schienen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("refillDelayMs", refillDelayMs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        refillDelayMs.set(cfg.getSetting("refillDelayMs", refillDelayMs.get()));
    }
}
