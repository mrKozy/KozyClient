// ☭ Red Star Client — FastDropModule (from Wurst 7)
package com.redstarclient.module.inventory;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;

/**
 * FastDrop (from Wurst 7):
 * Rapidly ejects designated items or all inventory slots with maximum speed.
 */
public class FastDropModule extends Module {

    private final ImBoolean dropHotbar = new ImBoolean(false);
    private final ImInt     delayMs    = new ImInt(35);

    private long lastDropTime = 0L;

    public FastDropModule() {
        super("FastDrop", "Wirft Gegenstaende aus dem Inventar mit maximaler Geschwindigkeit ab (Wurst 7).", ModuleCategory.INVENTORY, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastDropTime < delayMs.get()) return;

        int startSlot = dropHotbar.get() ? 0 : 9;
        boolean foundItem = false;

        for (int i = startSlot; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                foundItem = true;
                lastDropTime = now;

                // Button 1 in ContainerInput.THROW drops entire stack
                mc.gameMode.handleContainerInput(
                        0,
                        i,
                        1,
                        ContainerInput.THROW,
                        mc.player
                );
                return;
            }
        }

        if (!foundItem) {
            // All items cleared, turn off automatically
            setEnabled(false);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Hotbar-Plaetze ebenfalls abwerfen##fdrp", dropHotbar);
        ImGui.sliderInt("Abwurf-Verzoegerung (ms)##fdrp", delayMs.getData(), 10, 150);
        ImGui.spacing();
        ImGui.textDisabled("Leert dein Inventar in Sekundenschnelle und schaltet sich danach automatisch aus.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("dropHotbar", dropHotbar.get());
        cfg.putSetting("delayMs", delayMs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        dropHotbar.set(cfg.getSetting("dropHotbar", dropHotbar.get()));
        delayMs.set(cfg.getSetting("delayMs", delayMs.get()));
    }
}
