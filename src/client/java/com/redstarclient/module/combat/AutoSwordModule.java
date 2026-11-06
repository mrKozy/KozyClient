// â˜­ Red Star Client â€” AutoSwordModule (from Wurst 7)
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;

/**
 * AutoSword (from Wurst 7):
 * Automatically selects the best weapon (Sword, Mace, or Axe) in your hotbar
 * whenever attacking or aiming at an enemy.
 */
public class AutoSwordModule extends Module {

    private final ImBoolean preferMace   = new ImBoolean(true);
    private final ImBoolean preferAxe    = new ImBoolean(false);
    private final ImBoolean checkTarget  = new ImBoolean(true);

    public AutoSwordModule() {
        super("AutoSword", "Waehlt automatisch das beste Schwert oder die staerkste Waffe in der Hotbar aus (Wurst 7).", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        if (checkTarget.get()) {
            Entity target = PlayerUtils.getClosestEntity(mc.player, 4.0, false);
            if (target instanceof LivingEntity) {
                equipBestWeapon(mc);
            }
        }
    }

    public void onAttack() {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            equipBestWeapon(mc);
        }
    }

    private void equipBestWeapon(Minecraft mc) {
        int bestSlot = -1;
        float bestScore = -1f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (stack.isEmpty()) continue;

            float score = 0f;
            if (stack.getItem() instanceof MaceItem && preferMace.get()) {
                score = 12.0f;
            } else if (stack.is(ItemTags.SWORDS)) {
                score = 8.0f + getMaterialBonus(stack);
            } else if (stack.getItem() instanceof AxeItem axe && preferAxe.get()) {
                score = 9.0f + getMaterialBonus(stack);
            }

            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot != -1 && mc.player.getInventory().getSelectedSlot() != bestSlot) {
            mc.player.getInventory().setSelectedSlot(bestSlot);
        }
    }

    private float getMaterialBonus(ItemStack stack) {
        String name = stack.getItem().toString().toLowerCase();
        if (name.contains("netherite")) return 3.0f;
        if (name.contains("diamond")) return 2.0f;
        if (name.contains("iron")) return 1.0f;
        return 0f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Mace bevorzugen (1.21 Streitkolben)##asw", preferMace);
        ImGui.checkbox("Axt bevorzugen (Schildbrecher)##asw", preferAxe);
        ImGui.checkbox("Automatisch wechseln bei Gegner-Sichtung##asw", checkTarget);
        ImGui.spacing();
        ImGui.textDisabled("Ruistet im Kampf immer automatisch deine toedlichste Waffe aus.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("preferMace", preferMace.get());
        cfg.putSetting("preferAxe", preferAxe.get());
        cfg.putSetting("checkTarget", checkTarget.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        preferMace.set(cfg.getSetting("preferMace", preferMace.get()));
        preferAxe.set(cfg.getSetting("preferAxe", preferAxe.get()));
        checkTarget.set(cfg.getSetting("checkTarget", checkTarget.get()));
    }
}
