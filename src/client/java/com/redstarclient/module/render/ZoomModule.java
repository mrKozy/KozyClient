// ☭ Red Star Client — ZoomModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/**
 * Cinematic OptiFine-style smooth zoom.
 * Uses high-precision timestamp-based exponential easing to guarantee
 * 100% butter-smooth, wobble-free zooming in and out at any framerate.
 */
public class ZoomModule extends Module {

    private final ImFloat   zoomFOV       = new ImFloat(30f);
    private final ImFloat   smoothSpeed   = new ImFloat(12f);
    private final ImBoolean holdToZoom    = new ImBoolean(true);
    private final ImBoolean scrollAdjust  = new ImBoolean(true);

    private float currentFOV = 70f;
    private boolean transitioning = false;
    private long lastFrameTime = 0L;

    public ZoomModule() {
        super("Zoom", "Cinematic smooth zoom. Hold C to zoom in.", ModuleCategory.RENDER, GLFW.GLFW_KEY_C);
    }

    public boolean isZooming() {
        if (!isEnabled()) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.getWindow() == null || (mc.gui != null && mc.gui.screen() != null)) return false;

        if (!holdToZoom.get()) {
            return true; // Toggle mode
        }

        int bind = getKeybind();
        if (bind <= 0) bind = GLFW.GLFW_KEY_C;
        return GLFW.glfwGetKey(mc.getWindow().handle(), bind) == GLFW.GLFW_PRESS;
    }

    public boolean isTransitioning() {
        return transitioning;
    }

    public boolean isScrollAdjust() {
        return scrollAdjust.get();
    }

    /**
     * Returns smoothly interpolated FOV for GameRendererMixin.
     */
    public float getCurrentFOV(float normalFOV, float tickDelta) {
        boolean active = isZooming();
        float target = active ? zoomFOV.get() : normalFOV;

        long now = System.nanoTime();
        if (lastFrameTime == 0L) {
            lastFrameTime = now;
            currentFOV = normalFOV;
        }

        float dt = (now - lastFrameTime) / 1_000_000_000.0f;
        lastFrameTime = now;
        dt = Math.max(0.001f, Math.min(dt, 0.05f)); // Clamp delta time to avoid frame skips

        // True framerate-independent exponential smoothing: 1 - exp(-speed * dt)
        float speed = smoothSpeed.get();
        float factor = 1.0f - (float) Math.exp(-speed * dt);
        currentFOV += (target - currentFOV) * factor;

        if (Math.abs(currentFOV - normalFOV) > 0.25f) {
            transitioning = true;
        } else if (!active) {
            transitioning = false;
            currentFOV = normalFOV;
        }

        return currentFOV;
    }

    /** Adjust zoom level with mouse wheel */
    public void onMouseScroll(double amount) {
        if (scrollAdjust.get() && isZooming()) {
            float fov = zoomFOV.get() - (float)amount * 4.0f;
            fov = Math.max(5f, Math.min(fov, 65f));
            zoomFOV.set(fov);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Zoom Ziel-FOV##zm", zoomFOV.getData(),     5f,  60f, "%.0f°");
        ImGui.sliderFloat("Smooth-Speed##zm",  smoothSpeed.getData(), 2f,  25f, "%.1fx");
        ImGui.checkbox("Hold to Zoom (C halten)##zm", holdToZoom);
        ImGui.checkbox("Mausrad anpassen##zm", scrollAdjust);
        ImGui.spacing();
        ImGui.textDisabled("Taste: Standardmaessig 'C' halten.");
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Wackelfreie, mathematisch perfekte Exponential-Interpolation.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("zoomFOV", zoomFOV.get());
        cfg.putSetting("smoothSpeed", smoothSpeed.get());
        cfg.putSetting("holdToZoom", holdToZoom.get());
        cfg.putSetting("scrollAdjust", scrollAdjust.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        zoomFOV.set(cfg.getSetting("zoomFOV", zoomFOV.get()));
        smoothSpeed.set(cfg.getSetting("smoothSpeed", smoothSpeed.get()));
        holdToZoom.set(cfg.getSetting("holdToZoom", holdToZoom.get()));
        scrollAdjust.set(cfg.getSetting("scrollAdjust", scrollAdjust.get()));
    }
}
