// ☭ Red Star Client — CommunistTheme (Frosted Crimson Glass Edition)
package com.redstarclient.theme;

import com.redstarclient.gui.Icons;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

/**
 * Authentic Soviet Frosted Glass (Glasmorphism) Theme.
 * Deep translucent ruby obsidian glass with crimson glow, frosted cards,
 * and warm amber gold proletariat accents.
 */
public final class CommunistTheme {

    // ── Color palette (RGBA floats) ───────────────────────────────────────────
    public static final float[] SOVIET_RED    = { 0.88f, 0.12f, 0.16f, 1.00f }; // #E01F29 Vibrant Soviet Ruby
    public static final float[] DARK_RED      = { 0.52f, 0.06f, 0.10f, 0.88f };
    public static final float[] SOVIET_GOLD   = { 0.96f, 0.82f, 0.22f, 1.00f }; // #F5D138 Proletariat Gold
    public static final float[] BORDER_GOLD   = { 0.96f, 0.82f, 0.22f, 0.45f };

    // Frosted Glassmorphism Layers
    public static final float[] BG_GLASS      = { 0.10f, 0.02f, 0.03f, 0.72f }; // Translucent Smoked Ruby Glass
    public static final float[] CARD_GLASS    = { 0.20f, 0.03f, 0.05f, 0.42f }; // Frosted Glass Card
    public static final float[] CARD_HOVER    = { 0.36f, 0.06f, 0.09f, 0.65f }; // Illuminated Red Glass
    public static final float[] CARD_ACTIVE   = { 0.60f, 0.10f, 0.14f, 0.85f };
    public static final float[] BORDER_RED    = { 0.88f, 0.14f, 0.18f, 0.55f }; // Glowing Crimson Border Rim

    // Text colors
    public static final float[] TEXT_PRIMARY  = { 0.98f, 0.96f, 0.94f, 1.00f }; // Crisp Pure Cream
    public static final float[] TEXT_MUTED    = { 0.72f, 0.65f, 0.64f, 0.80f }; // Translucent Silver-Gray

    private CommunistTheme() {}

    /**
     * Applies the modern frosted glassmorphic Soviet theme directly to the base ImGuiStyle.
     */
    public static void applyToStyle(ImGuiStyle style) {
        if (style == null) return;

        // Windows & Translucent Backgrounds (Frosted Glass)
        style.setColor(ImGuiCol.WindowBg,          BG_GLASS[0],     BG_GLASS[1],     BG_GLASS[2],     BG_GLASS[3]);
        style.setColor(ImGuiCol.ChildBg,           CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   CARD_GLASS[3]);
        style.setColor(ImGuiCol.PopupBg,           0.12f, 0.02f, 0.04f, 0.88f);
        style.setColor(ImGuiCol.Border,            BORDER_RED[0],   BORDER_RED[1],   BORDER_RED[2],   BORDER_RED[3]);
        style.setColor(ImGuiCol.BorderShadow,      0.00f, 0.00f, 0.00f, 0.00f);

        // Title Bar
        style.setColor(ImGuiCol.TitleBg,           0.10f, 0.02f, 0.03f, 0.85f);
        style.setColor(ImGuiCol.TitleBgActive,     DARK_RED[0],    DARK_RED[1],    DARK_RED[2],    0.95f);
        style.setColor(ImGuiCol.TitleBgCollapsed,  0.06f, 0.01f, 0.02f, 0.70f);

        // Frame / Inputs
        style.setColor(ImGuiCol.FrameBg,           CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   CARD_GLASS[3]);
        style.setColor(ImGuiCol.FrameBgHovered,    CARD_HOVER[0],   CARD_HOVER[1],   CARD_HOVER[2],   CARD_HOVER[3]);
        style.setColor(ImGuiCol.FrameBgActive,     DARK_RED[0],    DARK_RED[1],    DARK_RED[2],    0.92f);

        // Buttons
        style.setColor(ImGuiCol.Button,            CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   0.75f);
        style.setColor(ImGuiCol.ButtonHovered,     CARD_HOVER[0],   CARD_HOVER[1],   CARD_HOVER[2],   0.88f);
        style.setColor(ImGuiCol.ButtonActive,      SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  0.95f);

        // Controls
        style.setColor(ImGuiCol.CheckMark,         SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.SliderGrab,        SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 0.95f);
        style.setColor(ImGuiCol.SliderGrabActive,  SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  1.00f);
        style.setColor(ImGuiCol.Header,            CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   0.70f);
        style.setColor(ImGuiCol.HeaderHovered,     CARD_HOVER[0],   CARD_HOVER[1],   CARD_HOVER[2],   0.85f);
        style.setColor(ImGuiCol.HeaderActive,      CARD_ACTIVE[0],  CARD_ACTIVE[1],  CARD_ACTIVE[2],  0.90f);

        // Tabs
        style.setColor(ImGuiCol.Tab,               CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   0.55f);
        style.setColor(ImGuiCol.TabHovered,        DARK_RED[0],    DARK_RED[1],    DARK_RED[2],    0.85f);
        style.setColor(ImGuiCol.TabActive,         SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  0.92f);
        style.setColor(ImGuiCol.TabUnfocused,      CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   0.35f);
        style.setColor(ImGuiCol.TabUnfocusedActive,DARK_RED[0],    DARK_RED[1],    DARK_RED[2],    0.70f);

        // Scrollbars & Separators
        style.setColor(ImGuiCol.ScrollbarBg,       0.08f, 0.02f, 0.03f, 0.65f);
        style.setColor(ImGuiCol.ScrollbarGrab,     SOVIET_RED[0], SOVIET_RED[1], SOVIET_RED[2], 0.85f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive,  SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.Separator,         BORDER_RED[0],  BORDER_RED[1],  BORDER_RED[2],  0.40f);
        style.setColor(ImGuiCol.SeparatorHovered,  SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  0.75f);
        style.setColor(ImGuiCol.SeparatorActive,   SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 0.90f);

        // Text
        style.setColor(ImGuiCol.Text,              TEXT_PRIMARY[0],TEXT_PRIMARY[1],TEXT_PRIMARY[2],TEXT_PRIMARY[3]);
        style.setColor(ImGuiCol.TextDisabled,      TEXT_MUTED[0],  TEXT_MUTED[1],  TEXT_MUTED[2],  TEXT_MUTED[3]);

        // Rounded Modern Geometry
        style.setWindowRounding(14.0f);
        style.setChildRounding(10.0f);
        style.setFrameRounding(8.0f);
        style.setPopupRounding(12.0f);
        style.setGrabRounding(6.0f);
        style.setTabRounding(8.0f);
        style.setScrollbarRounding(8.0f);
        style.setWindowBorderSize(1.5f);
        style.setChildBorderSize(1.0f);
        style.setFrameBorderSize(1.0f);
        style.setPopupBorderSize(1.5f);
        style.setWindowPadding(14.0f, 12.0f);
        style.setItemSpacing(8.0f, 6.0f);
    }

    public static void apply() {
        // Base style is already applied via applyToStyle(ImGui.getStyle())
    }

    public static void cleanup() {
        // No-op for style-based approach
    }

    public static void renderSovietHeader(String title) {
        float windowWidth = ImGui.getWindowWidth();
        float titleWidth = ImGui.calcTextSize(title).x;
        ImGui.setCursorPosX((windowWidth - titleWidth) / 2.0f);
        ImGui.textColored(SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.0f, title);

        String stars = Icons.STAR + " ═══════════════════════════════ " + Icons.STAR;
        float starsWidth = ImGui.calcTextSize(stars).x;
        ImGui.setCursorPosX((windowWidth - starsWidth) / 2.0f);
        ImGui.textColored(BORDER_GOLD[0], BORDER_GOLD[1], BORDER_GOLD[2], BORDER_GOLD[3], stars);
    }

    public static void renderSovietFooter() {
        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();
        String quote = "★ Proletarier aller Laender, vereinigt euch! ★";
        float windowWidth = ImGui.getWindowWidth();
        float quoteWidth = ImGui.calcTextSize(quote).x;
        ImGui.setCursorPosX((windowWidth - quoteWidth) / 2.0f);
        ImGui.textColored(SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 0.75f, quote);
    }
}
