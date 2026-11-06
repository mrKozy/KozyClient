// ☭ Red Star Client — InventoryViewerModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Soviet Inventory Viewer HUD:
 * Displays a clean, reliable preview of the player's full inventory, hotbar,
 * armor slots, and offhand item with correct item counts and durability.
 * Fully draggable via the HUD editor.
 */
public class InventoryViewerModule extends Module implements DraggableHudElement {

    private final ImInt     posX           = new ImInt(16);
    private final ImInt     posY           = new ImInt(140);
    private final ImBoolean showHotbar     = new ImBoolean(true);
    private final ImBoolean showArmor      = new ImBoolean(true);
    private final ImBoolean showOffhand    = new ImBoolean(true);

    private int lastWidth  = 180;
    private int lastHeight = 90;

    public InventoryViewerModule() {
        super("InventoryViewer", "Zeigt ein stylisches Miniatur-Inventar mit Ruestung, Offhand und Hotbar auf dem HUD an.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        int slotSize = 18;
        int padding = 6;

        int armorCols = (showArmor.get() || showOffhand.get()) ? 1 : 0;
        int invCols = 9;
        int rows = 3 + (showHotbar.get() ? 1 : 0);

        int armorOffset = armorCols > 0 ? (slotSize + 4) : 0;
        int innerW = armorOffset + invCols * slotSize;
        int innerH = Math.max(rows * slotSize + (showHotbar.get() ? 4 : 0), (showOffhand.get() ? 5 : 4) * slotSize);

        int totalW = innerW + padding * 2;
        int totalH = innerH + padding * 2 + 14; // +14 for header

        int x = posX.get();
        int y = posY.get();

        // Keep on screen
        if (x + totalW > screenW) x = Math.max(2, screenW - totalW - 2);
        if (y + totalH > screenH) y = Math.max(2, screenH - totalH - 2);
        if (x < 2) x = 2;
        if (y < 2) y = 2;

        this.lastWidth = totalW;
        this.lastHeight = totalH;

        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);

        // 1. Soviet Glassmorphic Panel Background & Borders
        graphics.fill(0, 0, totalW, totalH, RenderUtils.toARGB(0.06f, 0.02f, 0.02f, 0.85f));
        graphics.fill(0, 0, totalW, 1, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.50f));
        graphics.fill(0, totalH - 1, totalW, totalH, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.35f));
        graphics.fill(0, 0, 1, totalH, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.50f));
        graphics.fill(totalW - 1, 0, totalW, totalH, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.35f));

        // Header Title
        String title = "★ INVENTAR ★";
        graphics.text(mc.font, title, padding + 2, padding - 1, RenderUtils.SOVIET_GOLD, false);

        int contentStartY = padding + 12;

        // 2. Render Armor & Offhand (Left Column)
        if (armorCols > 0) {
            EquipmentSlot[] armorSlots = {
                    EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
            };

            if (showArmor.get()) {
                for (int i = 0; i < 4; i++) {
                    int slotX = padding;
                    int slotY = contentStartY + i * slotSize;
                    drawSlotBox(graphics, slotX, slotY);

                    ItemStack armorPiece = mc.player.getItemBySlot(armorSlots[i]);
                    if (!armorPiece.isEmpty()) {
                        graphics.item(armorPiece, slotX + 1, slotY + 1);
                        graphics.itemDecorations(mc.font, armorPiece, slotX + 1, slotY + 1);
                    }
                }
            }

            // Offhand slot below armor
            if (showOffhand.get()) {
                int offX = padding;
                int offY = contentStartY + (showArmor.get() ? 4 : 0) * slotSize + (showArmor.get() ? 2 : 0);
                drawSlotBox(graphics, offX, offY);

                ItemStack offhand = mc.player.getOffhandItem();
                if (!offhand.isEmpty()) {
                    graphics.item(offhand, offX + 1, offY + 1);
                    graphics.itemDecorations(mc.font, offhand, offX + 1, offY + 1);
                }
            }
        }

        // 3. Render Main Inventory (Slots 9 to 35)
        int invStartX = padding + armorOffset;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                int slotIdx = 9 + row * 9 + col;
                int sx = invStartX + col * slotSize;
                int sy = contentStartY + row * slotSize;

                drawSlotBox(graphics, sx, sy);

                ItemStack stack = mc.player.getInventory().getItem(slotIdx);
                if (!stack.isEmpty()) {
                    graphics.item(stack, sx + 1, sy + 1);
                    graphics.itemDecorations(mc.font, stack, sx + 1, sy + 1);
                }
            }
        }

        // 4. Render Hotbar (Slots 0 to 8) below main inventory
        if (showHotbar.get()) {
            int hotbarY = contentStartY + 3 * slotSize + 3;
            graphics.fill(invStartX, hotbarY - 2, invStartX + 9 * slotSize, hotbarY - 1,
                    RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.40f));

            for (int col = 0; col < 9; col++) {
                int sx = invStartX + col * slotSize;
                int sy = hotbarY;

                boolean isSelected = (col == mc.player.getInventory().getSelectedSlot());
                drawSlotBoxHighlight(graphics, sx, sy, isSelected);

                ItemStack stack = mc.player.getInventory().getItem(col);
                if (!stack.isEmpty()) {
                    graphics.item(stack, sx + 1, sy + 1);
                    graphics.itemDecorations(mc.font, stack, sx + 1, sy + 1);
                }
            }
        }

        graphics.pose().popMatrix();
    }

    private void drawSlotBox(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x, y, x + 18, y + 18, RenderUtils.toARGB(0.12f, 0.04f, 0.04f, 0.70f));
        graphics.fill(x, y, x + 18, y + 1, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.25f));
        graphics.fill(x, y, x + 1, y + 18, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.25f));
        graphics.fill(x, y + 17, x + 18, y + 18, RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.40f));
        graphics.fill(x + 17, y, x + 18, y + 18, RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.40f));
    }

    private void drawSlotBoxHighlight(GuiGraphicsExtractor graphics, int x, int y, boolean selected) {
        int bgColor = selected
                ? RenderUtils.toARGB(0.60f, 0.10f, 0.10f, 0.90f)
                : RenderUtils.toARGB(0.12f, 0.04f, 0.04f, 0.70f);
        graphics.fill(x, y, x + 18, y + 18, bgColor);

        int borderColor = selected
                ? RenderUtils.toARGB(0.95f, 0.80f, 0.15f, 0.95f)
                : RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.25f);
        graphics.fill(x, y, x + 18, y + 1, borderColor);
        graphics.fill(x, y, x + 1, y + 18, borderColor);
        graphics.fill(x, y + 17, x + 18, y + 18, borderColor);
        graphics.fill(x + 17, y, x + 18, y + 18, borderColor);
    }

    @Override public String getHudName() { return "Inventory Viewer"; }
    @Override public int getHudX() { return posX.get(); }
    @Override public int getHudY() { return posY.get(); }
    @Override public int getHudWidth() { return lastWidth; }
    @Override public int getHudHeight() { return lastHeight; }
    @Override public void setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled() { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Hotbar unter Inventar anzeigen##iv", showHotbar);
        ImGui.checkbox("Ruestung anzeigen##iv", showArmor);
        ImGui.checkbox("Offhand anzeigen##iv", showOffhand);
        ImGui.sliderInt("Position X##iv", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##iv", posY.getData(), 0, 800);
        ImGui.spacing();
        ImGui.textDisabled("Verschiebbar im HUD-Editor (Button 'Edit HUD' im Menue).");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showHotbar", showHotbar.get());
        cfg.putSetting("showArmor", showArmor.get());
        cfg.putSetting("showOffhand", showOffhand.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showHotbar.set(cfg.getSetting("showHotbar", showHotbar.get()));
        showArmor.set(cfg.getSetting("showArmor", showArmor.get()));
        showOffhand.set(cfg.getSetting("showOffhand", showOffhand.get()));
    }
}
