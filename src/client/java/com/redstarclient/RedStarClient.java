// ☭ Red Star Client — RedStarClient
package com.redstarclient;

import com.redstarclient.config.ConfigManager;
import com.redstarclient.event.EventBus;
import com.redstarclient.gui.RedStarGui;
import com.redstarclient.module.ModuleManager;
import net.minecraft.client.Minecraft;

/**
 * Main singleton for ☭ Red Star Client.
 * Holds references to core systems: modules, event bus, config, and GUI.
 */
public final class RedStarClient {

    public static final String MOD_ID      = "redstarclient";
    public static final String MOD_NAME    = "☭ Red Star Client";
    public static final String VERSION     = "1.0.1";

    public static final ModuleManager MODULES   = new ModuleManager();
    public static final EventBus      EVENT_BUS = new EventBus();
    public static final ConfigManager CONFIG    = new ConfigManager();

    public static RedStarGui GUI;
    public static int menuKey = org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
    public static com.redstarclient.module.Module bindingModule = null;
    public static boolean bindingMenuKey = false;

    private static boolean initialized = false;

    private RedStarClient() {}

    /**
     * Handles keyboard input for interactive rebinding and menu toggle.
     * @return true if the key event was consumed
     */
    public static boolean handleKeyInput(int key) {
        if (bindingMenuKey) {
            if (key != org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
                menuKey = key;
            }
            bindingMenuKey = false;
            CONFIG.save();
            return true;
        }

        if (bindingModule != null) {
            if (key == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE ||
                key == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE ||
                key == org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE) {
                bindingModule.setKeybind(-1);
            } else {
                bindingModule.setKeybind(key);
            }
            bindingModule = null;
            CONFIG.save();
            return true;
        }

        if (key == menuKey || key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT || key == org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL || key == org.lwjgl.glfw.GLFW.GLFW_KEY_U || key == org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT) {
            if (GUI != null) {
                GUI.toggleMenu();
            }
            return true;
        }

        return false;
    }

    /** Called once by the loader-specific entrypoint on the client. */
    public static void init() {
        if (initialized) return;
        initialized = true;

        MODULES.registerDefaults();
        CONFIG.load();
        GUI = new RedStarGui();
        cleanOldZipPacks();
        optimizePerformanceSettings();

        System.out.println("[☭ Red Star Client] v" + VERSION + " initialized — Workers of the world, unite!");
    }

    private static void optimizePerformanceSettings() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null) {
                mc.options.inactivityFpsLimit().set(net.minecraft.client.InactivityFpsLimit.MINIMIZED);
                mc.options.enableVsync().set(false);
                mc.options.framerateLimit().set(260);
            }
        } catch (Throwable ignored) {}
    }

    private static void cleanOldZipPacks() {
        try {
            java.io.File gameDir = Minecraft.getInstance().gameDirectory;
            if (gameDir == null) gameDir = new java.io.File(System.getProperty("user.dir"));
            java.io.File packDir = new java.io.File(gameDir, "resourcepacks");
            java.io.File oldZip = new java.io.File(packDir, "RedPvP-Cartoon_1.21.4.zip");
            if (oldZip.exists()) {
                oldZip.delete();
            }
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.options != null && mc.options.resourcePacks != null) {
                if (mc.options.resourcePacks.remove("file/RedPvP-Cartoon_1.21.4.zip")) {
                    mc.options.save();
                }
            }
        } catch (Throwable ignored) {}
    }

    /** Called on game shutdown to persist config. */
    public static void shutdown() {
        CONFIG.save();
    }

    public static void openHudEditor() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (GUI != null && GUI.isMenuOpen()) {
                GUI.toggleMenu();
            }
            if (mc.gui != null) {
                mc.gui.setScreen(new com.redstarclient.gui.hud.HudEditorScreen());
            }
        }
    }

    /** Convenience accessor for the Minecraft instance. */
    public static Minecraft mc() {
        return Minecraft.getInstance();
    }
}
