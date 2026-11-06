// ☭ Red Star Client — CPSCounterModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.redstarclient.util.TimerUtils;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/** Displays left-click and right-click CPS on the HUD. Supports dragging. */
public class CPSCounterModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(60);

    private final TimerUtils.CPSTracker leftTracker  = new TimerUtils.CPSTracker();
    private final TimerUtils.CPSTracker rightTracker = new TimerUtils.CPSTracker();

    private int lastWidth = 90;
    private int lastHeight = 16;

    public CPSCounterModule() {
        super("CPS Counter", "Displays clicks per second on screen.", ModuleCategory.INFO, -1);
    }

    /** Call from input hook on left-click. */
    public void onLeftClick()  { leftTracker.click(); }

    /** Call from input hook on right-click. */
    public void onRightClick() { rightTracker.click(); }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        String text = "LMB: " + leftTracker.getCPS() + " | RMB: " + rightTracker.getCPS();
        int x = posX.get();
        int y = posY.get();
        RenderUtils.drawTextWithBackground(graphics, text, x, y, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);

        this.lastWidth = mc.font.width(text) + 6;
        this.lastHeight = mc.font.lineHeight + 4;
    }

    @Override
    public String getHudName() { return "CPS Counter"; }

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
        ImGui.textDisabled("Tracks left and right mouse button CPS.");
        ImGui.sliderInt("Position X##cps", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##cps", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
    }
}
