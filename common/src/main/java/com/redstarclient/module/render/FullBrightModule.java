// ☭ Red Star Client — FullBrightModule
package com.redstarclient.module.render;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Forces maximum game brightness so caves are completely lit. */
public class FullBrightModule extends Module {

    private double previousGamma = -1.0;
    private static final double MAX_GAMMA = 1.0;

    public FullBrightModule() {
        super("FullBright", "Forces maximum gamma — no more darkness.", ModuleCategory.RENDER, GLFW.GLFW_KEY_F);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null && mc.options.gamma() != null) {
            previousGamma = mc.options.gamma().get();
            mc.options.gamma().set(MAX_GAMMA);
        }
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null && mc.options.gamma() != null) {
            if (previousGamma >= 0) {
                mc.options.gamma().set(previousGamma);
            }
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc != null && mc.options != null && mc.options.gamma() != null) {
            if (mc.options.gamma().get() < MAX_GAMMA) {
                if (previousGamma < 0) {
                    previousGamma = mc.options.gamma().get();
                }
                mc.options.gamma().set(MAX_GAMMA);
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textDisabled("Forces brightness to maximum (gamma = 16).");
    }
}
