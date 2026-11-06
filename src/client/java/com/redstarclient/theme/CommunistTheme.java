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

    // ── Meteor Red-Black Color palette (RGBA floats) ───────────────────────────
    public static final float[] SOVIET_RED    = { 0.82f, 0.11f, 0.15f, 1.00f }; // #D11C26 Vibrant Red
    public static final float[] DARK_RED      = { 0.50f, 0.06f, 0.09f, 0.95f }; // #800F17
    public static final float[] SOVIET_GOLD   = { 0.96f, 0.82f, 0.22f, 1.00f }; // #F5D138 Proletariat Gold
    public static final float[] BORDER_GOLD   = { 0.96f, 0.82f, 0.22f, 0.45f };

    // Meteor Red-Black Layers
    public static final float[] BG_GLASS      = { 0.07f, 0.07f, 0.09f, 0.94f }; // #121217 Deep Charcoal Black
    public static final float[] CARD_GLASS    = { 0.12f, 0.12f, 0.15f, 0.85f }; // Inactive item
    public static final float[] CARD_HOVER    = { 0.28f, 0.08f, 0.11f, 0.90f }; // Inactive hover
    public static final float[] CARD_ACTIVE   = { 0.82f, 0.11f, 0.15f, 1.00f }; // Enabled item
    public static final float[] BORDER_RED    = { 0.65f, 0.08f, 0.12f, 0.65f }; // Crimson border

    // Text colors
    public static final float[] TEXT_PRIMARY  = { 1.00f, 1.00f, 1.00f, 1.00f }; // Crisp White
    public static final float[] TEXT_MUTED    = { 0.70f, 0.70f, 0.75f, 0.85f }; // Light Gray

    private CommunistTheme() {}

    /**
     * Applies the modern Meteor Red-Black theme directly to the base ImGuiStyle.
     */
    public static void applyToStyle(ImGuiStyle style) {
        if (style == null) return;

        // Windows & Translucent Backgrounds (Deep Charcoal Black)
        style.setColor(ImGuiCol.WindowBg,          BG_GLASS[0],     BG_GLASS[1],     BG_GLASS[2],     BG_GLASS[3]);
        style.setColor(ImGuiCol.ChildBg,           0.09f, 0.09f, 0.12f, 0.90f);
        style.setColor(ImGuiCol.PopupBg,           0.08f, 0.08f, 0.10f, 0.96f);
        style.setColor(ImGuiCol.Border,            BORDER_RED[0],   BORDER_RED[1],   BORDER_RED[2],   BORDER_RED[3]);
        style.setColor(ImGuiCol.BorderShadow,      0.00f, 0.00f, 0.00f, 0.00f);

        // Title Bar (Vibrant Crimson Red like Meteor)
        style.setColor(ImGuiCol.TitleBg,           0.75f, 0.09f, 0.13f, 0.95f);
        style.setColor(ImGuiCol.TitleBgActive,     0.85f, 0.11f, 0.16f, 1.00f);
        style.setColor(ImGuiCol.TitleBgCollapsed,  0.55f, 0.07f, 0.10f, 0.85f);

        // Frame / Inputs
        style.setColor(ImGuiCol.FrameBg,           0.13f, 0.13f, 0.17f, 0.90f);
        style.setColor(ImGuiCol.FrameBgHovered,    CARD_HOVER[0],   CARD_HOVER[1],   CARD_HOVER[2],   CARD_HOVER[3]);
        style.setColor(ImGuiCol.FrameBgActive,     DARK_RED[0],    DARK_RED[1],    DARK_RED[2],    0.95f);

        // Buttons
        style.setColor(ImGuiCol.Button,            CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   CARD_GLASS[3]);
        style.setColor(ImGuiCol.ButtonHovered,     CARD_HOVER[0],   CARD_HOVER[1],   CARD_HOVER[2],   CARD_HOVER[3]);
        style.setColor(ImGuiCol.ButtonActive,      SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  1.00f);

        // Controls
        style.setColor(ImGuiCol.CheckMark,         SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.SliderGrab,        SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  1.00f);
        style.setColor(ImGuiCol.SliderGrabActive,  SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.Header,            0.75f, 0.09f, 0.13f, 0.85f);
        style.setColor(ImGuiCol.HeaderHovered,     0.85f, 0.11f, 0.16f, 0.95f);
        style.setColor(ImGuiCol.HeaderActive,      0.90f, 0.13f, 0.18f, 1.00f);

        // Tabs
        style.setColor(ImGuiCol.Tab,               CARD_GLASS[0],   CARD_GLASS[1],   CARD_GLASS[2],   0.70f);
        style.setColor(ImGuiCol.TabHovered,        DARK_RED[0],    DARK_RED[1],    DARK_RED[2],    0.90f);
        style.setColor(ImGuiCol.TabActive,         SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  0.95f);
        style.setColor(ImGuiCol.TabUnfocused,      0.10f, 0.10f, 0.13f, 0.50f);
        style.setColor(ImGuiCol.TabUnfocusedActive,0.60f, 0.08f, 0.11f, 0.80f);

        // Scrollbars & Separators
        style.setColor(ImGuiCol.ScrollbarBg,       0.06f, 0.06f, 0.08f, 0.70f);
        style.setColor(ImGuiCol.ScrollbarGrab,     SOVIET_RED[0], SOVIET_RED[1], SOVIET_RED[2], 0.85f);
        style.setColor(ImGuiCol.ScrollbarGrabHovered, SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.ScrollbarGrabActive,  SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);
        style.setColor(ImGuiCol.Separator,         BORDER_RED[0],  BORDER_RED[1],  BORDER_RED[2],  0.50f);
        style.setColor(ImGuiCol.SeparatorHovered,  SOVIET_RED[0],  SOVIET_RED[1],  SOVIET_RED[2],  0.85f);
        style.setColor(ImGuiCol.SeparatorActive,   SOVIET_GOLD[0], SOVIET_GOLD[1], SOVIET_GOLD[2], 1.00f);

        // Text
        style.setColor(ImGuiCol.Text,              TEXT_PRIMARY[0],TEXT_PRIMARY[1],TEXT_PRIMARY[2],TEXT_PRIMARY[3]);
        style.setColor(ImGuiCol.TextDisabled,      TEXT_MUTED[0],  TEXT_MUTED[1],  TEXT_MUTED[2],  TEXT_MUTED[3]);

        // Meteor Modern Geometry
        style.setWindowRounding(6.0f);
        style.setChildRounding(4.0f);
        style.setFrameRounding(4.0f);
        style.setPopupRounding(6.0f);
        style.setGrabRounding(3.0f);
        style.setTabRounding(4.0f);
        style.setScrollbarRounding(4.0f);
        style.setWindowBorderSize(1.5f);
        style.setChildBorderSize(1.0f);
        style.setFrameBorderSize(1.0f);
        style.setPopupBorderSize(1.5f);
        style.setWindowPadding(6.0f, 6.0f);
        style.setItemSpacing(3.0f, 2.0f);
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
