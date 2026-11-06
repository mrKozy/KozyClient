// ☭ Red Star Client — BlockOutlineModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;

import java.util.List;

/**
 * Customizes the bounding box outline of targeted blocks (custom color, rainbow, glow).
 */
public class BlockOutlineModule extends Module {

    private final ImBoolean rainbow = new ImBoolean(false);
    private final float[]   color   = { 0.92f, 0.76f, 0.13f, 1.0f }; // Soviet Gold default

    public BlockOutlineModule() {
        super("BlockOutline", "Customizes the crosshair block selection outline color.", ModuleCategory.RENDER, -1);
    }

    public int getOutlineColor() {
        if (!isEnabled()) return 0;
        if (rainbow.get()) {
            double tm = (System.currentTimeMillis() % 3000L) / 3000.0;
            float r = (float) (Math.sin(tm * Math.PI * 2) * 0.5 + 0.5);
            float g = (float) (Math.sin((tm + 0.33) * Math.PI * 2) * 0.5 + 0.5);
            float b = (float) (Math.sin((tm + 0.67) * Math.PI * 2) * 0.5 + 0.5);
            return RenderUtils.toARGB(r, g, b, color[3]);
        }
        return RenderUtils.toARGB(color[0], color[1], color[2], color[3]);
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Rainbow Chroma##bo", rainbow);
        ImGui.colorEdit4("Outline Color##bo", color);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("rainbow", rainbow.get());
        cfg.putSetting("color", List.of(color[0], color[1], color[2], color[3]));
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        rainbow.set(cfg.getSetting("rainbow", rainbow.get()));
        Object c = cfg.getSetting("color", null);
        if (c instanceof List<?> list && list.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                if (list.get(i) instanceof Number num) {
                    color[i] = num.floatValue();
                }
            }
        }
    }
}
