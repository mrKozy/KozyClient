// ☭ Red Star Client — HudEditorScreen
package com.redstarclient.gui.hud;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.Module;
import com.redstarclient.render.RenderUtils;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Interactive Screen for dragging and repositioning HUD elements on-screen.
 * - Left Click and Drag: Move HUD element
 * - Right Click or ESC: Exit and save layout
 */
public class HudEditorScreen extends Screen {

    private DraggableHudElement draggingElement = null;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public HudEditorScreen() {
        super(Component.literal("☭ HUD Editor"));
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Draw subtle dark background overlay
        graphics.fill(0, 0, width, height, RenderUtils.toARGB(0.04f, 0.01f, 0.01f, 0.45f));

        Font font = this.font;

        // 2. Draw Header Banner
        String title = "☭ RED STAR CLIENT — HUD LAYOUT EDITOR";
        String subtitle = "Left-Click & Drag to reposition elements  |  Right-Click or ESC to Save & Exit";

        int tw = font.width(title);
        int sw = font.width(subtitle);

        graphics.fill((width - tw) / 2 - 12, 10, (width + tw) / 2 + 12, 38, RenderUtils.toARGB(0.08f, 0.02f, 0.02f, 0.90f));
        graphics.fill((width - tw) / 2 - 12, 10, (width + tw) / 2 + 12, 12, RenderUtils.SOVIET_GOLD);
        graphics.drawString(font, title, (width - tw) / 2, 16, RenderUtils.SOVIET_GOLD, false);
        graphics.drawString(font, subtitle, (width - sw) / 2, 27, RenderUtils.toARGB(0.9f, 0.9f, 0.9f, 1f), false);

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
            graphics.fill(x, y, x + 1, y + h, borderColor);
            graphics.fill(x + w - 1, y, x + w, y + h, borderColor);

            // Badge / Label
            String label = "☭ " + elem.getHudName();
            int lw = font.width(label);
            int badgeY = Math.max(y - 10, 2);
            graphics.fill(x, badgeY, x + lw + 4, badgeY + 10, RenderUtils.toARGB(0.1f, 0.02f, 0.02f, 0.95f));
            graphics.fill(x, badgeY, x + lw + 4, badgeY + 1, borderColor);
            graphics.drawString(font, label, x + 2, badgeY + 1, hovered ? RenderUtils.SOVIET_GOLD : RenderUtils.WHITE, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingElement != null) {
            int newX = (int)mouseX - dragOffsetX;
            int newY = (int)mouseY - dragOffsetY;

            // Clamp inside screen bounds
            newX = Math.max(2, Math.min(newX, width - draggingElement.getHudWidth() - 2));
            newY = Math.max(2, Math.min(newY, height - draggingElement.getHudHeight() - 2));

            draggingElement.setHudPos(newX, newY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT && draggingElement != null) {
            draggingElement = null;
            RedStarClient.CONFIG.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        RedStarClient.CONFIG.save();
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
