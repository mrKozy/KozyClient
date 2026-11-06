// â˜­ Red Star Client â€” AutoEatModule
package com.redstarclient.module.player;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;

/** Automatically eats food when the hunger bar drops below a threshold. */
public class AutoEatModule extends Module {

    private final ImInt     hungerThreshold    = new ImInt(14);
    private final ImBoolean preferGoldenApple  = new ImBoolean(false);

    private int  savedSlot  = -1;
    private boolean eating  = false;

    public AutoEatModule() {
        super("AutoEat", "Automatically eats food when hungry.", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        int hunger = mc.player.getFoodData().getFoodLevel();

        if (!eating && hunger < hungerThreshold.get()) {
            int foodSlot = InventoryUtils.findFoodInHotbar(mc.player);
            if (foodSlot == -1) foodSlot = InventoryUtils.findFoodInInventory(mc.player);
            if (foodSlot == -1) return;

            savedSlot = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(Math.min(foodSlot, 8));
            eating = true;
        }

        if (eating) {
            mc.options.keyUse.setDown(true);
            if (hunger >= 20 || !mc.player.getFoodData().needsFood()) {
                mc.options.keyUse.setDown(false);
                if (savedSlot >= 0) mc.player.getInventory().setSelectedSlot(savedSlot);
                eating = false;
            }
        }
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && eating) {
            mc.options.keyUse.setDown(false);
            if (savedSlot >= 0) mc.player.getInventory().setSelectedSlot(savedSlot);
            eating = false;
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Hunger Threshold##ae", hungerThreshold.getData(), 1, 20);
        ImGui.checkbox("Prefer Golden Apple##ae", preferGoldenApple);
        ImGui.textDisabled("Eats when hunger < threshold.");
    }
}
