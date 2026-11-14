package com.redstarclient.cosmetics.knife;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class InspectHandler {

    private static volatile boolean inspecting = false;
    private static long startTime = 0;
    private static final long DURATION_MS = 2400;

    private InspectHandler() {}

    public static void onKeyPress(int key) {
        CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
        if (mod == null || !mod.isEnabled() || mod.getSwordModel() == CosmeticsModule.SwordModel.DEFAULT) {
            return;
        }

        if (key == mod.getInspectKey()) {
            inspecting = true;
            startTime = System.currentTimeMillis();
        }
    }

    public static boolean isInspecting() {
        if (!inspecting) return false;
        long elapsed = System.currentTimeMillis() - startTime;

        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() != null && (mc.gui == null || mc.gui.screen() == null)) {
            CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
            if (mod != null && mod.getInspectKey() > 0) {
                // If the inspect key is held down, continue looping
                if (GLFW.glfwGetKey(mc.getWindow().handle(), mod.getInspectKey()) == GLFW.GLFW_PRESS) {
                    return true;
                }
            }
        }

        if (elapsed > DURATION_MS) {
            inspecting = false;
            return false;
        }
        return true;
    }

    public static float getProgress() {
        if (!isInspecting()) return 0.0f;
        long elapsed = System.currentTimeMillis() - startTime;
        return (float) ((elapsed % DURATION_MS) / (double) DURATION_MS);
    }
}
