// ☭ Red Star Client — ReachDisplayModule
package com.redstarclient.module.combat;

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

/**
 * Displays the exact hit distance (in blocks) on screen whenever you attack an entity.
 */
public class ReachDisplayModule extends Module implements DraggableHudElement {

    private final ImInt     posX     = new ImInt(10);
    private final ImInt     posY     = new ImInt(100);
    private final ImBoolean fadeOut  = new ImBoolean(true);

    private double lastHitDistance = 0.0;
    private long   lastHitTime     = 0L;
    private int    lastWidth       = 80;
    private int    lastHeight      = 16;

    public ReachDisplayModule() {
        super("ReachDisplay", "Displays your attack hit distance in blocks.", ModuleCategory.COMBAT, -1);
    }

    public void onEntityHit(double distance) {
        this.lastHitDistance = distance;
        this.lastHitTime = System.currentTimeMillis();
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        long elapsed = System.currentTimeMillis() - lastHitTime;
        if (fadeOut.get() && elapsed > 2800L && lastHitDistance > 0) {
            return;
        }

        String text = (lastHitDistance > 0) ? String.format("Reach: %.2fm", lastHitDistance) : "Reach: 0.00m";
        int x = posX.get();
        int y = posY.get();

        RenderUtils.drawTextWithBackground(graphics, text, x, y, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
        this.lastWidth = mc.font.width(text) + 8;
        this.lastHeight = mc.font.lineHeight + 4;
    }

    @Override public String  getHudName()    { return "Reach Display"; }
    @Override public int     getHudX()       { return posX.get(); }
    @Override public int     getHudY()       { return posY.get(); }
    @Override public int     getHudWidth()   { return lastWidth; }
    @Override public int     getHudHeight()  { return lastHeight; }
    @Override public void    setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled()  { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Auto-Fade after hit##rd", fadeOut);
        ImGui.sliderInt("Position X##rd", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##rd", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("fadeOut", fadeOut.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        fadeOut.set(cfg.getSetting("fadeOut", fadeOut.get()));
    }
}
