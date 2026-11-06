// â˜­ Red Star Client â€” AutoGapModule
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
 * AutoGap: Automatically consumes Golden Apples or Enchanted Golden Apples
 * when player health drops below threshold, and seamlessly restores previous slot.
 */
public class AutoGapModule extends Module {

    private final ImFloat   healthThreshold = new ImFloat(13.0f);
    private final ImBoolean preferEnchanted = new ImBoolean(true);

    private int     savedSlot = -1;
    private boolean isEating  = false;
    private long    startEatTime = 0L;

    public AutoGapModule() {
        super("AutoGap", "Isst automatisch Goldaepfel bei niedrigem Leben und wechselt sofort zurueck.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        float hp = mc.player.getHealth();

        if (!isEating) {
            if (hp <= healthThreshold.get()) {
                int gapSlot = findGoldenAppleSlot(mc);
                if (gapSlot != -1) {
                    savedSlot = mc.player.getInventory().getSelectedSlot();
                    mc.player.getInventory().setSelectedSlot(gapSlot);
                    isEating = true;
                    startEatTime = System.currentTimeMillis();
                    mc.options.keyUse.setDown(true);
                }
            }
        } else {
            // Keep using item
            mc.options.keyUse.setDown(true);

            // Check if eaten or cancelled or timed out
            boolean hasAppleInHand = isGoldenApple(mc.player.getMainHandItem());
            if (!hasAppleInHand || hp >= 19.5f || (System.currentTimeMillis() - startEatTime > 2000)) {
                stopEating(mc);
            }
        }
    }

    private void stopEating(Minecraft mc) {
        if (mc.options != null && mc.options.keyUse != null) {
            mc.options.keyUse.setDown(false);
        }
        if (mc.player != null && savedSlot >= 0 && savedSlot < 9) {
            mc.player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        isEating = false;
    }

    private int findGoldenAppleSlot(Minecraft mc) {
        if (mc.player == null) return -1;
        int egap = -1;
        int regular = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                if (stack.is(Items.ENCHANTED_GOLDEN_APPLE)) egap = i;
                else if (stack.is(Items.GOLDEN_APPLE)) regular = i;
            }
        }

        if (preferEnchanted.get() && egap != -1) return egap;
        if (regular != -1) return regular;
        return egap;
    }

    private boolean isGoldenApple(ItemStack stack) {
        return !stack.isEmpty() && (stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE));
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            stopEating(mc);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Gesundheits-Schwelle (HP)##ag", healthThreshold.getData(), 4.0f, 19.0f, "%.1f HP");
        ImGui.checkbox("Bevorzuge verzauberte Goldaepfel (OP-Gapples)##ag", preferEnchanted);
        ImGui.spacing();
        ImGui.textDisabled("Isst automatisch und wechselt nahtlos zur Waffe zurueck.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("healthThreshold", healthThreshold.get());
        cfg.putSetting("preferEnchanted", preferEnchanted.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        healthThreshold.set(cfg.getSetting("healthThreshold", healthThreshold.get()));
        preferEnchanted.set(cfg.getSetting("preferEnchanted", preferEnchanted.get()));
    }
}
