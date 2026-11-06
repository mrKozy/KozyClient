// â˜­ Red Star Client â€” ArmorNotifierModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * ArmorNotifier:
 * Monitors equipped armor durability and displays a prominent warning banner
 * on HUD whenever any piece falls below the configured percentage threshold.
 */
public class ArmorNotifierModule extends Module {

    private final ImInt thresholdPercent = new ImInt(20);

    public ArmorNotifierModule() {
        super("ArmorNotifier", "Warnt mit rotem Alarm-Banner, wenn Ruestungsteile unter die Haltbarkeits-Schwelle fallen.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
        };

        String warningPiece = null;
        int lowestPercent = 100;

        for (EquipmentSlot slot : slots) {
            ItemStack stack = mc.player.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.isDamageableItem()) {
                int maxDamage = stack.getMaxDamage();
                int currentDamage = stack.getDamageValue();
                int remaining = maxDamage - currentDamage;
                int percent = (int) Math.round(((double) remaining / maxDamage) * 100.0);

                if (percent <= thresholdPercent.get() && percent < lowestPercent) {
                    lowestPercent = percent;
                    warningPiece = stack.getHoverName().getString();
                }
            }
        }

        if (warningPiece != null) {
            int screenW = mc.getWindow().getGuiScaledWidth();
            long time = System.currentTimeMillis();
            boolean flash = (time / 400) % 2 == 0;
            int textColor = flash ? RenderUtils.HOSTILE_RED : RenderUtils.SOVIET_GOLD;

            String alert = String.format("âš  WARNUNG: %s FAST ZERBROCHEN (%d%%) âš ", warningPiece.toUpperCase(), lowestPercent);
            int textW = mc.font.width(alert);
            int x = (screenW - textW) / 2;
            int y = 32;

            RenderUtils.drawTextWithBackground(graphics, alert, x, y, textColor, RenderUtils.BG_DARK_RED);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Haltbarkeits-Schwelle (%)##an", thresholdPercent.getData(), 5, 50);
        ImGui.spacing();
        ImGui.textDisabled("Blinkt bei niedriger Ruestung rot auf dem Bildschirm.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("thresholdPercent", thresholdPercent.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        thresholdPercent.set(cfg.getSetting("thresholdPercent", thresholdPercent.get()));
    }
}
