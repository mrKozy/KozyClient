// ☭ Red Star Client — ItemCounterModule
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Counts and displays critical competitive items in inventory:
 * Blocks (Wool/Concrete), Arrows, Ender Pearls, and Golden Apples.
 */
public class ItemCounterModule extends Module implements DraggableHudElement {

    private final ImInt     posX        = new ImInt(10);
    private final ImInt     posY        = new ImInt(220);
    private final ImBoolean countBlocks = new ImBoolean(true);
    private final ImBoolean countArrows = new ImBoolean(true);
    private final ImBoolean countPearls = new ImBoolean(true);
    private final ImBoolean countGaps   = new ImBoolean(true);

    private int lastWidth  = 120;
    private int lastHeight = 18;

    public ItemCounterModule() {
        super("ItemCounter", "Displays counts of blocks, arrows, pearls and gaps.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        Inventory inv = mc.player.getInventory();
        int blocks = 0;
        int arrows = 0;
        int pearls = 0;
        int gaps   = 0;

        for (int i = 0; i < 36; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            if (s.getItem() instanceof BlockItem) blocks += s.getCount();
            else if (s.is(Items.ARROW) || s.is(Items.SPECTRAL_ARROW) || s.is(Items.TIPPED_ARROW)) arrows += s.getCount();
            else if (s.is(Items.ENDER_PEARL)) pearls += s.getCount();
            else if (s.is(Items.GOLDEN_APPLE) || s.is(Items.ENCHANTED_GOLDEN_APPLE)) gaps += s.getCount();
        }

        StringBuilder sb = new StringBuilder();
        if (countBlocks.get()) sb.append("Blocks: ").append(blocks).append("  ");
        if (countArrows.get()) sb.append("Arrows: ").append(arrows).append("  ");
        if (countPearls.get()) sb.append("Pearls: ").append(pearls).append("  ");
        if (countGaps.get())   sb.append("Gapples: ").append(gaps);

        String text = sb.toString().trim();
        if (text.isEmpty()) text = "ItemCounter On";

        int x = posX.get();
        int y = posY.get();
        RenderUtils.drawTextWithBackground(graphics, text, x, y, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);

        this.lastWidth = mc.font.width(text) + 8;
        this.lastHeight = mc.font.lineHeight + 4;
    }

    @Override public String  getHudName()    { return "Item Counter"; }
    @Override public int     getHudX()       { return posX.get(); }
    @Override public int     getHudY()       { return posY.get(); }
    @Override public int     getHudWidth()   { return lastWidth; }
    @Override public int     getHudHeight()  { return lastHeight; }
    @Override public void    setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled()  { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Count Blocks##ic", countBlocks);
        ImGui.checkbox("Count Arrows##ic", countArrows);
        ImGui.checkbox("Count Pearls##ic", countPearls);
        ImGui.checkbox("Count Golden Apples##ic", countGaps);
        ImGui.sliderInt("Position X##ic", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##ic", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("countBlocks", countBlocks.get());
        cfg.putSetting("countArrows", countArrows.get());
        cfg.putSetting("countPearls", countPearls.get());
        cfg.putSetting("countGaps", countGaps.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        countBlocks.set(cfg.getSetting("countBlocks", countBlocks.get()));
        countArrows.set(cfg.getSetting("countArrows", countArrows.get()));
        countPearls.set(cfg.getSetting("countPearls", countPearls.get()));
        countGaps.set(cfg.getSetting("countGaps", countGaps.get()));
    }
}
