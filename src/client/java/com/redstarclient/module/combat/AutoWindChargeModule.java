// â˜­ Red Star Client â€” AutoWindChargeModule
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
import net.minecraft.world.item.ItemStack;

/**
 * Wind Charge PvP System (Short Wind Charge & Wind Charge Aura):
 * Automatically throws Wind Charges downwards to catapult you high into the air
 * for instant Mace smash combos, or pushes enemies away.
 */
public class AutoWindChargeModule extends Module {

    private final ImBoolean shortChargeOnJump = new ImBoolean(true);
    private final ImBoolean windChargeAura    = new ImBoolean(false);
    private final ImFloat   auraRange         = new ImFloat(6.0f);

    private long lastChargeTime = 0L;

    public AutoWindChargeModule() {
        super("AutoWindCharge", "Wirft Wind Charges nach unten fuer instant Hoehe (Mace-Combos) oder stoesst Gegner zurueck.", ModuleCategory.MACE, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastChargeTime < 400) return;

        int chargeSlot = findWindChargeInHotbar(mc.player);
        if (chargeSlot == -1) return;

        // 1. Short Wind Charge on Jump (Catapult combo)
        if (shortChargeOnJump.get() && mc.options.keyJump.isDown() && mc.player.onGround()) {
            lastChargeTime = now;
            int prev = mc.player.getInventory().getSelectedSlot();
            mc.player.getInventory().setSelectedSlot(chargeSlot);

            // Pitch straight down
            float prevPitch = mc.player.getXRot();
            mc.player.setXRot(90.0f);

            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);

            mc.player.setXRot(prevPitch);
            mc.player.getInventory().setSelectedSlot(prev);
            return;
        }

        // 2. Wind Charge Aura (shoot enemies off edges)
        if (windChargeAura.get()) {
            Entity target = PlayerUtils.getClosestEntity(mc.player, auraRange.get(), true);
            if (target != null) {
                lastChargeTime = now;
                int prev = mc.player.getInventory().getSelectedSlot();
                mc.player.getInventory().setSelectedSlot(chargeSlot);

                float[] rots = PlayerUtils.getRotationsToEntity(mc.player, target);
                float prevYaw = mc.player.getYRot();
                float prevPitch = mc.player.getXRot();

                mc.player.setYRot(rots[0]);
                mc.player.setXRot(rots[1]);

                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.swing(InteractionHand.MAIN_HAND);

                mc.player.setYRot(prevYaw);
                mc.player.setXRot(prevPitch);
                mc.player.getInventory().setSelectedSlot(prev);
            }
        }
    }

    private int findWindChargeInHotbar(net.minecraft.client.player.LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem().getDescriptionId().contains("wind_charge")) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Short Wind Charge bei Sprung (Katapult)##wc", shortChargeOnJump);
        ImGui.checkbox("Wind Charge Aura (Gegner wegpusten)##wc", windChargeAura);
        if (windChargeAura.get()) {
            ImGui.sliderFloat("Aura Reichweite##wc", auraRange.getData(), 3.0f, 10.0f, "%.1fm");
        }
        ImGui.spacing();
        ImGui.textDisabled("Katapultiert dich 8 Bloecke hoch fuer perfekte Mace-Combos.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("shortChargeOnJump", shortChargeOnJump.get());
        cfg.putSetting("windChargeAura", windChargeAura.get());
        cfg.putSetting("auraRange", auraRange.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        shortChargeOnJump.set(cfg.getSetting("shortChargeOnJump", shortChargeOnJump.get()));
        windChargeAura.set(cfg.getSetting("windChargeAura", windChargeAura.get()));
        auraRange.set(cfg.getSetting("auraRange", auraRange.get()));
    }
}
