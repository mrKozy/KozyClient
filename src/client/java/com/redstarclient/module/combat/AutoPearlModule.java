// â˜­ Red Star Client â€” AutoPearlModule
package com.redstarclient.module.combat;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

/**
 * AutoPearl: Quick one-shot pearl throw macro.
 * Swaps to pearl, throws it immediately, and restores previous hotbar item.
 */
public class AutoPearlModule extends Module {

    private final ImBoolean swapBack = new ImBoolean(true);

    public AutoPearlModule() {
        super("AutoPearl", "Wirft blitzschnell eine Enderperle aus der Hotbar und wechselt sofort zurueck.", ModuleCategory.COMBAT, GLFW.GLFW_KEY_V);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gameMode == null) {
            setEnabled(false);
            return;
        }

        int pearlSlot = findPearlSlot(mc);
        if (pearlSlot != -1) {
            int prev = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(pearlSlot);

            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);

            if (swapBack.get()) {
                mc.player.getInventory().setSelectedSlot(prev);
            }
        }

        // Instant macro: auto-disable after execution
        this.enabled = false;
    }

    private int findPearlSlot(Minecraft mc) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(Items.ENDER_PEARL)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Sofort zum vorherigen Slot zurueckwechseln##ap", swapBack);
        ImGui.spacing();
        ImGui.textDisabled("Hotkey V (anpassbar) druecken fuer Instant-Perle.");
    }
}
