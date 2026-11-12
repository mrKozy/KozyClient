// ☭ Red Star Client — RedStarGui
package com.redstarclient.gui;

import com.redstarclient.gui.window.MainMenuWindow;
import com.redstarclient.gui.widget.WatermarkWidget;
import com.redstarclient.theme.CommunistTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

/**
 * ImGui frame controller. Rendered every frame from the GameRenderer mixin.
 */
public class RedStarGui {

    private boolean menuOpen = false;
    private boolean menuKeyDownLastFrame = false;
    private final java.util.Set<Integer> keysDownLastFrame = new java.util.HashSet<>();

    private final MainMenuWindow  mainMenu  = new MainMenuWindow();
    private final WatermarkWidget watermark = new WatermarkWidget();
    private final com.redstarclient.gui.widget.SpotifyWidget spotifyWidget = new com.redstarclient.gui.widget.SpotifyWidget();

    /** Direct native GLFW key polling to guarantee menu toggle and module keybinds on any version. */
    public void checkDirectKeyInputs() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;
        long window = mc.getWindow().handle();
        if (window == 0) return;

        boolean lshift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;
        boolean rshift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        boolean shift  = lshift || rshift;
        boolean insert = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_INSERT) == GLFW.GLFW_PRESS;
        boolean custom = com.redstarclient.RedStarClient.menuKey > 0 && GLFW.glfwGetKey(window, com.redstarclient.RedStarClient.menuKey) == GLFW.GLFW_PRESS;

        // Shift + Insert toggles StreamProof emergency bypass
        if (shift && (insert || custom)) {
            if (!menuKeyDownLastFrame) {
                com.redstarclient.module.render.StreamProofModule.toggleBypass();
            }
            menuKeyDownLastFrame = true;
            return;
        }

        boolean anyMenuDown = insert || rshift || custom;
        if (anyMenuDown && !menuKeyDownLastFrame) {
            toggleMenu();
        }
        menuKeyDownLastFrame = anyMenuDown;

        // If in a screen (e.g. Chat, ClickGUI, Pause), do not dispatch in-game module hotkeys
        if (mc.gui != null && mc.gui.screen() != null) {
            keysDownLastFrame.clear();
            return;
        }

        java.util.Set<Integer> currentKeys = new java.util.HashSet<>();

        // Priority 1: Check Freecam exit keys (N, M, or Escape)
        com.redstarclient.module.render.FreecamModule freecam = com.redstarclient.RedStarClient.MODULES.get(com.redstarclient.module.render.FreecamModule.class);
        if (freecam != null && freecam.isEnabled()) {
            boolean nDown   = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_N) == GLFW.GLFW_PRESS;
            boolean mDown   = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
            boolean escDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;

            boolean wasNDown   = keysDownLastFrame.contains(GLFW.GLFW_KEY_N);
            boolean wasMDown   = keysDownLastFrame.contains(GLFW.GLFW_KEY_M);
            boolean wasEscDown = keysDownLastFrame.contains(GLFW.GLFW_KEY_ESCAPE);

            if (nDown)   currentKeys.add(GLFW.GLFW_KEY_N);
            if (mDown)   currentKeys.add(GLFW.GLFW_KEY_M);
            if (escDown) currentKeys.add(GLFW.GLFW_KEY_ESCAPE);

            if ((nDown && !wasNDown) || (mDown && !wasMDown) || (escDown && !wasEscDown)) {
                freecam.setEnabled(false);
                keysDownLastFrame.clear();
                keysDownLastFrame.addAll(currentKeys);
                return;
            }
        }

        // Priority 2: Dispatch module keybinds
        for (com.redstarclient.module.Module m : com.redstarclient.RedStarClient.MODULES.getAll()) {
            int key = m.getKeybind();
            if (key > 0) {
                boolean isDown = GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
                if (isDown) {
                    currentKeys.add(key);
                    if (!keysDownLastFrame.contains(key)) {
                        m.toggle();
                    }
                }
            }
        }

        // Priority 3: Fallback key 'M' to toggle Freecam if disabled
        if (freecam != null && !freecam.isEnabled()) {
            boolean mDown = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
            if (mDown) {
                currentKeys.add(GLFW.GLFW_KEY_M);
                if (!keysDownLastFrame.contains(GLFW.GLFW_KEY_M)) {
                    freecam.toggle();
                }
            }
        }

        keysDownLastFrame.clear();
        keysDownLastFrame.addAll(currentKeys);
    }

    /** Called every frame from the GameRenderer mixin. */
    public void renderFrame() {
        Minecraft mc = Minecraft.getInstance();

        // Lazy initialize ImGui if not initialized yet
        if (!ImGuiManager.isInitialized()) {
            if (mc != null && mc.getWindow() != null && mc.getWindow().handle() != 0) {
                ImGuiManager.initialize(mc.getWindow().handle());
            }
            if (!ImGuiManager.isInitialized()) {
                return;
            }
        }

        // Poll keys directly from native GLFW
        checkDirectKeyInputs();

        if (mc != null && mc.gui != null && mc.gui.screen() instanceof RedStarMenuScreen) {
            menuOpen = true;
        }

        // Start ImGui frame (returns false if skipping early frames or error)
        if (!ImGuiManager.beginFrame()) {
            return;
        }

        boolean themeApplied = false;
        try {
            // Apply Soviet theme
            CommunistTheme.apply();
            themeApplied = true;

            // Splash screen (renders over everything else while active, blocks menu)
            if (SplashScreen.render()) {
                // Splash is active - skip normal UI
                return;
            }

            // Watermark (hidden if StreamProof is active)
            if (!com.redstarclient.module.render.StreamProofModule.shouldHideWatermark()) {
                watermark.render();
            }

            // Main menu (hidden if StreamProof is active)
            if (menuOpen && !com.redstarclient.module.render.StreamProofModule.shouldHideUI()) {
                mainMenu.render();
            }
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] Error in GUI renderFrame: " + t.getMessage());
            t.printStackTrace();
        } finally {
            if (themeApplied) {
                try {
                    CommunistTheme.cleanup();
                } catch (Throwable t) {
                    System.err.println("[☭ Red Star Client] Error during theme cleanup: " + t.getMessage());
                    t.printStackTrace();
                }
            }
            // End ImGui frame and render draw data
            ImGuiManager.endFrame();
        }
    }

    /** Called from HUD render hooks to dispatch module HUD overlays. */
    public void renderHudWidgets(GuiGraphicsExtractor graphics, float tickDelta) {
        // Handled via loader callbacks
    }

    public void toggleMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        net.minecraft.client.gui.screens.Screen currentScreen = mc.gui != null ? mc.gui.screen() : null;

        if (menuOpen) {
            menuOpen = false;
            if (currentScreen instanceof RedStarMenuScreen && mc.gui != null) {
                mc.gui.setScreen(null);
            }
            if (mc.mouseHandler != null && (mc.gui == null || mc.gui.screen() == null)) {
                mc.mouseHandler.grabMouse();
            }
            System.out.println("[☭ Red Star Client] Menu CLOSED.");
        } else {
            menuOpen = true;
            mainMenu.onMenuOpened();
            if (currentScreen == null && mc.gui != null) {
                mc.gui.setScreen(new RedStarMenuScreen());
            }
            if (mc.mouseHandler != null) {
                mc.mouseHandler.releaseMouse();
            }
            System.out.println("[☭ Red Star Client] Menu OPENED. Screen: " + (currentScreen != null ? currentScreen.getClass().getSimpleName() : "null"));
        }
    }

    public void setMenuOpen(boolean open) {
        this.menuOpen = open;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.gui != null) {
            net.minecraft.client.gui.screens.Screen currentScreen = mc.gui.screen();
            if (open) {
                if (!(currentScreen instanceof RedStarMenuScreen)) {
                    mc.gui.setScreen(new RedStarMenuScreen());
                }
            } else {
                if (currentScreen instanceof RedStarMenuScreen) {
                    mc.gui.setScreen(null);
                }
            }
        }
    }

    public boolean isMenuOpen() {
        return menuOpen;
    }
}
