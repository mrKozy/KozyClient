// ☭ Red Star Client — AutoShieldBreakerModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

/**
 * AutoShieldBreaker:
 * Automatically switches to an Axe when an opposing player raises their Shield,
 * breaks their guard instantly with an axe hit, and swaps back to sword.
 */
public class AutoShieldBreakerModule extends Module {

    private final ImBoolean autoSwapBack = new ImBoolean(true);

    public AutoShieldBreakerModule() {
        super("ShieldBreaker", "Wechselt automatisch zur Axt, wenn der Gegner blockt, um sein Schild zu deaktivieren.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        Entity target = PlayerUtils.getClosestEntity(mc.player, 4.0, true);
        if (target instanceof Player enemy && enemy.isBlocking()) {
            int axeSlot = findAxeInHotbar(mc.player);
            if (axeSlot != -1 && mc.player.getInventory().selected != axeSlot) {
                int prevSlot = mc.player.getInventory().selected;
                mc.player.getInventory().selected = axeSlot;

                mc.gameMode.attack(mc.player, enemy);
                mc.player.swing(InteractionHand.MAIN_HAND);

                if (autoSwapBack.get()) {
                    mc.player.getInventory().selected = prevSlot;
                }
            }
        }
    }

    private int findAxeInHotbar(net.minecraft.client.player.LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Automatisch zum Schwert zurueck##sb", autoSwapBack);
        ImGui.spacing();
        ImGui.textDisabled("Deaktiviert gegnerische Schilde fuer 5 Sekunden durch Axtschlag.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("autoSwapBack", autoSwapBack.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        autoSwapBack.set(cfg.getSetting("autoSwapBack", autoSwapBack.get()));
    }
}
