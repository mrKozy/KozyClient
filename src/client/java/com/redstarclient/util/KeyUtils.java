// ☭ Red Star Client — KeyUtils
package com.redstarclient.util;

import org.lwjgl.glfw.GLFW;

/**
 * Key name formatting and translation utilities for GLFW key codes.
 */
public final class KeyUtils {

    private KeyUtils() {}

    /** Returns a concise, readable name for a GLFW key code. */
    public static String getKeyName(int key) {
        if (key <= 0 || key == GLFW.GLFW_KEY_UNKNOWN) return "NONE";

        return switch (key) {
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT  -> "LSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCTRL";
            case GLFW.GLFW_KEY_LEFT_CONTROL  -> "LCTRL";
            case GLFW.GLFW_KEY_RIGHT_ALT   -> "RALT";
            case GLFW.GLFW_KEY_LEFT_ALT    -> "LALT";
            case GLFW.GLFW_KEY_SPACE       -> "SPACE";
            case GLFW.GLFW_KEY_TAB         -> "TAB";
            case GLFW.GLFW_KEY_ENTER       -> "ENTER";
            case GLFW.GLFW_KEY_INSERT      -> "INSERT";
            case GLFW.GLFW_KEY_DELETE      -> "DELETE";
            case GLFW.GLFW_KEY_BACKSPACE   -> "BKSP";
            case GLFW.GLFW_KEY_ESCAPE      -> "ESC";
            case GLFW.GLFW_KEY_CAPS_LOCK   -> "CAPS";
            case GLFW.GLFW_KEY_UP          -> "UP";
            case GLFW.GLFW_KEY_DOWN        -> "DOWN";
            case GLFW.GLFW_KEY_LEFT        -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT       -> "RIGHT";
            default -> {
                String name = GLFW.glfwGetKeyName(key, 0);
                if (name != null && !name.isEmpty()) {
                    yield name.toUpperCase();
                }
                yield "KEY_" + key;
            }
        };
    }
}
