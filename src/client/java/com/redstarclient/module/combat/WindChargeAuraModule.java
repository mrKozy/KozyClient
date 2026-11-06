// â˜­ Red Star Client â€” WindChargeAuraModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * WindChargeAura: Mace PvP height boost assistant.
 * Automatically casts wind charge downward when jumping to launch the player
 * high into the air for extreme Mace smash attacks.
 */
public class WindChargeAuraModule extends Module {

    private final ImBoolean autoJumpBoost = new ImBoolean(true);
    private final ImFloat   cooldownSec   = new ImFloat(1.5f);

    private long lastWindChargeTime = 0L;

    public WindChargeAuraModule() {
        super("WindChargeAura", "Mace PvP: Feuert Windkugeln auf den Boden beim Springen fuer extreme Sprunghoehe.", ModuleCategory.MACE, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastWindChargeTime < (long)(cooldownSec.get() * 1000f)) return;

        // Trigger when player is in the air right after jumping
        if (autoJumpBoost.get() && mc.options.keyJump.isDown() && !mc.player.onGround() && mc.player.getDeltaMovement().y > 0.05) {
            int chargeSlot = findWindChargeSlot(mc);
            if (chargeSlot != -1) {
                lastWindChargeTime = now;
                int prev = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(chargeSlot);

                float prevPitch = mc.player.getXRot();
                mc.player.setXRot(89.0f); // aim straight down

                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);

                mc.player.setXRot(prevPitch);
                mc.player.getInventory().setSelectedSlot(prev);
            }
        }
    }

    private int findWindChargeSlot(Minecraft mc) {
        if (mc.player == null) return -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(Items.WIND_CHARGE)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Automatisch beim Springen fuer Extra-Hoehe zÃ¼nden##wca", autoJumpBoost);
        ImGui.sliderFloat("Abklingzeit (Sekunden)##wca", cooldownSec.getData(), 0.5f, 5.0f, "%.1fs");
        ImGui.spacing();
        ImGui.textDisabled("Erzeugt sofortige vertikale Boost-Hoehe fuer 50+ Schaden Mace Smashes.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("autoJumpBoost", autoJumpBoost.get());
        cfg.putSetting("cooldownSec", cooldownSec.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        autoJumpBoost.set(cfg.getSetting("autoJumpBoost", autoJumpBoost.get()));
        cooldownSec.set(cfg.getSetting("cooldownSec", cooldownSec.get()));
    }
}
