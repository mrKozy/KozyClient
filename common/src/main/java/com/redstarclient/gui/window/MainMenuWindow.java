// ☭ Red Star Client — MainMenuWindow
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Modern, compact glassmorphic Soviet ClickGUI.
 * Streamlined layout with zero nested scrollbars, quick category toggles,
 * 1-click module activation for HUD/Render/Combat, and instant search.
 */
public class MainMenuWindow {

    private final ImString searchFilter = new ImString("", 128);
    private final ImString configProfileName = new ImString("default", 64);
    private ModuleCategory selectedCategory = ModuleCategory.COMBAT;
    private boolean isSettingsSelected = false;
    private Module popupSettingsModule = null;
    private final Set<String> expandedModules = new HashSet<>();
    private String statusMessage = "";
    private long statusMessageTime = 0L;

    public void render() {
        ImGui.setNextWindowSize(720, 460, ImGuiCond.FirstUseEver);
        ImGui.setNextWindowPos(140, 90, ImGuiCond.FirstUseEver);

        int windowFlags = ImGuiWindowFlags.NoCollapse | ImGuiWindowFlags.NoTitleBar;

        boolean open = ImGui.begin("##RedStarClientMain", windowFlags);
        if (!open) {
            ImGui.end();
            return;
        }

        // ── 1. Compact Header Bar ─────────────────────────────────────────────
        renderHeader();

        ImGui.spacing();

        // ── 2. Split Body Layout: Left Sidebar & Right Content Deck ───────────
        float totalWidth = ImGui.getContentRegionAvailX();
        float sidebarWidth = 165.0f;
        float contentWidth = totalWidth - sidebarWidth - 10.0f;
        float bodyHeight = ImGui.getContentRegionAvailY() - 28.0f;

        // Left Sidebar
        ImGui.beginChild("##Sidebar", sidebarWidth, bodyHeight, true);
        renderSidebar();
        ImGui.endChild();

        ImGui.sameLine();

        // Right Main Content Deck (Single smooth scrollbar for content)
        ImGui.beginChild("##ContentDeck", contentWidth, bodyHeight, true);
        if (isSettingsSelected) {
            renderSettingsContent();
        } else {
            renderModulesContent();
        }
        ImGui.endChild();

        // ── 3. Sleek Footer ──────────────────────────────────────────────────
        renderFooter();

        ImGui.end();

        // ── 4. Dedicated Rectangular Settings Pop-up Window ──────────────────
        if (popupSettingsModule != null) {
            renderModuleSettingsPopup();
        }
    }

    private void renderHeader() {
        // Red Star Emblem & Title
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                Icons.STAR + " RED STAR");
        ImGui.sameLine();
        ImGui.textDisabled("// v" + RedStarClient.VERSION);

        // Header Freecam quick exit button
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

        // Header Right: Status chip & Close button
        int activeCount = 0;
        for (Module m : RedStarClient.MODULES.getAll()) {
            if (m.isEnabled()) activeCount++;
        }

        String activeText = "● " + activeCount + " ACTIVE";
        float closeBtnWidth = 24.0f;
        float badgeWidth = ImGui.calcTextSize(activeText).x + 14.0f;

        ImGui.sameLine(ImGui.getWindowWidth() - badgeWidth - closeBtnWidth - 28.0f);
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, activeText);

        ImGui.sameLine(ImGui.getWindowWidth() - closeBtnWidth - 18.0f);
        if (ImGui.smallButton(Icons.TIMES + "##close")) {
            RedStarClient.GUI.toggleMenu();
        }
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.textUnformatted("Close Menu (Right Shift)");
            ImGui.endTooltip();
        }

        ImGui.separator();
    }

    private void renderSidebar() {
        ImGui.textDisabled("CATEGORIES");
        ImGui.spacing();

        float btnWidth = ImGui.getContentRegionAvailX();

        for (ModuleCategory cat : ModuleCategory.values()) {
            boolean active = (!isSettingsSelected && selectedCategory == cat);

            if (active) {
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.95f);
                ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
            }

            String label = " " + cat.icon + "  " + cat.displayName;
            if (ImGui.button(label + "##cat", btnWidth, 29.0f)) {
                selectedCategory = cat;
                isSettingsSelected = false;
            }

            if (active) {
                ImGui.popStyleColor(2);
            }
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        // Settings Button
        boolean settingsActive = isSettingsSelected;
        if (settingsActive) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.95f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
        }

        if (ImGui.button(" " + Icons.COG + "  Settings##nav", btnWidth, 29.0f)) {
            isSettingsSelected = true;
        }

        if (settingsActive) {
            ImGui.popStyleColor(2);
        }

        ImGui.spacing();

        // ✎ Edit HUD Button
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.85f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
        if (ImGui.button(" ✎  Edit HUD##nav", btnWidth, 29.0f)) {
            RedStarClient.openHudEditor();
        }
        ImGui.popStyleColor(2);
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.textUnformatted("Drag HUD elements with mouse! Right-click or ESC to exit.");
            ImGui.endTooltip();
        }

        // Quick Bottom Action
        float remainingY = ImGui.getContentRegionAvailY();
        if (remainingY > 38.0f) {
            ImGui.dummy(0, remainingY - 32.0f);
        }

        ImGui.separator();
        if (ImGui.button("Disable All##quick", btnWidth, 24.0f)) {
            for (Module m : RedStarClient.MODULES.getAll()) {
                m.setEnabled(false);
            }
            showStatus("All modules disabled.");
        }
    }

    private void renderModulesContent() {
        // Top Search Bar + Category Action Buttons
        ImGui.pushItemWidth(ImGui.getContentRegionAvailX() - 170.0f);
        ImGui.inputText("##search", searchFilter);
        ImGui.popItemWidth();

        String filter = searchFilter.get().toLowerCase().trim();
        if (filter.isEmpty()) {
            ImGui.sameLine(8f);
            ImGui.textDisabled(Icons.SEARCH + " Search...");
        }

        // Quick Category Toggles: [ + All On ] [ - All Off ]
        ImGui.sameLine(ImGui.getContentRegionAvailX() - 150.0f);
        if (ImGui.smallButton("+ All On##cat")) {
            for (Module m : RedStarClient.MODULES.getByCategory(selectedCategory)) {
                m.setEnabled(true);
            }
            showStatus("Enabled all " + selectedCategory.displayName + " modules.");
        }
        ImGui.sameLine();
        if (ImGui.smallButton("- All Off##cat")) {
            for (Module m : RedStarClient.MODULES.getByCategory(selectedCategory)) {
                m.setEnabled(false);
            }
            showStatus("Disabled all " + selectedCategory.displayName + " modules.");
        }

        ImGui.separator();
        ImGui.spacing();

        // Module Cards (Clean & Compact with NO nested scrollbars)
        List<Module> modules = filter.isEmpty()
                ? RedStarClient.MODULES.getByCategory(selectedCategory)
                : RedStarClient.MODULES.getAll();

        for (Module m : modules) {
            if (!filter.isEmpty() && !m.getName().toLowerCase().contains(filter) &&
                !m.getDescription().toLowerCase().contains(filter)) {
                continue;
            }

            renderModuleCard(m);
            ImGui.spacing();
        }
    }

    private void renderModuleCard(Module m) {
        ImGui.pushID(m.getName());

        boolean enabled = m.isEnabled();
        boolean isExpanded = expandedModules.contains(m.getName());
        float cardWidth = ImGui.getContentRegionAvailX();

        // Styled Compact Card Frame
        if (enabled) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.30f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 0.70f);
        } else {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg, CommunistTheme.CARD_GLASS[0], CommunistTheme.CARD_GLASS[1], CommunistTheme.CARD_GLASS[2], 0.55f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border, CommunistTheme.BORDER_GOLD[0], CommunistTheme.BORDER_GOLD[1], CommunistTheme.BORDER_GOLD[2], 0.20f);
        }

        // Push tighter padding inside card to guarantee zero scrollbars
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8.0f, 5.0f);

        // Fixed 42px height when collapsed, auto-size when expanded
        float childHeight = isExpanded ? 0f : 42.0f;
        int cardFlags = ImGuiWindowFlags.NoScrollbar;
        if (isExpanded) {
            cardFlags |= ImGuiWindowFlags.AlwaysAutoResize;
        }

        ImGui.beginChild("##card_" + m.getName(), cardWidth, childHeight, true, cardFlags);

        // ── Single-line compact row: Toggle, Name, Description, Keybind, Settings Chevron ──
        String toggleLabel = enabled ? "[ ON ]" : "[ OFF ]";
        if (enabled) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
        }

        // 1-Click Toggle Button
        if (ImGui.button(toggleLabel + "##toggle", 52.0f, 22.0f)) {
            m.toggle();
        }

        if (enabled) {
            ImGui.popStyleColor(2);
        }

        // Clickable Module Name (Left-click toggles, Right-click opens popup)
        ImGui.sameLine();
        if (enabled) {
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f, m.getName());
        } else {
            ImGui.textColored(CommunistTheme.TEXT_PRIMARY[0], CommunistTheme.TEXT_PRIMARY[1], CommunistTheme.TEXT_PRIMARY[2], 1.0f, m.getName());
        }

        if (ImGui.isItemClicked(0)) {
            m.toggle();
        } else if (ImGui.isItemClicked(1)) {
            popupSettingsModule = m;
        }

        // Right side controls width
        float rightControlsWidth = 110.0f;
        float currentX = ImGui.getCursorPosX();
        float availForDesc = cardWidth - currentX - rightControlsWidth - 14.0f;

        if (availForDesc > 40.0f) {
            ImGui.sameLine();
            String desc = m.getDescription();
            int maxChars = (int) (availForDesc / 7.2f);
            if (desc.length() > maxChars && maxChars > 6) {
                desc = desc.substring(0, maxChars - 3) + "...";
            }
            ImGui.textDisabled("— " + desc);
        }

        // Pinned to right side (no overlapping)
        ImGui.sameLine(cardWidth - rightControlsWidth);

        boolean isBindingThis = (RedStarClient.bindingModule == m);
        String bindText = isBindingThis ? "[ PRESS ]" : "[" + KeyUtils.getKeyName(m.getKeybind()) + "]";

        if (isBindingThis) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.1f, 0.05f, 0.05f, 1.0f);
        }

        if (ImGui.smallButton(bindText + "##bind")) {
            if (isBindingThis) {
                RedStarClient.bindingModule = null;
            } else {
                RedStarClient.bindingModule = m;
                RedStarClient.bindingMenuKey = false;
            }
        }

        if (isBindingThis) {
            ImGui.popStyleColor(2);
        }

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.textUnformatted("Click to bind key. Press ESC/BKSP to clear.");
            ImGui.endTooltip();
        }

        // Dedicated Settings Button (Opens Pop-up Window)
        ImGui.sameLine();
        if (ImGui.smallButton(Icons.COG + "##settingsPopup")) {
            popupSettingsModule = m;
        }
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.textUnformatted("Einstellungen (Fenster oeffnen)");
            ImGui.endTooltip();
        }

        // Expand inline toggle button
        ImGui.sameLine();
        String chevron = isExpanded ? Icons.CHEVRON_DOWN : Icons.CHEVRON_RIGHT;
        if (ImGui.smallButton(chevron + "##expand")) {
            if (isExpanded) {
                expandedModules.remove(m.getName());
            } else {
                expandedModules.add(m.getName());
            }
        }

        // ── Expanded Settings Area ───────────────────────────────────────────
        if (isExpanded) {
            ImGui.spacing();
            ImGui.separator();
            ImGui.spacing();
            ImGui.indent(8.0f);
            m.renderImGuiConfig();
            ImGui.unindent(8.0f);
            ImGui.spacing();
        }

        ImGui.endChild();
        ImGui.popStyleVar(); // Pop WindowPadding
        ImGui.popStyleColor(2); // Pop ChildBg & Border
        ImGui.popID();
    }

    private void renderSettingsContent() {
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                Icons.COG + " Client Configuration & Keybindings");
        ImGui.separator();
        ImGui.spacing();

        // 1. Menu Rebind Card
        ImGui.beginChild("##KeybindCard", 0, 75.0f, true, ImGuiWindowFlags.NoScrollbar);
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                Icons.KEY + " Primary Keybindings");
        ImGui.spacing();

        boolean isBindingMenu = RedStarClient.bindingMenuKey;
        String menuKeyLabel = isBindingMenu ? "[ PRESS ANY KEY FOR MENU ]" : "ClickGUI Key: [" + KeyUtils.getKeyName(RedStarClient.menuKey) + "]";

        if (isBindingMenu) {
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Button, CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f);
            ImGui.pushStyleColor(imgui.flag.ImGuiCol.Text, 0.1f, 0.05f, 0.05f, 1.0f);
        }

        if (ImGui.button(menuKeyLabel, 240.0f, 26.0f)) {
            RedStarClient.bindingMenuKey = !isBindingMenu;
            RedStarClient.bindingModule = null;
        }

        if (isBindingMenu) {
            ImGui.popStyleColor(2);
        }

        ImGui.sameLine();
        ImGui.textDisabled("Zoom: Hold C");
        ImGui.endChild();

        ImGui.spacing();

        // 2. Profile & Config Storage Card (C:\Redstar Client configs)
        ImGui.beginChild("##ConfigCard", 0, 150.0f, true, ImGuiWindowFlags.NoScrollbar);
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                Icons.BOX + " Configuration Profiles (C:\\Redstar Client configs)");
        ImGui.spacing();

        ImGui.pushItemWidth(170.0f);
        ImGui.inputText("Profile Name##cfgName", configProfileName);
        ImGui.popItemWidth();

        ImGui.sameLine();
        if (ImGui.button(Icons.CHECK + " Save Profile", 110.0f, 24.0f)) {
            boolean ok = RedStarClient.CONFIG.saveConfig(configProfileName.get());
            if (ok) {
                showStatus("Profile '" + configProfileName.get() + "' saved successfully!");
            } else {
                showStatus("Failed to save profile.");
            }
        }

        ImGui.sameLine();
        if (ImGui.button(Icons.TERMINAL + " Load Profile", 110.0f, 24.0f)) {
            boolean ok = RedStarClient.CONFIG.loadConfig(configProfileName.get());
            if (ok) {
                showStatus("Profile '" + configProfileName.get() + "' loaded successfully!");
            } else {
                showStatus("Profile not found.");
            }
        }

        ImGui.sameLine();
        if (ImGui.button("Open Folder", 95.0f, 24.0f)) {
            try {
                java.awt.Desktop.getDesktop().open(RedStarClient.CONFIG.getConfigDirectory().toFile());
            } catch (Throwable t) {
                showStatus("Folder: " + RedStarClient.CONFIG.getConfigDirectory());
            }
        }

        ImGui.spacing();
        List<String> configs = RedStarClient.CONFIG.listConfigs();
        if (!configs.isEmpty()) {
            ImGui.textDisabled("Quick Select:");
            for (String cfgName : configs) {
                ImGui.sameLine();
                if (ImGui.smallButton(cfgName + "##loadbtn")) {
                    configProfileName.set(cfgName);
                    RedStarClient.CONFIG.loadConfig(cfgName);
                    showStatus("Loaded: " + cfgName);
                }
            }
        }

        ImGui.spacing();
        if (ImGui.button("Reset All Modules (Uncheck All)", 220.0f, 24.0f)) {
            for (Module m : RedStarClient.MODULES.getAll()) {
                m.setEnabled(false);
            }
            showStatus("All modules disabled (clean slate).");
        }

        ImGui.spacing();
        if (!statusMessage.isEmpty() && System.currentTimeMillis() - statusMessageTime < 4000L) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, Icons.CHECK + " " + statusMessage);
        } else {
            ImGui.textDisabled("Profiles are saved locally in " + RedStarClient.CONFIG.getConfigDirectory().toAbsolutePath());
        }

        ImGui.endChild();

        ImGui.spacing();

        // 3. Custom PvP Resource Pack Card (Wonder-Red Edition - Native)
        ImGui.beginChild("##ResourcePackCard", 0, 75.0f, true, ImGuiWindowFlags.NoScrollbar);
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                Icons.BOX + " Wonder-Red PvP Textures (Natively Embedded in Client)");
        ImGui.spacing();
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Status: ● NATIVELY EMBEDDED (Short swords, Red GUI, Low Fire, Crisp Hit Particles)");
        ImGui.textDisabled("Built directly into Red Star Client JAR — No external zip files required.");
        ImGui.endChild();

        ImGui.spacing();

        // 4. Proletariat Info Card
        ImGui.beginChild("##InfoCard", 0, 85.0f, true, ImGuiWindowFlags.NoScrollbar);
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                Icons.STAR + " ☭ Red Star Client // Manifesto");
        ImGui.spacing();
        ImGui.text("Crafted with Dear ImGui for Minecraft Java Edition.");
        ImGui.textDisabled("For the Proletariat, by the Proletariat. All power to the Soviets!");
        ImGui.endChild();
    }

    private void renderFooter() {
        ImGui.separator();
        ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 0.70f,
                Icons.STAR + " \"From each according to ability, to each according to need.\"");
        ImGui.sameLine(ImGui.getWindowWidth() - 170.0f);
        ImGui.textDisabled("Minecraft 1.21.4 // Fabric");
    }

    /**
     * Dedicated floating rectangular pop-up window for module configuration.
     */
    private void renderModuleSettingsPopup() {
        if (popupSettingsModule == null) return;

        // Glassmorphic styling for the rectangular settings popup window
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.WindowBg, CommunistTheme.BG_GLASS[0], CommunistTheme.BG_GLASS[1], CommunistTheme.BG_GLASS[2], 0.82f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.Border, CommunistTheme.BORDER_RED[0], CommunistTheme.BORDER_RED[1], CommunistTheme.BORDER_RED[2], 0.85f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBg, CommunistTheme.DARK_RED[0], CommunistTheme.DARK_RED[1], CommunistTheme.DARK_RED[2], 0.90f);
        ImGui.pushStyleColor(imgui.flag.ImGuiCol.TitleBgActive, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 0.95f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 2.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 12.0f);

        int popupFlags = ImGuiWindowFlags.NoCollapse;
        ImBoolean pOpen = new ImBoolean(true);
        String windowTitle = "★ " + popupSettingsModule.getName() + " — Einstellungen##ModulePopup";

        if (ImGui.begin(windowTitle, pOpen, popupFlags)) {
            // Header Bar inside popup
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    Icons.COG + "  " + popupSettingsModule.getName().toUpperCase());
            ImGui.sameLine();
            ImGui.textDisabled("[" + popupSettingsModule.getCategory().displayName + "]");

            float closeBtnWidth = 32.0f;
            ImGui.sameLine(ImGui.getWindowWidth() - closeBtnWidth - 14.0f);
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
            String bindText = isBindingThis ? "[ TASTE DRUECKEN ]" : "Keybind: [" + KeyUtils.getKeyName(popupSettingsModule.getKeybind()) + "]";
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

    private void showStatus(String msg) {
        statusMessage = msg;
        statusMessageTime = System.currentTimeMillis();
    }
}
