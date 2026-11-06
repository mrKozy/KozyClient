// ☭ Red Star Client — PotionRefillModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SplashPotionItem;

/**
 * PotionRefill: Automatically restocks splash potions from main inventory into hotbar.
 */
public class PotionRefillModule extends Module {

    private final ImInt refillDelayMs = new ImInt(180);
    private long lastRefillTime = 0L;

    public PotionRefillModule() {
        super("PotionRefill", "Pot & UHC: Fuellt Wurftraenke automatisch aus dem Rucksack in leere Hotbar-Plaetze nach.", ModuleCategory.POT_UHC, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastRefillTime < refillDelayMs.get()) return;

        // Check if there is space in hotbar (0-8)
        int emptyHotbarSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getItem(i).isEmpty()) {
                emptyHotbarSlot = i;
                break;
            }
        }

        // If hotbar has an empty slot or no splash pot
        if (emptyHotbarSlot != -1) {
            // Find splash pot in inventory (9-35)
            for (int invSlot = 9; invSlot < 36; invSlot++) {
                ItemStack stack = mc.player.getInventory().getItem(invSlot);
                if (!stack.isEmpty() && stack.getItem() instanceof SplashPotionItem) {
                    lastRefillTime = now;
                    // Move into hotbar via quick move
                    mc.gameMode.handleInventoryMouseClick(
                            mc.player.containerMenu.containerId,
                            invSlot,
                            0,
                            ClickType.QUICK_MOVE,
                            mc.player
                    );
                    break;
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Nachfuell-Verzoegerung (ms)##pr", refillDelayMs.getData(), 50, 600);
        ImGui.spacing();
        ImGui.textDisabled("Haltet die Hotbar im Pot-PvP immer voll mit Wurftraenken.");
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
