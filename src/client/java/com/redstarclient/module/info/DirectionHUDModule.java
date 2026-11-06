// â˜­ Red Star Client â€” DirectionHUDModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.redstarclient.util.MathUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Modern Direction Compass HUD bar displaying cardinal direction (N, NE, E, SE, S, SW, W, NW)
 * and degree orientation for team callouts.
 */
public class DirectionHUDModule extends Module implements DraggableHudElement {

    private final ImInt     posX       = new ImInt(240);
    private final ImInt     posY       = new ImInt(10);
    private final ImBoolean showDegree = new ImBoolean(true);
    private final ImBoolean barStyle   = new ImBoolean(true);

    private int lastWidth  = 160;
    private int lastHeight = 22;

    public DirectionHUDModule() {
        super("DirectionHUD", "Displays a compass heading and degrees at top of screen.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        float yaw = MathUtils.wrapDegrees(mc.player.getYRot());
        if (yaw < 0) yaw += 360f;

        String cardinal = getCardinal(yaw);
        int x = posX.get();
        int y = posY.get();

        if (barStyle.get()) {
            int barWidth = 140;
            int barHeight = 16;
            this.lastWidth = barWidth;
            this.lastHeight = barHeight;

            // Background pill
            graphics.fill(x, y, x + barWidth, y + barHeight, RenderUtils.toARGB(0.06f, 0.02f, 0.02f, 0.85f));
            graphics.fill(x, y, x + barWidth, y + 1, RenderUtils.SOVIET_GOLD);
            graphics.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, RenderUtils.SOVIET_GOLD);

            // Center indicator
            int cx = x + barWidth / 2;
            graphics.fill(cx - 1, y + 1, cx + 1, y + barHeight - 1, RenderUtils.toARGB(0.92f, 0.76f, 0.13f, 0.9f));

            String text = cardinal + (showDegree.get() ? String.format(" [%.0fÂ°]", yaw) : "");
            int tw = mc.font.width(text);
            graphics.text(mc.font, text, cx - tw / 2, y + 4, RenderUtils.SOVIET_GOLD, false);
        } else {
            String text = cardinal + (showDegree.get() ? String.format(" (%.0fÂ°)", yaw) : "");
            RenderUtils.drawTextWithBackground(graphics, text, x, y, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
            this.lastWidth = mc.font.width(text) + 8;
            this.lastHeight = mc.font.lineHeight + 4;
        }
    }

    private String getCardinal(float yaw) {
        if (yaw >= 337.5 || yaw < 22.5)  return "S";
        if (yaw >= 22.5  && yaw < 67.5)  return "SW";
        if (yaw >= 67.5  && yaw < 112.5) return "W";
        if (yaw >= 112.5 && yaw < 157.5) return "NW";
        if (yaw >= 157.5 && yaw < 202.5) return "N";
        if (yaw >= 202.5 && yaw < 247.5) return "NE";
        if (yaw >= 247.5 && yaw < 292.5) return "E";
        return "SE";
    }

    @Override public String  getHudName()    { return "Direction Compass"; }
    @Override public int     getHudX()       { return posX.get(); }
    @Override public int     getHudY()       { return posY.get(); }
    @Override public int     getHudWidth()   { return lastWidth; }
    @Override public int     getHudHeight()  { return lastHeight; }
    @Override public void    setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled()  { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show Degrees##dir", showDegree);
        ImGui.checkbox("Bar Compass Style##dir", barStyle);
        ImGui.sliderInt("Position X##dir", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##dir", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showDegree", showDegree.get());
        cfg.putSetting("barStyle", barStyle.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showDegree.set(cfg.getSetting("showDegree", showDegree.get()));
        barStyle.set(cfg.getSetting("barStyle", barStyle.get()));
    }
}
