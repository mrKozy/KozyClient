// ☭ Red Star Client — ShieldDrainModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;

/**
 * ShieldDrain: Detects when enemy blocks with shield, switches to Axe,
 * attacks to break/stun shield, and returns to sword.
 */
public class ShieldDrainModule extends Module {

    private final ImFloat   range       = new ImFloat(3.8f);
    private final ImBoolean autoAttack  = new ImBoolean(true);
    private final ImBoolean swapBack    = new ImBoolean(true);

    private long lastDrainTime = 0L;

    public ShieldDrainModule() {
        super("ShieldDrain", "Erkennt gegnerische Schilde, wechselt zur Axt, zerbricht das Schild und wechselt zurueck.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastDrainTime < 500) return;

        Entity target = PlayerUtils.getClosestEntity(mc.player, range.get(), true);
        if (target instanceof LivingEntity enemy && enemy.isBlocking()) {
            int axeSlot = findAxeSlot(mc);
            if (axeSlot != -1) {
                lastDrainTime = now;
                int prev = mc.player.getInventory().selected;

                mc.player.getInventory().selected = axeSlot;

                if (autoAttack.get()) {
                    mc.gameMode.attack(mc.player, enemy);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }

                if (swapBack.get()) {
                    mc.player.getInventory().selected = prev;
                }
            }
        }
    }

    private int findAxeSlot(Minecraft mc) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof AxeItem) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Reichweite (Bloecke)##sd", range.getData(), 2.0f, 6.0f, "%.1fm");
        ImGui.checkbox("Automatisch Axt-Schlag ausfuehren##sd", autoAttack);
        ImGui.checkbox("Nach Schildbruch zurueckwechseln##sd", swapBack);
        ImGui.spacing();
        ImGui.textDisabled("Deaktiviert gegnerische Schilde fuer 5 Sekunden.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("autoAttack", autoAttack.get());
        cfg.putSetting("swapBack", swapBack.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        autoAttack.set(cfg.getSetting("autoAttack", autoAttack.get()));
        swapBack.set(cfg.getSetting("swapBack", swapBack.get()));
    }
}
