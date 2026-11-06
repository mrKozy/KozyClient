// ☭ Red Star Client — KeystrokesHUDModule
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
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedList;

/**
 * Renders on-screen WASD + Mouse Keystrokes with press animations and real-time CPS.
 * Supports interactive dragging in the HUD Editor.
 */
public class KeystrokesHUDModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(120);
    private final ImBoolean showSpace = new ImBoolean(true);
    private final ImBoolean showCPS = new ImBoolean(true);

    private final LinkedList<Long> leftClicks = new LinkedList<>();
    private final LinkedList<Long> rightClicks = new LinkedList<>();

    private boolean lastLmb = false;
    private boolean lastRmb = false;

    private static final int KEY_SIZE = 22;
    private static final int GAP = 2;

    public KeystrokesHUDModule() {
        super("Keystrokes", "Shows WASD and mouse buttons with press highlights and CPS on HUD.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.mouseHandler == null) return;
        long window = mc.getWindow().getWindow();

        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        long now = System.currentTimeMillis();
        if (lmb && !lastLmb) leftClicks.add(now);
        if (rmb && !lastRmb) rightClicks.add(now);

        lastLmb = lmb;
        lastRmb = rmb;

        leftClicks.removeIf(t -> now - t > 1000L);
        rightClicks.removeIf(t -> now - t > 1000L);
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;
        Font font = mc.font;

        boolean w = mc.options.keyUp.isDown();
        boolean a = mc.options.keyLeft.isDown();
        boolean s = mc.options.keyDown.isDown();
        boolean d = mc.options.keyRight.isDown();
        boolean space = mc.options.keyJump.isDown();

        long window = mc.getWindow().getWindow();
        boolean lmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean rmb = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;

        int x = posX.get();
        int y = posY.get();
        int size = KEY_SIZE;
        int gap = GAP;

        // Row 1: [ W ]
        drawKey(graphics, font, "W", x + size + gap, y, size, size, w);

        // Row 2: [ A ] [ S ] [ D ]
        int row2Y = y + size + gap;
        drawKey(graphics, font, "A", x, row2Y, size, size, a);
        drawKey(graphics, font, "S", x + size + gap, row2Y, size, size, s);
        drawKey(graphics, font, "D", x + (size + gap) * 2, row2Y, size, size, d);

        // Row 3: [ LMB ] [ RMB ] with CPS
        int row3Y = row2Y + size + gap;
        int mouseW = (size * 3 + gap * 2 - gap) / 2;

        String lmbLabel = showCPS.get() ? "LMB " + leftClicks.size() : "LMB";
        String rmbLabel = showCPS.get() ? "RMB " + rightClicks.size() : "RMB";

        drawKey(graphics, font, lmbLabel, x, row3Y, mouseW, size, lmb);
        drawKey(graphics, font, rmbLabel, x + mouseW + gap, row3Y, mouseW, size, rmb);

        // Row 4: [ ---- SPACE ---- ]
        if (showSpace.get()) {
            int row4Y = row3Y + size + gap;
            int spaceW = size * 3 + gap * 2;
            drawKey(graphics, font, "—", x, row4Y, spaceW, 14, space);
        }
    }

    private void drawKey(GuiGraphics graphics, Font font, String label,
                         int x, int y, int w, int h, boolean pressed) {
        int bg = pressed
                ? RenderUtils.toARGB(0.72f, 0.05f, 0.05f, 0.90f)
                : RenderUtils.toARGB(0.08f, 0.02f, 0.02f, 0.70f);

        int border = pressed ? RenderUtils.SOVIET_GOLD : RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.30f);
        int textColor = pressed ? RenderUtils.SOVIET_GOLD : RenderUtils.WHITE;

        graphics.fill(x, y, x + w, y + h, bg);
        graphics.fill(x, y, x + w, y + 1, border);
        graphics.fill(x, y + h - 1, x + w, y + h, border);
        graphics.fill(x, y, x + 1, y + h, border);
        graphics.fill(x + w - 1, y, x + w, y + h, border);

        int textW = font.width(label);
        int textX = x + (w - textW) / 2;
        int textY = y + (h - font.lineHeight) / 2 + 1;
        graphics.drawString(font, label, textX, textY, textColor, false);
    }

    @Override
    public String getHudName() { return "Keystrokes"; }

    @Override
    public int getHudX() { return posX.get(); }

    @Override
    public int getHudY() { return posY.get(); }

    @Override
    public int getHudWidth() { return KEY_SIZE * 3 + GAP * 2; }

    @Override
    public int getHudHeight() {
        int h = (KEY_SIZE + GAP) * 3;
        if (showSpace.get()) h += 14 + GAP;
        return h;
    }

    @Override
    public void setHudPos(int x, int y) {
        posX.set(x);
        posY.set(y);
    }

    @Override
    public boolean isHudEnabled() { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show Space Bar##ks", showSpace);
        ImGui.checkbox("Show CPS Counters##ks", showCPS);
        ImGui.sliderInt("Position X##ks", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##ks", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showSpace", showSpace.get());
        cfg.putSetting("showCPS", showCPS.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showSpace.set(cfg.getSetting("showSpace", showSpace.get()));
        showCPS.set(cfg.getSetting("showCPS", showCPS.get()));
    }
}
