// â˜­ Red Star Client â€” ArmorHUDModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Displays armor durability and held item info on the HUD. Supports dragging. */
public class ArmorHUDModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(300);
    private final ImBoolean showEffects = new ImBoolean(true);

    private int lastWidth = 84;
    private int lastHeight = 110;

    public ArmorHUDModule() {
        super("ArmorHUD", "Shows armor durability and active effects on-screen.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        int x = posX.get();
        int y = posY.get();

        EquipmentSlot[] slots = { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
        String[] labels = { "Helm", "Chest", "Legs", "Boots" };

        int curY = y;
        int maxW = 84;

        for (int i = 0; i < slots.length; i++) {
            ItemStack stack = mc.player.getItemBySlot(slots[i]);
            if (stack.isEmpty()) continue;

            int maxDmg = stack.getMaxDamage();
            int curDmg = maxDmg - stack.getDamageValue();

            String text = labels[i] + ": " + curDmg + "/" + maxDmg;
            RenderUtils.drawTextWithBackground(graphics, text, x, curY, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
            RenderUtils.drawHealthBar(graphics, x, curY + 12, 70, curDmg, maxDmg);
            curY += 22;
        }

        ItemStack held = mc.player.getMainHandItem();
        if (!held.isEmpty() && held.isDamageableItem()) {
            int maxD = held.getMaxDamage();
            int curD = maxD - held.getDamageValue();
            String ht = "Hand: " + curD + "/" + maxD;
            RenderUtils.drawTextWithBackground(graphics, ht, x, curY, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
            RenderUtils.drawHealthBar(graphics, x, curY + 12, 70, curD, maxD);
            curY += 22;
        }

        if (showEffects.get()) {
            for (var effect : mc.player.getActiveEffects()) {
                String name = effect.getEffect().value().getDescriptionId();
                int dur = effect.getDuration() / 20;
                String txt = name.substring(name.lastIndexOf('.') + 1) + " " + dur + "s";
                maxW = Math.max(maxW, mc.font.width(txt) + 6);
                RenderUtils.drawTextWithBackground(graphics, txt, x, curY, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
                curY += 14;
            }
        }

        this.lastWidth = maxW;
        this.lastHeight = Math.max(curY - y, 24);
    }

    @Override
    public String getHudName() { return "Armor HUD"; }

    @Override
    public int getHudX() { return posX.get(); }

    @Override
    public int getHudY() { return posY.get(); }

    @Override
    public int getHudWidth() { return lastWidth; }

    @Override
    public int getHudHeight() { return lastHeight; }

    @Override
    public void setHudPos(int x, int y) {
        posX.set(x);
        posY.set(y);
    }

    @Override
    public boolean isHudEnabled() { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show Potion Effects##ah", showEffects);
        ImGui.sliderInt("Position X##ah", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##ah", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showEffects", showEffects.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showEffects.set(cfg.getSetting("showEffects", showEffects.get()));
    }
}
