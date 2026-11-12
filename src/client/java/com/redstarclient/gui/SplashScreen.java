// KozyClient — SplashScreen
package com.redstarclient.gui;

import com.redstarclient.RedStarClient;
import com.redstarclient.render.TextureLoader;
import com.redstarclient.theme.CommunistTheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

/**
 * Clean, animated splash screen shown for ~2 seconds on first game join or client init.
 * Fades in, displays logo + name + version, then fades out automatically.
 *
 * Usage: call SplashScreen.show() to trigger. Call SplashScreen.render() every frame.
 * Returns true while visible.
 */
public class SplashScreen {

    private static final float FADE_IN_DURATION  = 0.45f; // seconds
    private static final float HOLD_DURATION     = 1.60f; // seconds
    private static final float FADE_OUT_DURATION = 0.50f; // seconds
    private static final float TOTAL_DURATION    = FADE_IN_DURATION + HOLD_DURATION + FADE_OUT_DURATION;

    private static boolean active = false;
    private static long    startMs = 0L;

    private static final int FLAGS =
            ImGuiWindowFlags.NoTitleBar   |
            ImGuiWindowFlags.NoResize     |
            ImGuiWindowFlags.NoMove       |
            ImGuiWindowFlags.NoScrollbar  |
            ImGuiWindowFlags.NoCollapse;

    /** Trigger the splash screen. */
    public static void show() {
        active  = true;
        startMs = System.currentTimeMillis();
    }

    /** Returns true if the splash is currently visible. */
    public static boolean isActive() {
        return active;
    }

    /**
     * Call every ImGui frame. Renders the splash and returns true while active.
     * Returns false once the animation finishes.
     */
    public static boolean render() {
        if (!active) return false;

        float elapsed = (System.currentTimeMillis() - startMs) / 1000.0f;
        if (elapsed >= TOTAL_DURATION) {
            active = false;
            return false;
        }

        // Compute alpha
        float alpha;
        if (elapsed < FADE_IN_DURATION) {
            alpha = elapsed / FADE_IN_DURATION;
        } else if (elapsed < FADE_IN_DURATION + HOLD_DURATION) {
            alpha = 1.0f;
        } else {
            float fadeElapsed = elapsed - FADE_IN_DURATION - HOLD_DURATION;
            alpha = 1.0f - (fadeElapsed / FADE_OUT_DURATION);
        }
        alpha = Math.max(0f, Math.min(1f, alpha));

        // Ease-in-out
        float eased = alpha < 0.5f
                ? 2f * alpha * alpha
                : 1f - (float) Math.pow(-2f * alpha + 2f, 2) / 2f;

        float sw = ImGui.getIO().getDisplaySizeX();
        float sh = ImGui.getIO().getDisplaySizeY();

        // Full-screen dark overlay
        ImGui.setNextWindowPos(0, 0);
        ImGui.setNextWindowSize(sw, sh);

        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.04f, 0.04f, 0.06f, eased * 0.97f);
        ImGui.pushStyleColor(ImGuiCol.Border, 0f, 0f, 0f, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f);
        ImGui.pushStyleVar(ImGuiStyleVar.Alpha, eased);

        if (ImGui.begin("##KozySplash", FLAGS)) {

            float contentW = 320f;
            float contentH = 160f;
            float cx = (sw - contentW) * 0.5f;
            float cy = (sh - contentH) * 0.5f;

            // Logo
            int logoId = TextureLoader.getLogoTextureId();
            if (logoId > 0) {
                float logoSize = 72f;
                ImGui.setCursorPos(cx + (contentW - logoSize) * 0.5f, cy);
                ImGui.image(logoId, logoSize, logoSize);
                cy += logoSize + 14f;
            } else {
                cy += 20f;
            }

            // Client Name
            String name = "KozyClient";
            float nameW = ImGui.calcTextSize(name).x;
            ImGui.setCursorPos(cx + (contentW - nameW) * 0.5f, cy);
            ImGui.textColored(
                    CommunistTheme.SOVIET_GOLD[0],
                    CommunistTheme.SOVIET_GOLD[1],
                    CommunistTheme.SOVIET_GOLD[2],
                    eased,
                    name
            );
            cy += ImGui.getTextLineHeight() + 6f;

            // Version
            String ver = "v" + RedStarClient.VERSION + "  |  by mr.Kozy";
            float verW = ImGui.calcTextSize(ver).x;
            ImGui.setCursorPos(cx + (contentW - verW) * 0.5f, cy);
            ImGui.textColored(0.65f, 0.65f, 0.65f, eased * 0.85f, ver);
            cy += ImGui.getTextLineHeight() + 18f;

            // Animated progress bar (thin line)
            float progress = Math.min(1f, elapsed / TOTAL_DURATION);
            float barW = contentW;
            float barH = 2f;
            float barX = cx;
            float barY = cy;
            ImGui.setCursorPos(barX, barY);
            ImGui.getWindowDrawList().addRectFilled(
                    ImGui.getWindowPos().x + barX,
                    ImGui.getWindowPos().y + barY,
                    ImGui.getWindowPos().x + barX + barW * progress,
                    ImGui.getWindowPos().y + barY + barH,
                    toImGuiColor(CommunistTheme.SOVIET_GOLD, eased)
            );
        }
        ImGui.end();

        ImGui.popStyleVar(2);
        ImGui.popStyleColor(2);

        return true;
    }

    private static int toImGuiColor(float[] rgb, float alpha) {
        int r = (int)(rgb[0] * 255) & 0xFF;
        int g = (int)(rgb[1] * 255) & 0xFF;
        int b = (int)(rgb[2] * 255) & 0xFF;
        int a = (int)(alpha    * 255) & 0xFF;
        return (a << 24) | (b << 16) | (g << 8) | r;
    }
}
