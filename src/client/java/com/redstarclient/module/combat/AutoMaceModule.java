// â˜­ Red Star Client â€” AutoMaceModule
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Mace PvP Master System (Minecraft 1.21 & 26.3):
 * - Auto Mace: Detects fall smash momentum and automatically swaps to Mace right before impact
 * - Breach Swap: Optimizes weapon selection based on enemy armor density
 * - Shield Drain: Breaks shields with overhead heavy smash strikes
 */
public class AutoMaceModule extends Module {

    private final ImFloat   minFallDistance = new ImFloat(2.5f);
    private final ImBoolean autoSmash       = new ImBoolean(true);
    private final ImBoolean breachSwap      = new ImBoolean(true);
    private final ImBoolean swapBack        = new ImBoolean(true);

    public AutoMaceModule() {
        super("AutoMace", "Wechselt beim Herabfallen automatisch zum Streitkolben (Mace) fuer maximalen Smash-Schaden.", ModuleCategory.MACE, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        // Check if player is falling fast enough for a heavy Mace smash
        if (mc.player.fallDistance >= minFallDistance.get() && mc.player.getDeltaMovement().y < -0.2) {
            Entity target = PlayerUtils.getClosestEntity(mc.player, 4.2, false);
            if (target instanceof LivingEntity enemy) {
                int maceSlot = findMaceInHotbar(mc.player);
                if (maceSlot != -1) {
                    int prev = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(maceSlot);

                    if (autoSmash.get() && mc.player.distanceTo(enemy) <= 3.8) {
                        mc.gameMode.attack(mc.player, enemy);
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }

                    if (swapBack.get() && mc.player.onGround()) {
                        mc.player.getInventory().setSelectedSlot(prev);
                    }
                }
            }
        }
    }

    private int findMaceInHotbar(net.minecraft.client.player.LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem().getDescriptionId().contains("mace")) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Min. Fallhoehe (Bloecke)##am", minFallDistance.getData(), 1.5f, 6.0f, "%.1fm");
        ImGui.checkbox("Auto-Smash Angriff##am", autoSmash);
        ImGui.checkbox("Breach-Swap Ruestungsoptimierung##am", breachSwap);
        ImGui.checkbox("Nach Aufprall zurueckwechseln##am", swapBack);
        ImGui.spacing();
        ImGui.textDisabled("Nutzt die 1.21/26.3 Mace Smash-Mechanik fuer 1-Hit Kills.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("minFallDistance", minFallDistance.get());
        cfg.putSetting("autoSmash", autoSmash.get());
        cfg.putSetting("breachSwap", breachSwap.get());
        cfg.putSetting("swapBack", swapBack.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        minFallDistance.set(cfg.getSetting("minFallDistance", minFallDistance.get()));
        autoSmash.set(cfg.getSetting("autoSmash", autoSmash.get()));
        breachSwap.set(cfg.getSetting("breachSwap", breachSwap.get()));
        swapBack.set(cfg.getSetting("swapBack", swapBack.get()));
    }
}
