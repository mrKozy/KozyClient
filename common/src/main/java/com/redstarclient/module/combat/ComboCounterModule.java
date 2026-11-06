// ☭ Red Star Client — ComboCounterModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Tracks consecutive hits landed on opponents without taking damage yourself.
 */
public class ComboCounterModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(120);

    private int  comboCount = 0;
    private long lastHitTime = 0L;
    private int  lastWidth = 75;
    private int  lastHeight = 16;

    public ComboCounterModule() {
        super("ComboCounter", "Counts consecutive hits landed without taking damage.", ModuleCategory.COMBAT, -1);
    }

    public void onHitLanded() {
        comboCount++;
        lastHitTime = System.currentTimeMillis();
    }

    public void onDamageTaken() {
        comboCount = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        // Reset combo if more than 3.0s without a hit
        if (comboCount > 0 && System.currentTimeMillis() - lastHitTime > 3000L) {
            comboCount = 0;
        }
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        String text = (comboCount > 0) ? "Combo: " + comboCount : "Combo: 0";
        int x = posX.get();
        int y = posY.get();

        int textColor = (comboCount >= 3) ? RenderUtils.toARGB(0.35f, 0.95f, 0.45f, 1.0f) : RenderUtils.SOVIET_GOLD;
        RenderUtils.drawTextWithBackground(graphics, text, x, y, textColor, RenderUtils.BG_DARK_RED);

        this.lastWidth = mc.font.width(text) + 8;
        this.lastHeight = mc.font.lineHeight + 4;
    }

    @Override public String  getHudName()    { return "Combo Counter"; }
    @Override public int     getHudX()       { return posX.get(); }
    @Override public int     getHudY()       { return posY.get(); }
    @Override public int     getHudWidth()   { return lastWidth; }
    @Override public int     getHudHeight()  { return lastHeight; }
    @Override public void    setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled()  { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.textDisabled("Counts hits on opponents. Resets on receiving damage.");
        ImGui.sliderInt("Position X##cc", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##cc", posY.getData(), 0, 800);
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
