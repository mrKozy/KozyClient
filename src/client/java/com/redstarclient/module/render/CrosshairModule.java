// ☭ Red Star Client — CrosshairModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImColor;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

/**
 * Highly customizable Crosshair Mod.
 * Supports multiple shapes (Cross, Dot, Circle, T-Shape, Box),
 * custom sizing, gap, thickness, center dot, outlines, and color picker.
 */
public class CrosshairModule extends Module {

    private static final String[] SHAPES = { "Classic Cross", "Dot Only", "Circle / Hollow Dot", "T-Shape", "Square Box", "Cross + Dot" };

    private final ImInt     shape         = new ImInt(0);
    private final ImInt     size          = new ImInt(6);
    private final ImInt     gap           = new ImInt(3);
    private final ImInt     thickness     = new ImInt(2);
    private final ImBoolean showDot       = new ImBoolean(true);
    private final ImInt     dotSize       = new ImInt(2);
    private final ImBoolean outline       = new ImBoolean(true);
    private final ImBoolean dynamicSpread = new ImBoolean(false);
    private final ImBoolean rainbow       = new ImBoolean(false);

    private final float[] color = { 0.92f, 0.76f, 0.13f, 1.0f }; // Soviet Gold default

    public CrosshairModule() {
        super("CustomCrosshair", "Customizable crosshair overlay with custom shapes, gap, size and colors.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if ((mc.gui != null && mc.gui.hud.isHidden()) || (mc.gui != null && mc.gui.screen() != null)) return;

        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;

        int actualGap = gap.get();
        if (dynamicSpread.get() && mc.player != null) {
            if (!mc.player.onGround()) {
                actualGap += 4;
            } else if (mc.player.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
                actualGap += 2;
            }
        }

        int crossColor;
        if (rainbow.get()) {
            double t = (System.currentTimeMillis() % 3000L) / 3000.0;
            float r = (float) (Math.sin(t * Math.PI * 2) * 0.5 + 0.5);
            float g = (float) (Math.sin((t + 0.33) * Math.PI * 2) * 0.5 + 0.5);
            float b = (float) (Math.sin((t + 0.67) * Math.PI * 2) * 0.5 + 0.5);
            crossColor = RenderUtils.toARGB(r, g, b, color[3]);
        } else {
            crossColor = RenderUtils.toARGB(color[0], color[1], color[2], color[3]);
        }

        int outlineColor = RenderUtils.toARGB(0.0f, 0.0f, 0.0f, color[3] * 0.9f);

        int s = size.get();
        int t = Math.max(1, thickness.get());
        int halfT = t / 2;
        int sh = shape.get();

        // 1. Center Dot
        if ((showDot.get() && sh != 1) || sh == 1 || sh == 5) {
            int ds = dotSize.get();
            int hds = ds / 2;
            if (outline.get()) {
                graphics.fill(cx - hds - 1, cy - hds - 1, cx + (ds - hds) + 1, cy + (ds - hds) + 1, outlineColor);
            }
            graphics.fill(cx - hds, cy - hds, cx + (ds - hds), cy + (ds - hds), crossColor);
            if (sh == 1) return; // Dot only mode
        }

        // 2. Circle / Hollow Dot
        if (sh == 2) {
            int r = actualGap + s / 2;
            drawHollowBox(graphics, cx - r, cy - r, cx + r + 1, cy + r + 1, crossColor, outlineColor);
            return;
        }

        // 3. Square Box
        if (sh == 4) {
            int r = actualGap + s;
            drawHollowBox(graphics, cx - r, cy - r, cx + r, cy + r, crossColor, outlineColor);
            return;
        }

        // 4. Arms (Cross, T-Shape, Cross+Dot)
        // Top arm (omitted if T-Shape)
        if (sh != 3) {
            drawArm(graphics, cx - halfT, cy - actualGap - s, t, s, crossColor, outlineColor);
        }
        // Bottom arm
        drawArm(graphics, cx - halfT, cy + actualGap, t, s, crossColor, outlineColor);
        // Left arm
        drawArm(graphics, cx - actualGap - s, cy - halfT, s, t, crossColor, outlineColor);
        // Right arm
        drawArm(graphics, cx + actualGap, cy - halfT, s, t, crossColor, outlineColor);
    }

    private void drawArm(GuiGraphicsExtractor graphics, int x, int y, int w, int h, int color, int outlineColor) {
        if (outline.get()) {
            graphics.fill(x - 1, y - 1, x + w + 1, y + h + 1, outlineColor);
        }
        graphics.fill(x, y, x + w, y + h, color);
    }

    private void drawHollowBox(GuiGraphicsExtractor graphics, int x1, int y1, int x2, int y2, int color, int outlineColor) {
        int t = Math.max(1, thickness.get());
        if (outline.get()) {
            // Outlines
            graphics.fill(x1 - 1, y1 - 1, x2 + 1, y1, outlineColor);
            graphics.fill(x1 - 1, y2, x2 + 1, y2 + 1, outlineColor);
            graphics.fill(x1 - 1, y1, x1, y2, outlineColor);
            graphics.fill(x2, y1, x2 + 1, y2, outlineColor);
        }
        // Top, Bottom, Left, Right edges
        graphics.fill(x1, y1, x2, y1 + t, color);
        graphics.fill(x1, y2 - t, x2, y2, color);
        graphics.fill(x1, y1 + t, x1 + t, y2 - t, color);
        graphics.fill(x2 - t, y1 + t, x2, y2 - t, color);
    }

    @Override
    public void renderImGuiConfig() {
        // Live Visual Crosshair Preview Card
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "Live Preview:");

        float previewW = 190.0f;
        float previewH = 100.0f;
        float pX = ImGui.getCursorScreenPosX();
        float pY = ImGui.getCursorScreenPosY();

        ImGui.dummy(previewW, previewH);

        ImDrawList drawList = ImGui.getWindowDrawList();
        if (drawList != null) {
            int bgCol = ImColor.rgba(0.08f, 0.05f, 0.06f, 0.95f);
            int borderCol = ImColor.rgba(0.92f, 0.76f, 0.13f, 0.50f);
            drawList.addRectFilled(pX, pY, pX + previewW, pY + previewH, bgCol, 6.0f);
            drawList.addRect(pX, pY, pX + previewW, pY + previewH, borderCol, 6.0f);

            // Subtle center guide lines
            int gridCol = ImColor.rgba(0.5f, 0.5f, 0.5f, 0.18f);
            drawList.addLine(pX, pY + previewH / 2.0f, pX + previewW, pY + previewH / 2.0f, gridCol, 1.0f);
            drawList.addLine(pX + previewW / 2.0f, pY, pX + previewW / 2.0f, pY + previewH, gridCol, 1.0f);

            float cx = pX + previewW / 2.0f;
            float cy = pY + previewH / 2.0f;

            int crossColor;
            if (rainbow.get()) {
                double tm = (System.currentTimeMillis() % 3000L) / 3000.0;
                float r = (float) (Math.sin(tm * Math.PI * 2) * 0.5 + 0.5);
                float g = (float) (Math.sin((tm + 0.33) * Math.PI * 2) * 0.5 + 0.5);
                float b = (float) (Math.sin((tm + 0.67) * Math.PI * 2) * 0.5 + 0.5);
                crossColor = ImColor.rgba(r, g, b, color[3]);
            } else {
                crossColor = ImColor.rgba(color[0], color[1], color[2], color[3]);
            }

            int outlineColor = ImColor.rgba(0.0f, 0.0f, 0.0f, color[3] * 0.9f);

            int s = size.get();
            int g = gap.get();
            int t = Math.max(1, thickness.get());
            float halfT = t / 2.0f;
            int sh = shape.get();

            // 1. Center Dot
            if ((showDot.get() && sh != 1) || sh == 1 || sh == 5) {
                int ds = dotSize.get();
                float hds = ds / 2.0f;
                if (outline.get()) {
                    drawList.addRectFilled(cx - hds - 1, cy - hds - 1, cx + (ds - hds) + 1, cy + (ds - hds) + 1, outlineColor, 0f);
                }
                drawList.addRectFilled(cx - hds, cy - hds, cx + (ds - hds), cy + (ds - hds), crossColor, 0f);
            }

            // 2. Shapes
            if (sh == 2) { // Circle / Hollow Dot
                float r = g + s / 2.0f;
                if (outline.get()) {
                    drawList.addCircle(cx, cy, r, outlineColor, 24, t + 2.0f);
                }
                drawList.addCircle(cx, cy, r, crossColor, 24, (float) t);
            } else if (sh == 4) { // Square Box
                float r = g + s;
                if (outline.get()) {
                    drawList.addRect(cx - r - 1, cy - r - 1, cx + r + 1, cy + r + 1, outlineColor, 0f, 0, t + 2.0f);
                }
                drawList.addRect(cx - r, cy - r, cx + r, cy + r, crossColor, 0f, 0, (float) t);
            } else if (sh != 1) { // Arms
                if (sh != 3) { // Top arm
                    drawImGuiArm(drawList, cx - halfT, cy - g - s, t, s, crossColor, outlineColor);
                }
                // Bottom
                drawImGuiArm(drawList, cx - halfT, cy + g, t, s, crossColor, outlineColor);
                // Left
                drawImGuiArm(drawList, cx - g - s, cy - halfT, s, t, crossColor, outlineColor);
                // Right
                drawImGuiArm(drawList, cx + g, cy - halfT, s, t, crossColor, outlineColor);
            }
        }

        ImGui.spacing();
        ImGui.separator();

        ImGui.combo("Shape##ch", shape, SHAPES);
        ImGui.sliderInt("Arm Size##ch",      size.getData(),      2, 25);
        ImGui.sliderInt("Center Gap##ch",    gap.getData(),       0, 20);
        ImGui.sliderInt("Thickness##ch",     thickness.getData(), 1, 8);

        ImGui.separator();
        ImGui.checkbox("Center Dot##ch",     showDot);
        if (showDot.get()) {
            ImGui.sliderInt("Dot Size##ch",   dotSize.getData(),   1, 6);
        }
        ImGui.checkbox("Black Outline##ch",  outline);
        ImGui.checkbox("Dynamic Spread##ch", dynamicSpread);
        ImGui.checkbox("Rainbow Chroma##ch", rainbow);

        ImGui.separator();
        ImGui.colorEdit4("Crosshair Color##ch", color);
    }

    private void drawImGuiArm(ImDrawList drawList, float x, float y, float w, float h, int col, int outlineCol) {
        if (outline.get()) {
            drawList.addRectFilled(x - 1, y - 1, x + w + 1, y + h + 1, outlineCol, 0f);
        }
        drawList.addRectFilled(x, y, x + w, y + h, col, 0f);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("shape", shape.get());
        cfg.putSetting("size", size.get());
        cfg.putSetting("gap", gap.get());
        cfg.putSetting("thickness", thickness.get());
        cfg.putSetting("showDot", showDot.get());
        cfg.putSetting("dotSize", dotSize.get());
        cfg.putSetting("outline", outline.get());
        cfg.putSetting("dynamicSpread", dynamicSpread.get());
        cfg.putSetting("rainbow", rainbow.get());
        cfg.putSetting("color", List.of(color[0], color[1], color[2], color[3]));
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        shape.set(cfg.getSetting("shape", shape.get()));
        size.set(cfg.getSetting("size", size.get()));
        gap.set(cfg.getSetting("gap", gap.get()));
        thickness.set(cfg.getSetting("thickness", thickness.get()));
        showDot.set(cfg.getSetting("showDot", showDot.get()));
        dotSize.set(cfg.getSetting("dotSize", dotSize.get()));
        outline.set(cfg.getSetting("outline", outline.get()));
        dynamicSpread.set(cfg.getSetting("dynamicSpread", dynamicSpread.get()));
        rainbow.set(cfg.getSetting("rainbow", rainbow.get()));

        Object c = cfg.getSetting("color", null);
        if (c instanceof List<?> list && list.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                if (list.get(i) instanceof Number num) {
                    color[i] = num.floatValue();
                }
            }
        }
    }
}
