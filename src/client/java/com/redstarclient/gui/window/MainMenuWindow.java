// ☭ Red Star Client — MainMenuWindow (Meteor ClickGUI Red-Black Edition)
package com.redstarclient.gui.window;

import com.redstarclient.RedStarClient;
import com.redstarclient.gui.Icons;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.theme.CommunistTheme;
import com.redstarclient.util.KeyUtils;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

/**
 * Meteor Client-inspired ClickGUI with authentic Red-Black theme.
 * Features side-by-side draggable category column windows, 1-click toggles,
 * right-click settings popups, dedicated live search column, and top navigation bar.
 */
public class MainMenuWindow {

    public enum TopTab {
        MODULES,
        COSMETICS,
        CONFIG,
        SETTINGS
    }

    private TopTab activeTab = TopTab.MODULES;
    private final ImString searchFilter = new ImString("", 128);
    private final ImString configProfileName = new ImString("default", 64);
    private Module popupSettingsModule = null;
    private String statusMessage = "";
    private long statusMessageTime = 0L;

    // Smooth opening / closing transition animation
    private float openAnimationProgress = 0.0f;
    private long lastRenderTime = 0L;

    public void onMenuOpened() {
        openAnimationProgress = 0.0f;
        lastRenderTime = System.currentTimeMillis();
    }

    public void render() {
        long now = System.currentTimeMillis();
        float dt = (lastRenderTime > 0) ? Math.min(0.1f, (now - lastRenderTime) / 1000.0f) : 0.016f;
        lastRenderTime = now;

        if (openAnimationProgress < 1.0f) {
            openAnimationProgress = Math.min(1.0f, openAnimationProgress + dt * 6.5f);
        }
        float alpha = (float) Math.sin(openAnimationProgress * (Math.PI / 2.0));
        ImGui.pushStyleVar(ImGuiStyleVar.Alpha, alpha);

        try {
            // ── 1. Top Navigation Bar (Meteor Style) ──────────────────────────────
            renderTopBar();

            // ── 2. Render Active Tab Content ──────────────────────────────────────
            if (activeTab == TopTab.MODULES) {
                renderClickGuiColumns();
            } else if (activeTab == TopTab.COSMETICS) {
                renderCosmeticsWindow();
            } else if (activeTab == TopTab.CONFIG) {
                renderConfigWindow();
            } else if (activeTab == TopTab.SETTINGS) {
                renderSettingsWindow();
            }

            // ── 3. Module Settings Floating Window (Right-click any module) ───────
            if (popupSettingsModule != null) {
                renderModuleSettingsPopup();
            }
        } finally {
            ImGui.popStyleVar();
        }
    }

    /**
     * Sleek top navigation bar displaying client title, active tab selector, and quick actions.
     */
    private void renderTopBar() {
        float screenWidth = ImGui.getIO().getDisplaySizeX();
        ImGui.setNextWindowPos(0, 0, ImGuiCond.Always);
        ImGui.setNextWindowSize(screenWidth, 36.0f, ImGuiCond.Always);

        int barFlags = ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove |
                       ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoCollapse;

        ImGui.pushStyleColor(imgui.flag.ImGuiCol.WindowBg, 0.05f, 0.05f, 0.07f, 0.96f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 0.65f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 10.0f, 6.0f);

        if (ImGui.begin("##RedStarTopBar", barFlags)) {
            // Left: High-resolution logo emblem & Title & Version
            int logoId = com.redstarclient.render.TextureLoader.getLogoTextureId();
            if (logoId > 0) {
                ImGui.image(logoId, 22.0f, 22.0f);
                ImGui.sameLine();
            }
            ImGui.alignTextToFramePadding();
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    "KozyClient");
            ImGui.sameLine();
            ImGui.textDisabled("v" + RedStarClient.VERSION);

            ImGui.sameLine();
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.85f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
            ImGui.smallButton("Key: " + KeyUtils.getKeyName(RedStarClient.menuKey));
            ImGui.popStyleColor(2);
            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.textUnformatted("Menu-Taste zum Oeffnen/Schliessen: " + KeyUtils.getKeyName(RedStarClient.menuKey));
                ImGui.endTooltip();
            }

            // Header Freecam quick exit button if active
            com.redstarclient.module.render.FreecamModule freecam = RedStarClient.MODULES.get(com.redstarclient.module.render.FreecamModule.class);
            if (freecam != null && freecam.isEnabled()) {
                ImGui.sameLine();
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 1.0f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
                if (ImGui.smallButton("★ FREECAM BEENDEN##hdrFc")) {
                    freecam.setEnabled(false);
                }
                ImGui.popStyleColor(2);
            }

            // Right side: Active modules badge, Tabs, and Close Button
            int activeCount = 0;
            for (Module m : RedStarClient.MODULES.getAll()) {
                if (m.isEnabled()) activeCount++;
            }

            float rightSideWidth = 460.0f;
            ImGui.sameLine(screenWidth - rightSideWidth);

            // Active counter
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "● " + activeCount + " Active");

            // Tab: Modules
            ImGui.sameLine();
            boolean isMod = (activeTab == TopTab.MODULES);
            if (isMod) pushActiveTabStyle();
            if (ImGui.smallButton(" Modules ")) {
                activeTab = TopTab.MODULES;
            }
            if (isMod) popActiveTabStyle();

            // Tab: Cosmetics
            ImGui.sameLine();
            boolean isCos = (activeTab == TopTab.COSMETICS);
            if (isCos) pushActiveTabStyle();
            if (ImGui.smallButton(" Cosmetics ")) {
                activeTab = TopTab.COSMETICS;
            }
            if (isCos) popActiveTabStyle();

            // Tab: Config
            ImGui.sameLine();
            boolean isCfg = (activeTab == TopTab.CONFIG);
            if (isCfg) pushActiveTabStyle();
            if (ImGui.smallButton(" Config ")) {
                activeTab = TopTab.CONFIG;
            }
            if (isCfg) popActiveTabStyle();

            // Tab: HUD Editor
            ImGui.sameLine();
            if (ImGui.smallButton(" [✎] HUD Editor ")) {
                RedStarClient.openHudEditor();
            }

            // Tab: Settings
            ImGui.sameLine();
            boolean isSet = (activeTab == TopTab.SETTINGS);
            if (isSet) pushActiveTabStyle();
            if (ImGui.smallButton(" Settings ")) {
                activeTab = TopTab.SETTINGS;
            }
            if (isSet) popActiveTabStyle();

            // Close button [ ✕ ]
            ImGui.sameLine();
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.85f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.TEXT_PRIMARY[0], CommunistTheme.TEXT_PRIMARY[1], CommunistTheme.TEXT_PRIMARY[2], 1.0f);
            if (ImGui.smallButton(" ✕ ##topClose")) {
                RedStarClient.GUI.toggleMenu();
            }
            ImGui.popStyleColor(2);
            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.textUnformatted("Menue schliessen (" + KeyUtils.getKeyName(RedStarClient.menuKey) + ")");
                ImGui.endTooltip();
            }
        }
        ImGui.end();
        ImGui.popStyleVar(2);
        ImGui.popStyleColor(2);
    }

    private void pushActiveTabStyle() {
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 1.0f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
    }

    private void popActiveTabStyle() {
        ImGui.popStyleColor(2);
    }

    /**
     * Renders the 7 Meteor-style side-by-side category column windows.
     */
    private void renderClickGuiColumns() {
        // Collect modules for each column
        List<Module> combatMods = new ArrayList<>();
        combatMods.addAll(RedStarClient.MODULES.getByCategory(ModuleCategory.COMBAT));
        combatMods.addAll(RedStarClient.MODULES.getByCategory(ModuleCategory.CRYSTAL));
        combatMods.addAll(RedStarClient.MODULES.getByCategory(ModuleCategory.MACE));
        combatMods.addAll(RedStarClient.MODULES.getByCategory(ModuleCategory.CART));
        combatMods.addAll(RedStarClient.MODULES.getByCategory(ModuleCategory.POT_UHC));

        List<Module> playerMods    = RedStarClient.MODULES.getByCategory(ModuleCategory.PLAYER);
        List<Module> movementMods  = RedStarClient.MODULES.getByCategory(ModuleCategory.MOVEMENT);
        List<Module> renderMods    = RedStarClient.MODULES.getByCategory(ModuleCategory.RENDER);
        List<Module> inventoryMods = RedStarClient.MODULES.getByCategory(ModuleCategory.INVENTORY);
        List<Module> infoMods      = RedStarClient.MODULES.getByCategory(ModuleCategory.INFO);

        float colWidth = 138.0f;
        float startY = 40.0f;

        // Column 1: Combat
        renderCategoryColumn("Combat", Icons.SWORDS, combatMods, 12.0f, startY, colWidth);

        // Column 2: Player
        renderCategoryColumn("Player", Icons.USER, playerMods, 155.0f, startY, colWidth);

        // Column 3: Movement
        renderCategoryColumn("Movement", Icons.RUNNING, movementMods, 298.0f, startY, colWidth);

        // Column 4: Render
        renderCategoryColumn("Render", Icons.EYE, renderMods, 441.0f, startY, colWidth);

        // Column 5: Inventory
        renderCategoryColumn("Inventory", Icons.BOX, inventoryMods, 584.0f, startY, colWidth);

        // Column 6: Info
        renderCategoryColumn("Info", Icons.CHART, infoMods, 727.0f, startY, colWidth);

        // Column 7: Search
        renderSearchColumn(870.0f, startY, colWidth);
    }

    /**
     * Renders an individual draggable category window containing a vertical stack of module buttons.
     */
    private void renderCategoryColumn(String name, String icon, List<Module> modules, float defaultX, float defaultY, float width) {
        ImGui.setNextWindowPos(defaultX, defaultY, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(width, 0.0f, ImGuiCond.FirstUseEver);

        int winFlags = ImGuiWindowFlags.NoResize;

        // Custom Crimson Title Bar & Deep Charcoal Window
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBg,          0.72f, 0.08f, 0.12f, 0.95f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBgActive,    0.85f, 0.11f, 0.16f, 1.00f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBgCollapsed, 0.50f, 0.06f, 0.09f, 0.90f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.WindowBg,         0.08f, 0.08f, 0.10f, 0.94f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border,           0.65f, 0.08f, 0.12f, 0.65f);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 3.0f, 3.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0.0f, 1.0f);

        String windowTitle = " " + icon + " " + name + "##col_" + name;
        boolean open = ImGui.begin(windowTitle, winFlags);

        if (open) {
            float btnWidth = ImGui.getContentRegionAvailX();

            for (Module m : modules) {
                renderModuleRow(m, btnWidth);
            }
        }
        ImGui.end();

        ImGui.popStyleVar(3);
        ImGui.popStyleColor(5);
    }

    /**
     * Dedicated Search Column (Search input field at top, matching modules below).
     */
    private void renderSearchColumn(float defaultX, float defaultY, float width) {
        ImGui.setNextWindowPos(defaultX, defaultY, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(width, 0.0f, ImGuiCond.FirstUseEver);

        int winFlags = ImGuiWindowFlags.NoResize;

        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBg,          0.72f, 0.08f, 0.12f, 0.95f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBgActive,    0.85f, 0.11f, 0.16f, 1.00f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBgCollapsed, 0.50f, 0.06f, 0.09f, 0.90f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.WindowBg,         0.08f, 0.08f, 0.10f, 0.94f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border,           0.65f, 0.08f, 0.12f, 0.65f);

        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 4.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 3.0f, 3.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0.0f, 1.0f);

        String windowTitle = " " + Icons.SEARCH + " Search##col_Search";
        boolean open = ImGui.begin(windowTitle, winFlags);

        if (open) {
            float btnWidth = ImGui.getContentRegionAvailX();

            // Search input field
            ImGui.pushItemWidth(btnWidth);
            ImGui.inputText("##searchModInput", searchFilter);
            ImGui.popItemWidth();

            ImGui.spacing();

            String filter = searchFilter.get().toLowerCase().trim();
            if (filter.isEmpty()) {
                ImGui.textDisabled(" Tippe um zu suchen...");
            } else {
                int matched = 0;
                for (Module m : RedStarClient.MODULES.getAll()) {
                    if (m.getName().toLowerCase().contains(filter) ||
                        m.getDescription().toLowerCase().contains(filter)) {
                        renderModuleRow(m, btnWidth);
                        matched++;
                    }
                }
                if (matched == 0) {
                    ImGui.textDisabled(" Keine Treffer.");
                }
            }
        }
        ImGui.end();

        ImGui.popStyleVar(3);
        ImGui.popStyleColor(5);
    }

    /**
     * Renders a single module row/button in Meteor style.
     * Left click: Toggle ON/OFF.
     * Right click: Open Settings Window.
     */
    private void renderModuleRow(Module m, float btnWidth) {
        ImGui.pushID(m.getName());

        boolean enabled = m.isEnabled();

        // Pulsing breathing animation for active modules
        long time = System.currentTimeMillis();
        float pulse = (float) (0.86 + 0.14 * Math.sin(time / 180.0));

        // Enabled: Pulsing Crimson. Disabled: Dark Charcoal/Slate Gray.
        if (enabled) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button,        0.78f * pulse, 0.10f, 0.14f, 0.95f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 0.94f, 0.15f, 0.22f, 1.00f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive,  0.65f, 0.08f, 0.12f, 1.00f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text,          1.00f, 1.00f, 1.00f, 1.00f);
        } else {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button,        0.13f, 0.13f, 0.17f, 0.85f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonHovered, 0.28f, 0.10f, 0.14f, 0.92f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ButtonActive,  0.45f, 0.08f, 0.11f, 1.00f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text,          0.80f, 0.80f, 0.85f, 0.90f);
        }

        // Module Label with animated indicator dot and keybind badge if bound
        String keyBadge = (m.getKeybind() > 0) ? " [" + KeyUtils.getKeyName(m.getKeybind()) + "]" : "";
        String dot = enabled ? "● " : "  ";
        String label = " " + dot + m.getName() + keyBadge;

        // Render Button (compact height)
        ImGui.button(label, btnWidth, 20.0f);

        // Click detection
        if (ImGui.isItemClicked(0)) {
            m.toggle();
        } else if (ImGui.isItemClicked(1)) {
            popupSettingsModule = m;
        }

        // Hover tooltip
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    m.getName() + (enabled ? " [AKTIV]" : " [INAKTIV]"));
            ImGui.textDisabled(m.getDescription());
            ImGui.separator();
            ImGui.textDisabled("Linksklick: Umschalten | Rechtsklick: Optionen anpassen");
            if (m.getKeybind() > 0) {
                ImGui.textColored(0.96f, 0.82f, 0.22f, 1.0f, "Keybind: " + KeyUtils.getKeyName(m.getKeybind()));
            }
            ImGui.endTooltip();
        }

        ImGui.popStyleColor(4);
        ImGui.popID();
    }

    /**
     * Dedicated floating rectangular pop-up window for module configuration.
     * Triggered by right-clicking any module.
     */
    private void renderModuleSettingsPopup() {
        if (popupSettingsModule == null) return;

        ImGui.setNextWindowSize(380.0f, 340.0f, ImGuiCond.FirstUseEver);

        ImGui.pushStyleColor(imgui.flag.ImGuiCol.WindowBg,         0.08f, 0.08f, 0.10f, 0.96f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border,           0.75f, 0.10f, 0.14f, 0.85f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBg,          0.70f, 0.08f, 0.12f, 0.95f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBgActive,    0.85f, 0.11f, 0.16f, 1.00f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.5f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 8.0f);

        int popupFlags = ImGuiWindowFlags.NoCollapse;
        ImBoolean pOpen = new ImBoolean(true);
        String windowTitle = "★ " + popupSettingsModule.getName() + " — Einstellungen##ModulePopup";

        if (ImGui.begin(windowTitle, pOpen, popupFlags)) {
            // Header Bar inside popup
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    Icons.COG + "  " + popupSettingsModule.getName().toUpperCase());
            ImGui.sameLine();
            ImGui.textDisabled("[" + popupSettingsModule.getCategory().displayName + "]");

            float closeBtnWidth = 28.0f;
            ImGui.sameLine(ImGui.getWindowWidth() - closeBtnWidth - 10.0f);
            if (ImGui.smallButton(" ✕ ##closeModal")) {
                pOpen.set(false);
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            // Description
            ImGui.textDisabled(popupSettingsModule.getDescription());
            ImGui.spacing();

            // Toggle button
            boolean enabled = popupSettingsModule.isEnabled();
            if (enabled) {
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 1.0f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
            }
            if (ImGui.button(enabled ? "[ AKTIVIERT ]" : "[ DEAKTIVIERT ]", 130.0f, 26.0f)) {
                popupSettingsModule.toggle();
            }
            if (enabled) {
                ImGui.popStyleColor(2);
            }

            // Keybind button
            ImGui.sameLine();
            boolean isBindingThis = (RedStarClient.bindingModule == popupSettingsModule);
            String bindText = isBindingThis ? "[ TASTE DRUECKEN ]" : "Key: [" + KeyUtils.getKeyName(popupSettingsModule.getKeybind()) + "]";
            if (isBindingThis) {
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.1f, 0.05f, 0.05f, 1.0f);
            }
            if (ImGui.button(bindText + "##modalBind", 160.0f, 26.0f)) {
                if (isBindingThis) {
                    RedStarClient.bindingModule = null;
                } else {
                    RedStarClient.bindingModule = popupSettingsModule;
                    RedStarClient.bindingMenuKey = false;
                }
            }
            if (isBindingThis) {
                ImGui.popStyleColor(2);
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            // Module-specific Settings container (scrollable rectangular view)
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    Icons.SLIDERS + " Parameter & Optionen:");
            ImGui.spacing();

            ImGui.beginChild("##ModalSettingsBody", 0, 0, true);
            popupSettingsModule.renderImGuiConfig();
            ImGui.endChild();
        }
        ImGui.end();

        ImGui.popStyleVar(2);
        ImGui.popStyleColor(4);

        if (!pOpen.get()) {
            popupSettingsModule = null;
        }
    }

    /**
     * Floating window for configuration profile management.
     */
    private void renderConfigWindow() {
        ImGui.setNextWindowPos(280.0f, 80.0f, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(460.0f, 280.0f, ImGuiCond.FirstUseEver);

        int winFlags = ImGuiWindowFlags.NoCollapse;
        ImBoolean pOpen = new ImBoolean(true);

        if (ImGui.begin("★ Configuration Profiles##RedStarConfigWin", pOpen, winFlags)) {
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    Icons.BOX + " Profiles (C:\\Redstar Client configs)");
            ImGui.separator();
            ImGui.spacing();

            ImGui.pushItemWidth(170.0f);
            ImGui.inputText("Profile Name##cfgName", configProfileName);
            ImGui.popItemWidth();

            ImGui.sameLine();
            if (ImGui.button(Icons.CHECK + " Speichern", 100.0f, 24.0f)) {
                boolean ok = RedStarClient.CONFIG.saveConfig(configProfileName.get());
                if (ok) showStatus("Profil '" + configProfileName.get() + "' gespeichert!");
                else showStatus("Fehler beim Speichern.");
            }

            ImGui.sameLine();
            if (ImGui.button(Icons.TERMINAL + " Laden", 90.0f, 24.0f)) {
                boolean ok = RedStarClient.CONFIG.loadConfig(configProfileName.get());
                if (ok) showStatus("Profil '" + configProfileName.get() + "' geladen!");
                else showStatus("Profil nicht gefunden.");
            }

            ImGui.spacing();
            if (ImGui.button("Ordner oeffnen##openFolder", 120.0f, 24.0f)) {
                try {
                    java.awt.Desktop.getDesktop().open(RedStarClient.CONFIG.getConfigDirectory().toFile());
                } catch (Throwable t) {
                    showStatus("Ordner: " + RedStarClient.CONFIG.getConfigDirectory());
                }
            }

            ImGui.spacing();
            List<String> configs = RedStarClient.CONFIG.listConfigs();
            if (!configs.isEmpty()) {
                ImGui.textDisabled("Schnellauswahl:");
                for (String cfgName : configs) {
                    ImGui.sameLine();
                    if (ImGui.smallButton(cfgName + "##loadbtn")) {
                        configProfileName.set(cfgName);
                        RedStarClient.CONFIG.loadConfig(cfgName);
                        showStatus("Geladen: " + cfgName);
                    }
                }
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            if (ImGui.button("Alle Module deaktivieren (Clean Slate)##disableAll", 240.0f, 24.0f)) {
                for (Module m : RedStarClient.MODULES.getAll()) {
                    m.setEnabled(false);
                }
                showStatus("Alle Module wurden deaktiviert.");
            }

            ImGui.spacing();
            if (!statusMessage.isEmpty() && System.currentTimeMillis() - statusMessageTime < 4000L) {
                ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, Icons.CHECK + " " + statusMessage);
            }
        }
        ImGui.end();

        if (!pOpen.get()) {
            activeTab = TopTab.MODULES;
        }
    }

    /**
     * Dedicated Cosmetics tab: Sword models (Butterfly, Karambit, Katana, Red Dagger),
     * Character models & skins (Tung Tung Tung Sahur), and Head wear (Asian Hat, Halo, Horns).
     */
    private void renderCosmeticsWindow() {
        float screenWidth = ImGui.getIO().getDisplaySizeX();
        float screenHeight = ImGui.getIO().getDisplaySizeY();

        float winW = Math.min(620.0f, screenWidth - 60.0f);
        float winH = Math.min(500.0f, screenHeight - 80.0f);
        ImGui.setNextWindowPos((screenWidth - winW) * 0.5f, (screenHeight - winH) * 0.5f + 16.0f, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(winW, winH, ImGuiCond.FirstUseEver);

        int flags = ImGuiWindowFlags.NoCollapse;
        ImBoolean pOpen = new ImBoolean(true);

        if (ImGui.begin("Cosmetics & Skins##KozyCosmetics", pOpen, flags)) {
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    Icons.STAR + " Kozy Cosmetics & 3D Skin Models");
            ImGui.separator();
            ImGui.spacing();

            com.redstarclient.module.render.CosmeticsModule cosMod = RedStarClient.MODULES.get(com.redstarclient.module.render.CosmeticsModule.class);
            if (cosMod != null) {
                boolean enabled = cosMod.isEnabled();
                if (ImGui.checkbox("Cosmetics Aktivieren##cosToggle", enabled)) {
                    cosMod.setEnabled(!enabled);
                }

                ImGui.spacing();
                cosMod.renderImGuiConfig();
            }

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            // Head cosmetics quick config (AsianHatModule)
            com.redstarclient.module.render.AsianHatModule hatMod = RedStarClient.MODULES.get(com.redstarclient.module.render.AsianHatModule.class);
            if (hatMod != null) {
                ImGui.textColored(CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 1.0f,
                        Icons.USER + " Head Wear (Asian Hat & Horns):");
                boolean hatEnabled = hatMod.isEnabled();
                if (ImGui.checkbox("Head Cosmetics Aktivieren##hatToggle", hatEnabled)) {
                    hatMod.setEnabled(!hatEnabled);
                }
                if (hatMod.isEnabled()) {
                    hatMod.renderImGuiConfig();
                }
            }
        }
        ImGui.end();

        if (!pOpen.get()) {
            activeTab = TopTab.MODULES;
        }
    }

    /**
     * Floating window for general client settings & keybindings.
     */
    private void renderSettingsWindow() {
        ImGui.setNextWindowPos(320.0f, 90.0f, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowSize(440.0f, 260.0f, ImGuiCond.FirstUseEver);

        int winFlags = ImGuiWindowFlags.NoCollapse;
        ImBoolean pOpen = new ImBoolean(true);

        if (ImGui.begin("★ Client Einstellungen##RedStarSettingsWin", pOpen, winFlags)) {
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    Icons.COG + " Client Keybindings & Optionen");
            ImGui.separator();
            ImGui.spacing();

            // Menu key rebinding
            ImGui.text("ClickGUI Taste zum Oeffnen:");
            boolean isBindingMenu = RedStarClient.bindingMenuKey;
            String menuKeyLabel = isBindingMenu ? "[ BELIEBIGE TASTE DRUECKEN ]" : "Menu Key: [" + KeyUtils.getKeyName(RedStarClient.menuKey) + "]";

            if (isBindingMenu) {
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.1f, 0.05f, 0.05f, 1.0f);
            }

            if (ImGui.button(menuKeyLabel, 260.0f, 28.0f)) {
                RedStarClient.bindingMenuKey = !isBindingMenu;
                RedStarClient.bindingModule = null;
            }

            if (isBindingMenu) {
                ImGui.popStyleColor(2);
            }

            ImGui.textDisabled("Standardmaessig: INSERT (Einfuegen).");

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();

            ImGui.text("PvP Texturen: " + Icons.CHECK + " Nativ integriert (Kurze Schwerter, Low Fire)");
            ImGui.textDisabled("Zoom: C gedrueckt halten (Hold C to Zoom)");

            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 0.8f,
                    Icons.STAR + " KozyClient v3.0 by mr.Kozy");
        }
        ImGui.end();

        if (!pOpen.get()) {
            activeTab = TopTab.MODULES;
        }
    }

    private void showStatus(String msg) {
        statusMessage = msg;
        statusMessageTime = System.currentTimeMillis();
    }
}
