// ☭ Red Star Client — SpeedometerModule
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

/**
 * Calculates and displays the player's movement speed in blocks/sec (m/s) or km/h.
 */
public class SpeedometerModule extends Module implements DraggableHudElement {

    private final ImInt     posX    = new ImInt(10);
    private final ImInt     posY    = new ImInt(140);
    private final ImBoolean showKmh = new ImBoolean(true);

    private double currentSpeedBps = 0.0;
    private int    lastWidth       = 95;
    private int    lastHeight      = 16;

    public SpeedometerModule() {
        super("Speedometer", "Measures and displays movement speed in m/s.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        double dx = mc.player.getX() - mc.player.xo;
        double dz = mc.player.getZ() - mc.player.zo;
        double bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        // Exponential smoothing
        this.currentSpeedBps = (this.currentSpeedBps * 0.7) + (bps * 0.3);
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        String text;
        if (showKmh.get()) {
            double kmh = currentSpeedBps * 3.6;
            text = String.format("Speed: %.1f m/s (%.0f km/h)", currentSpeedBps, kmh);
        } else {
            text = String.format("Speed: %.2f m/s", currentSpeedBps);
        }

        int x = posX.get();
        int y = posY.get();
        RenderUtils.drawTextWithBackground(graphics, text, x, y, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);

        this.lastWidth = mc.font.width(text) + 8;
        this.lastHeight = mc.font.lineHeight + 4;
    }

    @Override public String  getHudName()    { return "Speedometer"; }
    @Override public int     getHudX()       { return posX.get(); }
    @Override public int     getHudY()       { return posY.get(); }
    @Override public int     getHudWidth()   { return lastWidth; }
    @Override public int     getHudHeight()  { return lastHeight; }
    @Override public void    setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled()  { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show km/h##spd", showKmh);
        ImGui.sliderInt("Position X##spd", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##spd", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showKmh", showKmh.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showKmh.set(cfg.getSetting("showKmh", showKmh.get()));
    }
}
