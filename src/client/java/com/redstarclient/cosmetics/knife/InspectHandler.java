package com.redstarclient.cosmetics.knife;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class InspectHandler {

    private static volatile boolean inspecting = false;
    private static long startTime = 0;
    private static boolean easingOut = false;
    private static long easeStartTime = 0;
    private static float easeFrom = 0.0f;
    private static final long DURATION_MS = 3800;
    private static final float EASE_OUT_SECONDS = 0.2f;

    private InspectHandler() {}

    public static void triggerInspect() {
        CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
        if (mod == null || !mod.isEnabled() || mod.getSwordModel() == CosmeticsModule.SwordModel.DEFAULT) {
            return;
        }
        inspecting = true;
        easingOut = false;
        startTime = System.currentTimeMillis();
    }

    public static void onKeyPress(int key) {
        CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
        if (mod == null || !mod.isEnabled() || mod.getSwordModel() == CosmeticsModule.SwordModel.DEFAULT) {
            return;
        }

        if (key == mod.getInspectKey()) {
            triggerInspect();
        }
    }

    public static boolean isInspecting() {
        if (!inspecting) return false;
        long elapsed = System.currentTimeMillis() - startTime;

        Minecraft mc = Minecraft.getInstance();
        boolean held = false;
        if (mc.getWindow() != null && (mc.gui == null || mc.gui.screen() == null)) {
            CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
            if (mod != null && mod.getInspectKey() > 0) {
                // If the inspect key is held down, continue looping
                if (GLFW.glfwGetKey(mc.getWindow().handle(), mod.getInspectKey()) == GLFW.GLFW_PRESS) {
                    held = true;
                }
            }
        }

        if (!held && elapsed > DURATION_MS) {
            if (!easingOut) {
                easingOut = true;
                easeStartTime = System.currentTimeMillis();
                easeFrom = 1.0f;
            } else {
                float easeElapsed = (System.currentTimeMillis() - easeStartTime) / 1000.0f;
                if (easeElapsed >= EASE_OUT_SECONDS) {
                    inspecting = false;
                    easingOut = false;
                    return false;
                }
            }
        }
        return true;
    }

    public static float getProgress() {
        if (!inspecting) return 0.0f;
        long elapsed = System.currentTimeMillis() - startTime;
        return (float) ((elapsed % DURATION_MS) / (double) DURATION_MS);
    }

    public static float getWeight() {
        if (!inspecting) return 0.0f;
        if (easingOut) {
            float t = (System.currentTimeMillis() - easeStartTime) / 1000.0f / EASE_OUT_SECONDS;
            if (t >= 1.0f) return 0.0f;
            return 1.0f - t * t * (3.0f - 2.0f * t);
        }
        long elapsed = System.currentTimeMillis() - startTime;
        float in = elapsed / 200.0f; // 200ms ease-in
        return Math.min(1.0f, in);
    }
}
