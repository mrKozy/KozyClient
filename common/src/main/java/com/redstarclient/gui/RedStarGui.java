// ☭ Red Star Client — RedStarGui
package com.redstarclient.gui;

import com.redstarclient.gui.window.MainMenuWindow;
import com.redstarclient.gui.widget.WatermarkWidget;
import com.redstarclient.theme.CommunistTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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

    /** Direct native GLFW key polling to guarantee menu toggle and module keybinds on any version. */
    public void checkDirectKeyInputs() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getWindow() == null) return;
        long window = mc.getWindow().getWindow();
        if (window == 0) return;

        boolean rshift = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
        boolean rctrl  = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
        boolean keyU   = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_U) == GLFW.GLFW_PRESS;
        boolean insert = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_INSERT) == GLFW.GLFW_PRESS;
        boolean custom = com.redstarclient.RedStarClient.menuKey > 0 && GLFW.glfwGetKey(window, com.redstarclient.RedStarClient.menuKey) == GLFW.GLFW_PRESS;

        boolean anyMenuDown = rshift || rctrl || keyU || insert || custom;
        if (anyMenuDown && !menuKeyDownLastFrame) {
            toggleMenu();
        }
        menuKeyDownLastFrame = anyMenuDown;

        // If in a screen (e.g. Chat, ClickGUI, Pause), do not dispatch in-game module hotkeys
        if (mc.screen != null) {
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
            if (mc != null && mc.getWindow() != null && mc.getWindow().getWindow() != 0) {
                ImGuiManager.initialize(mc.getWindow().getWindow());
            }
            if (!ImGuiManager.isInitialized()) {
                return;
            }
        }

        // Poll keys directly from native GLFW
        checkDirectKeyInputs();

        if (mc != null && mc.screen instanceof RedStarMenuScreen) {
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

            // Always visible watermark
            watermark.render();

            // Main menu (toggled by keybind or screen)
            if (menuOpen) {
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
    public void renderHudWidgets(GuiGraphics graphics, float tickDelta) {
        // Handled via loader callbacks
    }

    public void toggleMenu() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) return;

        if (mc.screen instanceof RedStarMenuScreen || menuOpen) {
            menuOpen = false;
            if (mc.screen instanceof RedStarMenuScreen) {
                mc.setScreen(null);
            }
            if (mc.mouseHandler != null && mc.screen == null) {
                mc.mouseHandler.grabMouse();
            }
        } else if (mc.screen == null) {
            menuOpen = true;
            mc.setScreen(new RedStarMenuScreen());
            if (mc.mouseHandler != null) {
                mc.mouseHandler.releaseMouse();
            }
        }
    }

    public void setMenuOpen(boolean open) {
        this.menuOpen = open;
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (open) {
                if (!(mc.screen instanceof RedStarMenuScreen)) {
                    mc.setScreen(new RedStarMenuScreen());
                }
            } else {
                if (mc.screen instanceof RedStarMenuScreen) {
                    mc.setScreen(null);
                }
            }
        }
    }

    public boolean isMenuOpen() {
        return menuOpen;
    }
}
