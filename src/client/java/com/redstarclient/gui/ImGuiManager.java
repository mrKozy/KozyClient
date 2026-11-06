// ☭ Red Star Client — ImGuiManager
package com.redstarclient.gui;

import imgui.ImFont;
import imgui.ImFontAtlas;
import imgui.ImFontConfig;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Manages ImGui context initialization, font loading, and frame lifecycle.
 * Must be initialized before any ImGui calls.
 */
public class ImGuiManager {
    
    private static ImGuiImplGlfw imguiGlfw;
    private static ImGuiImplGl3 imguiGl3;
    private static boolean initialized = false;
    private static boolean frameStarted = false;
    private static boolean fontsUploaded = false;
    private static ImFont defaultFont;
    private static long windowHandle = 0;
    private static int skipFrames = 3; // Skip first few loading frames until OpenGL viewport settles
    
    private ImGuiManager() {}
    
    /**
     * Initialize ImGui context with GLFW and OpenGL3 backends.
     * @param windowHandle GLFW window handle from Minecraft
     */
    public static void initialize(long windowHandle) {
        if (initialized) {
            System.out.println("[☭ Red Star Client] ImGui already initialized!");
            return;
        }
        
        ImGuiManager.windowHandle = windowHandle;
        
        try {
            // Create ImGui context
            ImGui.createContext();
            
            // Configure ImGui IO
            ImGuiIO io = ImGui.getIO();
            io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
            io.setIniFilename(null); // Disable imgui.ini file
            
            // Apply Communist glassmorphic theme to base ImGuiStyle
            com.redstarclient.theme.CommunistTheme.applyToStyle(ImGui.getStyle());
            
            // Setup fonts with Cyrillic, symbols, and FontAwesome icons
            setupFonts(io);
            
            // Initialize GLFW backend
            imguiGlfw = new ImGuiImplGlfw();
            imguiGlfw.init(windowHandle, true);
            
            // Initialize OpenGL3 backend
            imguiGl3 = new ImGuiImplGl3();
            imguiGl3.init("#version 150");
            
            // Chained GLFW input callbacks to guarantee input handling on any Minecraft version (1.21.4, 26.3, etc.)
            // Chained GLFW input callbacks to guarantee input handling on any Minecraft version (1.21.4, 26.3, etc.)
            try {
                org.lwjgl.glfw.GLFWKeyCallback[] prevKeyHolder = new org.lwjgl.glfw.GLFWKeyCallback[1];
                prevKeyHolder[0] = org.lwjgl.glfw.GLFW.glfwSetKeyCallback(windowHandle, (win, key, scancode, action, mods) -> {
                    if (action == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                        boolean consumed = com.redstarclient.RedStarClient.handleKeyInput(key);
                        if (!consumed) {
                            com.redstarclient.RedStarClient.MODULES.onKeyPress(key);
                            com.redstarclient.RedStarClient.EVENT_BUS.post(new com.redstarclient.event.events.KeyPressEvent(key, action));
                        }
                    }
                    if (prevKeyHolder[0] != null) {
                        prevKeyHolder[0].invoke(win, key, scancode, action, mods);
                    }
                });
            } catch (Throwable t) {
                System.err.println("[☭ Red Star Client] Warning: Could not chain GLFW callbacks: " + t.getMessage());
            }

            initialized = true;
            fontsUploaded = false;
            System.out.println("[☭ Red Star Client] ImGui initialized successfully with modern typography & icons!");
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] Failed to initialize ImGui: " + t.getMessage());
            t.printStackTrace();
            initialized = false;
        }
    }

    private static void setupFonts(ImGuiIO io) {
        ImFontAtlas fontAtlas = io.getFonts();
        fontAtlas.clear();
        defaultFont = null;

        try {
            // Custom glyph ranges: Basic Latin + Latin Supplement + Cyrillic + Symbols (★, ☭, ⚔, ⚙, etc.) + Dingbats (✎, ✕)
            short[] baseRanges = new short[] {
                0x0020, 0x00FF, // Basic Latin + Latin Supplement
                0x0400, 0x052F, // Cyrillic + Cyrillic Supplement
                0x2000, 0x206F, // General Punctuation
                0x25A0, 0x26FF, // Geometric Shapes & Misc Symbols
                0x2700, 0x27BF, // Dingbats (✎, ✕, etc.)
                0
            };
            
            byte[] fontBytes = loadResourceBytes("/assets/redstarclient/font.ttf");
            if (fontBytes != null) {
                defaultFont = fontAtlas.addFontFromMemoryTTF(fontBytes, 17.5f, new ImFontConfig(), baseRanges);
            } else {
                File winFont = new File("C:/Windows/Fonts/segoeui.ttf");
                if (winFont.exists()) {
                    defaultFont = fontAtlas.addFontFromFileTTF(winFont.getAbsolutePath(), 17.5f, new ImFontConfig(), baseRanges);
                } else {
                    defaultFont = fontAtlas.addFontDefault();
                }
            }
            
            // Merge FontAwesome 6 Solid icons
            byte[] faBytes = loadResourceBytes("/assets/redstarclient/fa-solid-900.ttf");
            if (faBytes != null) {
                ImFontConfig iconConfig = new ImFontConfig();
                iconConfig.setMergeMode(true);
                iconConfig.setPixelSnapH(true);
                short[] iconRanges = new short[] { (short) 0xe000, (short) 0xf8ff, 0 };
                fontAtlas.addFontFromMemoryTTF(faBytes, 15.0f, iconConfig, iconRanges);
            }

            if (defaultFont == null) {
                defaultFont = fontAtlas.addFontDefault();
            }

            io.setFontDefault(defaultFont);

            if (!fontAtlas.build() || !defaultFont.isLoaded()) {
                System.err.println("[☭ Red Star Client] Custom font atlas was not ready after build. Falling back to the default ImGui font.");
                fontAtlas.clear();
                defaultFont = fontAtlas.addFontDefault();
                io.setFontDefault(defaultFont);
                fontAtlas.build();
            }
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] Font setup warning: " + t.getMessage());
            fontAtlas.clear();
            defaultFont = fontAtlas.addFontDefault();
            io.setFontDefault(defaultFont);
            fontAtlas.build();
        }
    }

    private static byte[] loadResourceBytes(String path) {
        try (InputStream in = ImGuiManager.class.getResourceAsStream(path)) {
            if (in != null) {
                return in.readAllBytes();
            }
        } catch (Throwable ignored) {}
        
        try {
            File f = new File("c:/Users/Administrator/Desktop/Client/common/src/main/resources" + path);
            if (f.exists()) {
                return Files.readAllBytes(f.toPath());
            }
        } catch (Throwable ignored) {}
        
        return null;
    }
    
    /**
     * Begin a new ImGui frame. Call this before any ImGui rendering.
     * @return true if frame started successfully, false otherwise
     */
    public static boolean beginFrame() {
        if (!initialized) {
            return false;
        }
        
        // Skip initial frames during splash loading
        if (skipFrames > 0) {
            skipFrames--;
            return false;
        }
        
        if (frameStarted) {
            System.err.println("[☭ Red Star Client] Warning: previous ImGui frame was still open. Forcing cleanup before starting a new frame.");
            endFrame();
        }
        
        try {
            if (!ensureFontsReady()) {
                return false;
            }

            imguiGlfw.newFrame();
            if (!fontsUploaded) {
                fontsUploaded = imguiGl3.createFontsTexture();
                if (!fontsUploaded) {
                    System.err.println("[☭ Red Star Client] Failed to upload ImGui font texture.");
                    return false;
                }
            }
            imguiGl3.newFrame();
            ImGui.newFrame();
            frameStarted = true;
            return true;
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] Error in ImGui beginFrame: " + t.getMessage());
            t.printStackTrace();
            frameStarted = false;
            return false;
        }
    }

    private static boolean ensureFontsReady() {
        ImGuiIO io = ImGui.getIO();
        ImFontAtlas fontAtlas = io.getFonts();

        if (defaultFont == null) {
            defaultFont = fontAtlas.addFontDefault();
            io.setFontDefault(defaultFont);
            fontsUploaded = false;
        }

        if (!fontAtlas.isBuilt() || !defaultFont.isLoaded()) {
            fontAtlas.build();
            fontsUploaded = false;
        }

        if (!fontAtlas.isBuilt() || !defaultFont.isLoaded()) {
            System.err.println("[☭ Red Star Client] ImGui fonts are still not ready. Skipping this frame.");
            return false;
        }

        return true;
    }
    
    /**
     * End the current ImGui frame and render. Call this after all ImGui rendering.
     */
    public static void endFrame() {
        if (!initialized || !frameStarted) {
            return;
        }
        frameStarted = false;
        
        try {
            ImGui.render();
            imguiGl3.renderDrawData(ImGui.getDrawData());
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] Error in ImGui endFrame: " + t.getMessage());
            t.printStackTrace();
        }
    }
    
    /**
     * Shutdown ImGui and clean up resources.
     */
    public static void shutdown() {
        if (!initialized) {
            return;
        }
        
        try {
            if (imguiGl3 != null) {
                imguiGl3.shutdown();
            }
            if (imguiGlfw != null) {
                imguiGlfw.shutdown();
            }
            ImGui.destroyContext();
        } catch (Throwable t) {
            System.err.println("[☭ Red Star Client] Error during ImGui shutdown: " + t.getMessage());
        }
        
        initialized = false;
        frameStarted = false;
        fontsUploaded = false;
        defaultFont = null;
        System.out.println("[☭ Red Star Client] ImGui shutdown complete.");
    }
    
    public static boolean isInitialized() {
        return initialized;
    }
    
    public static boolean isFrameStarted() {
        return frameStarted;
    }
    
    public static long getWindowHandle() {
        return windowHandle;
    }
}
