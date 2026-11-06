// ☭ Red Star Client — PlayerRadarHUDModule
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Soviet Player Radar HUD:
 * Displays all nearby players with distance, current health, and held weapon.
 * Draggable in HUD editor.
 */
public class PlayerRadarHUDModule extends Module implements DraggableHudElement {

    private final ImInt     posX       = new ImInt(10);
    private final ImInt     posY       = new ImInt(100);
    private final ImInt     maxPlayers = new ImInt(5);
    private final ImBoolean showItem   = new ImBoolean(true);

    private int lastWidth  = 160;
    private int lastHeight = 80;

    public PlayerRadarHUDModule() {
        super("PlayerRadar", "Sowjetisches Radar-HUD: Zeigt nahe Spieler mit Distanz, Leben und Waffe an.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        List<Player> nearby = new ArrayList<>();
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof Player p && p != mc.player && p.isAlive()) {
                nearby.add(p);
            }
        }

        nearby.sort(Comparator.comparingDouble(p -> p.distanceToSqr(mc.player)));

        int x = posX.get();
        int y = posY.get();
        int curY = y;
        int maxW = 140;

        // Header Title
        String header = "★ SPIELER RADAR (" + nearby.size() + ") ★";
        maxW = Math.max(maxW, mc.font.width(header) + 8);
        RenderUtils.drawTextWithBackground(graphics, header, x, curY, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
        curY += 14;

        int count = Math.min(nearby.size(), maxPlayers.get());
        if (count == 0) {
            String empty = "Keine Spieler in der Naehe";
            maxW = Math.max(maxW, mc.font.width(empty) + 8);
            RenderUtils.drawTextWithBackground(graphics, empty, x, curY, RenderUtils.WHITE, RenderUtils.BG_DARK_RED);
            curY += 14;
        } else {
            for (int i = 0; i < count; i++) {
                Player p = nearby.get(i);
                float dist = (float) Math.sqrt(p.distanceToSqr(mc.player));
                int health = (int) Math.ceil(p.getHealth());

                String nameStr = p.getGameProfile().name();
                String line = String.format("● %s (%.1fm) - %d HP", nameStr, dist, health);
                if (showItem.get() && !p.getMainHandItem().isEmpty()) {
                    line += " [" + p.getMainHandItem().getHoverName().getString() + "]";
                }

                int color = dist < 12.0f ? RenderUtils.HOSTILE_RED : (dist < 28.0f ? RenderUtils.SOVIET_GOLD : RenderUtils.PASSIVE_GREEN);
                maxW = Math.max(maxW, mc.font.width(line) + 8);
                RenderUtils.drawTextWithBackground(graphics, line, x, curY, color, RenderUtils.BG_DARK_RED);
                curY += 13;
            }
        }

        this.lastWidth = maxW;
        this.lastHeight = Math.max(curY - y, 20);
    }

    @Override
    public String getHudName() { return "Player Radar"; }

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
        ImGui.sliderInt("Maximale Spieler-Anzahl##pr", maxPlayers.getData(), 1, 15);
        ImGui.checkbox("Gehaltenes Item anzeigen##pr", showItem);
        ImGui.sliderInt("Position X##pr", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##pr", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("maxPlayers", maxPlayers.get());
        cfg.putSetting("showItem", showItem.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        maxPlayers.set(cfg.getSetting("maxPlayers", maxPlayers.get()));
        showItem.set(cfg.getSetting("showItem", showItem.get()));
    }
}
