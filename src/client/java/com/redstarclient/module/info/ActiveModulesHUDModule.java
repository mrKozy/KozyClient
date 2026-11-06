// â˜­ Red Star Client â€” ActiveModulesHUDModule (ArrayList)
package com.redstarclient.module.info;

import com.redstarclient.RedStarClient;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Clean, compact Active Modules ArrayList on the HUD.
 * Lists all currently enabled modules with Soviet red/gold gradients and accent bars.
 * Supports dragging in the HUD Editor.
 */
public class ActiveModulesHUDModule extends Module implements DraggableHudElement {

    private final ImBoolean showCategory = new ImBoolean(false);
    private final ImBoolean rightAlign   = new ImBoolean(true);
    private final ImInt     posX         = new ImInt(4);
    private final ImInt     posY         = new ImInt(4);

    private int lastWidth = 110;
    private int lastHeight = 60;

    public ActiveModulesHUDModule() {
        super("ArrayList", "Displays enabled modules sorted by length on the HUD.", ModuleCategory.INFO, -1);
        setEnabled(true); // Default ON
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if ((mc.gui != null && mc.gui.hud.isHidden())) return;
        Font font = mc.font;

        List<Module> active = new ArrayList<>();
        for (Module m : RedStarClient.MODULES.getAll()) {
            if (m.isEnabled() && m != this) {
                active.add(m);
            }
        }

        if (active.isEmpty()) {
            this.lastWidth = 90;
            this.lastHeight = 16;
            return;
        }

        // Sort by text length descending
        active.sort(Comparator.comparingInt(m -> -font.width(getModuleLabel(m))));

        int screenWidth = graphics.guiWidth();
        int curY = posY.get();
        long time = System.currentTimeMillis();
        int maxW = 0;

        for (int i = 0; i < active.size(); i++) {
            Module m = active.get(i);
            String label = getModuleLabel(m);
            int textWidth = font.width(label);
            maxW = Math.max(maxW, textWidth + 10);

            int x = rightAlign.get() ? (screenWidth - posX.get() - textWidth - 6) : posX.get();
            int h = font.lineHeight + 2;

            // Semi-transparent dark background
            graphics.fill(x - 2, curY, x + textWidth + 4, curY + h, RenderUtils.toARGB(0.06f, 0.02f, 0.02f, 0.75f));

            // Animated Soviet Gold / Red gradient pulse
            double pulse = Math.sin((time / 400.0) + (i * 0.35));
            int textColor = (pulse > 0) ? RenderUtils.SOVIET_GOLD : RenderUtils.toARGB(1.0f, 0.85f, 0.35f, 1.0f);

            // Accent strip
            if (rightAlign.get()) {
                graphics.fill(x + textWidth + 3, curY, x + textWidth + 5, curY + h, RenderUtils.SOVIET_RED);
            } else {
                graphics.fill(x - 3, curY, x - 1, curY + h, RenderUtils.SOVIET_RED);
            }

            graphics.text(font, label, x, curY + 1, textColor, true);
            curY += h + 1;
        }

        this.lastWidth = Math.max(maxW, 80);
        this.lastHeight = Math.max(curY - posY.get(), 20);
    }

    private String getModuleLabel(Module m) {
        if (showCategory.get()) {
            return m.getName() + " Â§7[" + m.getCategory().displayName + "]";
        }
        return m.getName();
    }

    @Override
    public String getHudName() { return "ArrayList"; }

    @Override
    public int getHudX() {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        return rightAlign.get() ? (sw - posX.get() - lastWidth) : posX.get();
    }

    @Override
    public int getHudY() { return posY.get(); }

    @Override
    public int getHudWidth() { return lastWidth; }

    @Override
    public int getHudHeight() { return lastHeight; }

    @Override
    public void setHudPos(int x, int y) {
        Minecraft mc = Minecraft.getInstance();
        int sw = mc.getWindow().getGuiScaledWidth();
        if (x > sw / 2) {
            rightAlign.set(true);
            posX.set(Math.max(2, sw - x - lastWidth));
        } else {
            rightAlign.set(false);
            posX.set(Math.max(2, x));
        }
        posY.set(Math.max(2, y));
    }

    @Override
    public boolean isHudEnabled() { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Right Aligned##al", rightAlign);
        ImGui.checkbox("Show Categories##al", showCategory);
        ImGui.sliderInt("Offset X##al", posX.getData(), 0, 500);
        ImGui.sliderInt("Offset Y##al", posY.getData(), 0, 500);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("rightAlign", rightAlign.get());
        cfg.putSetting("showCategory", showCategory.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        rightAlign.set(cfg.getSetting("rightAlign", rightAlign.get()));
        showCategory.set(cfg.getSetting("showCategory", showCategory.get()));
    }
}
