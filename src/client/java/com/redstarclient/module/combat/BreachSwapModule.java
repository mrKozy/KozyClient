// â˜­ Red Star Client â€” BreachSwapModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * BreachSwap: Mace PvP weapon swapping.
 * Detects downward momentum and fall distance, automatically equips the Mace
 * before landing, and swaps back to primary weapon afterwards.
 */
public class BreachSwapModule extends Module {

    private final ImFloat   minFallDistance = new ImFloat(1.5f);
    private final ImBoolean swapBack        = new ImBoolean(true);

    private int     savedSlot = -1;
    private boolean swapped   = false;

    public BreachSwapModule() {
        super("BreachSwap", "Mace PvP: Wechselt beim Fallen automatisch auf den Streitkolben fuer maximalen Fall-Smash.", ModuleCategory.MACE, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        // Check if player is falling fast enough
        if (mc.player.fallDistance >= minFallDistance.get() && mc.player.getDeltaMovement().y < -0.15) {
            if (!swapped) {
                int maceSlot = findMaceSlot(mc);
                if (maceSlot != -1 && maceSlot != mc.player.getInventory().getSelectedSlot()) {
                    savedSlot = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(maceSlot);
                    swapped = true;
                }
            }
        } else if (mc.player.onGround() || mc.player.fallDistance <= 0.1f) {
            if (swapped) {
                if (swapBack.get() && savedSlot >= 0 && savedSlot < 9) {
                    mc.player.getInventory().setSelectedSlot(savedSlot);
                }
                savedSlot = -1;
                swapped = false;
            }
        }
    }

    private int findMaceSlot(Minecraft mc) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(Items.MACE)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null && swapped && savedSlot >= 0) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        swapped = false;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Minimale Fall-Distanz (Bloecke)##bs", minFallDistance.getData(), 0.5f, 6.0f, "%.1fm");
        ImGui.checkbox("Nach Landung zurueckwechseln##bs", swapBack);
        ImGui.spacing();
        ImGui.textDisabled("Aktiviert automatisch die Breach/Density Mace bei Fall-Angriffen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("minFallDistance", minFallDistance.get());
        cfg.putSetting("swapBack", swapBack.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        minFallDistance.set(cfg.getSetting("minFallDistance", minFallDistance.get()));
        swapBack.set(cfg.getSetting("swapBack", swapBack.get()));
    }
}
