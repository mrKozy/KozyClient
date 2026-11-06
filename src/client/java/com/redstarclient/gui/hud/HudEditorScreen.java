// ☭ Red Star Client — HudEditorScreen
package com.redstarclient.gui.hud;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.Module;
import com.redstarclient.render.RenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Drag-and-Drop HUD Position Editor.
 * Features clean vector square pencil badges and smooth dragging bounds.
 */
public class HudEditorScreen extends Screen {

    private DraggableHudElement draggingElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen() {
        super(Component.literal("HUD Layout Editor"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private List<DraggableHudElement> getHudElements() {
        List<DraggableHudElement> list = new ArrayList<>();
        for (Module m : RedStarClient.MODULES.getAll()) {
            if (m instanceof DraggableHudElement elem && elem.isHudEnabled()) {
                list.add(elem);
            }
        }
        return list;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Draw subtle dark background overlay
        graphics.fill(0, 0, width, height, RenderUtils.toARGB(0.04f, 0.01f, 0.01f, 0.45f));

        Font font = this.font;

        // 2. Draw Header Banner with clean square pencil icon
        String title = "RED STAR CLIENT — HUD LAYOUT EDITOR";
        String subtitle = "Left-Click & Drag to reposition elements  |  Right-Click or ESC to Save & Exit";

        int tw = font.width(title);
        int sw = font.width(subtitle);
        int boxW = Math.max(tw, sw) + 56;
        int boxX = (width - boxW) / 2;

        graphics.fill(boxX, 10, boxX + boxW, 40, RenderUtils.toARGB(0.08f, 0.02f, 0.02f, 0.92f));
        graphics.fill(boxX, 10, boxX + boxW, 12, RenderUtils.SOVIET_GOLD);
        graphics.fill(boxX, 39, boxX + boxW, 40, RenderUtils.SOVIET_RED);
        graphics.fill(boxX, 10, boxX + 1, 40, RenderUtils.SOVIET_RED);
        graphics.fill(boxX + boxW - 1, 10, boxX + boxW, 40, RenderUtils.SOVIET_RED);

        // Draw crisp square with pencil icon
        int iconX = boxX + 10;
        int iconY = 16;
        graphics.fill(iconX, iconY, iconX + 16, iconY + 16, RenderUtils.SOVIET_RED);
        graphics.fill(iconX + 1, iconY + 1, iconX + 15, iconY + 15, RenderUtils.toARGB(0.12f, 0.03f, 0.03f, 0.95f));
        // Pencil diagonal body
        graphics.fill(iconX + 10, iconY + 3, iconX + 13, iconY + 6, RenderUtils.SOVIET_GOLD);
        graphics.fill(iconX + 7, iconY + 6, iconX + 11, iconY + 10, RenderUtils.SOVIET_GOLD);
        graphics.fill(iconX + 4, iconY + 9, iconX + 8, iconY + 12, RenderUtils.WHITE);
        graphics.fill(iconX + 3, iconY + 12, iconX + 5, iconY + 13, RenderUtils.SOVIET_RED);

        graphics.text(font, title, boxX + 34, 15, RenderUtils.SOVIET_GOLD, false);
        graphics.text(font, subtitle, boxX + 34, 26, RenderUtils.toARGB(0.85f, 0.85f, 0.85f, 1f), false);

        // 3. Render and handle each HUD element bounding box
        List<DraggableHudElement> elements = getHudElements();

        for (DraggableHudElement elem : elements) {
            int x = elem.getHudX();
            int y = elem.getHudY();
            int w = elem.getHudWidth();
            int h = elem.getHudHeight();

            boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
            boolean active = (draggingElement == elem);

            int borderColor = active ? RenderUtils.toARGB(1.0f, 1.0f, 0.3f, 1.0f)
                            : hovered ? RenderUtils.SOVIET_GOLD
                            : RenderUtils.toARGB(0.72f, 0.05f, 0.05f, 0.85f);

            int bgColor = active ? RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.25f)
                        : hovered ? RenderUtils.toARGB(0.72f, 0.05f, 0.05f, 0.30f)
                        : RenderUtils.toARGB(0.05f, 0.02f, 0.02f, 0.20f);

            // Fill area
            graphics.fill(x, y, x + w, y + h, bgColor);

            // Draw border box
            graphics.fill(x, y, x + w, y + 1, borderColor);
            graphics.fill(x, y + h - 1, x + w, y + h, borderColor);
            graphics.fill(x, y + 1, x + 1, y + h, borderColor);
            graphics.fill(x + w - 1, y, x + w, y + h, borderColor);

            // Badge / Label with small square icon
            String label = elem.getHudName();
            int lw = font.width(label) + 14;
            int badgeY = Math.max(y - 12, 2);
            graphics.fill(x, badgeY, x + lw + 4, badgeY + 12, RenderUtils.toARGB(0.1f, 0.02f, 0.02f, 0.95f));
            graphics.fill(x, badgeY, x + lw + 4, badgeY + 1, borderColor);
            // Small square icon
            graphics.fill(x + 3, badgeY + 3, x + 9, badgeY + 9, borderColor);
            graphics.text(font, label, x + 12, badgeY + 2, hovered ? RenderUtils.SOVIET_GOLD : RenderUtils.WHITE, false);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        // Right Click: exit and save
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.onClose();
            return true;
        }

        // Left Click: start dragging
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            List<DraggableHudElement> elements = getHudElements();
            for (DraggableHudElement elem : elements) {
                int x = elem.getHudX();
                int y = elem.getHudY();
                int w = elem.getHudWidth();
                int h = elem.getHudHeight();

                if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h) {
                    draggingElement = elem;
                    dragOffsetX = (int)mouseX - x;
                    dragOffsetY = (int)mouseY - y;
                    return true;
                }
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        int button = event.button();
        double mouseX = event.x();
        double mouseY = event.y();

        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingElement != null) {
            int newX = (int)mouseX - dragOffsetX;
            int newY = (int)mouseY - dragOffsetY;

            // Clamp inside screen bounds
            newX = Math.max(2, Math.min(newX, width - draggingElement.getHudWidth() - 2));
            newY = Math.max(2, Math.min(newY, height - draggingElement.getHudHeight() - 2));

            draggingElement.setHudPos(newX, newY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        int button = event.button();
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingElement != null) {
            draggingElement = null;
            RedStarClient.CONFIG.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        RedStarClient.CONFIG.save();
        super.onClose();
    }
}
