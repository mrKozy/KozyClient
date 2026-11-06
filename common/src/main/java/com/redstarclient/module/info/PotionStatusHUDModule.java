// ☭ Red Star Client — PotionStatusHUDModule
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
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.Collection;

/**
 * Displays all active potion effects with names, amplifier levels,
 * and exact seconds countdown on the HUD. Fully draggable.
 */
public class PotionStatusHUDModule extends Module implements DraggableHudElement {

    private final ImInt     posX        = new ImInt(10);
    private final ImInt     posY        = new ImInt(180);
    private final ImBoolean showEmpty   = new ImBoolean(false);
    private final ImBoolean colorCoded  = new ImBoolean(true);

    private int lastWidth  = 110;
    private int lastHeight = 20;

    public PotionStatusHUDModule() {
        super("PotionStatus", "Displays active potion effects and duration on HUD.", ModuleCategory.INFO, -1);
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        Collection<MobEffectInstance> effects = mc.player.getActiveEffects();
        if (effects.isEmpty()) {
            if (showEmpty.get()) {
                String emptyText = "No Effects Active";
                RenderUtils.drawTextWithBackground(graphics, emptyText, posX.get(), posY.get(), RenderUtils.WHITE, RenderUtils.BG_DARK_RED);
                this.lastWidth = mc.font.width(emptyText) + 8;
                this.lastHeight = mc.font.lineHeight + 4;
            }
            return;
        }

        int x = posX.get();
        int y = posY.get();
        int curY = y;
        int maxW = 90;

        for (MobEffectInstance inst : effects) {
            Holder<MobEffect> effectHolder = inst.getEffect();
            String name = Component.translatable(effectHolder.value().getDescriptionId()).getString();

            int amp = inst.getAmplifier();
            if (amp > 0) {
                name += " " + (amp + 1);
            }

            int totalSeconds = inst.getDuration() / 20;
            int mins = totalSeconds / 60;
            int secs = totalSeconds % 60;
            String timeStr = String.format("%d:%02d", mins, secs);

            String fullText = name + " §7" + timeStr;
            int textColor = RenderUtils.SOVIET_GOLD;
            if (colorCoded.get()) {
                int effectColor = effectHolder.value().getColor();
                if (effectColor != 0) {
                    textColor = 0xFF000000 | effectColor;
                }
            }

            RenderUtils.drawTextWithBackground(graphics, fullText, x, curY, textColor, RenderUtils.BG_DARK_RED);
            maxW = Math.max(maxW, mc.font.width(fullText) + 8);
            curY += 14;
        }

        this.lastWidth = maxW;
        this.lastHeight = Math.max(curY - y, 16);
    }

    @Override public String  getHudName()    { return "Potion Status"; }
    @Override public int     getHudX()       { return posX.get(); }
    @Override public int     getHudY()       { return posY.get(); }
    @Override public int     getHudWidth()   { return lastWidth; }
    @Override public int     getHudHeight()  { return lastHeight; }
    @Override public void    setHudPos(int x, int y) { posX.set(x); posY.set(y); }
    @Override public boolean isHudEnabled()  { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show when Empty##pot", showEmpty);
        ImGui.checkbox("Color-code Effects##pot", colorCoded);
        ImGui.sliderInt("Position X##pot", posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##pot", posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showEmpty", showEmpty.get());
        cfg.putSetting("colorCoded", colorCoded.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showEmpty.set(cfg.getSetting("showEmpty", showEmpty.get()));
        colorCoded.set(cfg.getSetting("colorCoded", colorCoded.get()));
    }
}
