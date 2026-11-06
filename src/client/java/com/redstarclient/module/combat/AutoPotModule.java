// â˜­ Red Star Client â€” AutoPotModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SplashPotionItem;

/**
 * Pot & UHC PvP Master System:
 * - AutoPot: Automatically pitches down and throws Splash Healing / Regeneration potions when health drops
 * - Potion Refill: Restocks splash potions into hotbar from main inventory automatically
 */
public class AutoPotModule extends Module {

    private final ImFloat   healthThreshold = new ImFloat(12.0f); // Triggers at 6 hearts
    private final ImBoolean refillHotbar    = new ImBoolean(true);
    private final ImBoolean pitchDown       = new ImBoolean(true);

    private long lastPotTime = 0L;

    public AutoPotModule() {
        super("AutoPot", "Wirft automatisch Heilungs- und Buff-Traenke bei niedrigem Leben (Pot PvP / UHC).", ModuleCategory.POT_UHC, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();

        // 1. Auto Throw Splash Potion
        if (mc.player.getHealth() <= healthThreshold.get() && now - lastPotTime >= 650) {
            int potSlot = findSplashPotInHotbar(mc.player);
            if (potSlot != -1) {
                lastPotTime = now;
                int prev = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(potSlot);

                float prevPitch = mc.player.getXRot();
                if (pitchDown.get()) {
                    mc.player.setXRot(88.0f); // Throw directly at feet
                }

                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);

                if (pitchDown.get()) {
                    mc.player.setXRot(prevPitch);
                }

                mc.player.getInventory().setSelectedSlot(prev);
            }
        }

        // 2. Potion Refill from inventory into hotbar
        if (refillHotbar.get() && findSplashPotInHotbar(mc.player) == -1) {
            for (int i = 9; i < 36; i++) {
                ItemStack stack = mc.player.getInventory().getItem(i);
                if (!stack.isEmpty() && stack.getItem() instanceof SplashPotionItem) {
                    mc.gameMode.handleContainerInput(
                            mc.player.containerMenu.containerId,
                            i,
                            0,
                            ContainerInput.QUICK_MOVE,
                            mc.player
                    );
                    break;
                }
            }
        }
    }

    private int findSplashPotInHotbar(net.minecraft.client.player.LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof SplashPotionItem) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Gesundheits-Schwelle##pot", healthThreshold.getData(), 4.0f, 18.0f, "%.0f HP");
        ImGui.checkbox("Nach unten zielen (Fuesse)##pot", pitchDown);
        ImGui.checkbox("Potion Refill (Hotbar nachfuellen)##pot", refillHotbar);
        ImGui.spacing();
        ImGui.textDisabled("Wirft Traenke in Bruchteilen einer Sekunde fuer perfekte UHC & Nethpot Matches.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("healthThreshold", healthThreshold.get());
        cfg.putSetting("pitchDown", pitchDown.get());
        cfg.putSetting("refillHotbar", refillHotbar.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        healthThreshold.set(cfg.getSetting("healthThreshold", healthThreshold.get()));
        pitchDown.set(cfg.getSetting("pitchDown", pitchDown.get()));
        refillHotbar.set(cfg.getSetting("refillHotbar", refillHotbar.get()));
    }
}
