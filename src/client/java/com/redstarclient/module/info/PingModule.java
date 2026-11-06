// â˜­ Red Star Client â€” PingModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

/** Displays server ping with color-coded latency. Supports dragging. */
public class PingModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(95);

    private int lastWidth = 75;
    private int lastHeight = 16;

    public PingModule() {
        super("Ping", "Displays server ping with color coding.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
        if (info == null) return;

        int ping = info.getLatency();
        int color;
        if (ping < 50)       color = RenderUtils.PASSIVE_GREEN;
        else if (ping < 100) color = RenderUtils.SOVIET_GOLD;
        else                 color = RenderUtils.HOSTILE_RED;

        String text = "Ping: " + ping + "ms";
        int x = posX.get();
        int y = posY.get();
        RenderUtils.drawTextWithBackground(graphics, text, x, y, color, RenderUtils.BG_DARK_RED);

        this.lastWidth = mc.font.width(text) + 6;
        this.lastHeight = mc.font.lineHeight + 4;
    }

    @Override
    public String getHudName() { return "Ping"; }

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
        ImGui.textDisabled("<50ms = green, <100ms = yellow, >100ms = red");
        ImGui.sliderInt("Position X##pi", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##pi", posY.getData(), 0, 800);
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
