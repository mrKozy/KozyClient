// ☭ Red Star Client — KeyPressEvent
package com.redstarclient.event.events;

import org.lwjgl.glfw.GLFW;

/** Fired on keyboard input events. */
public class KeyPressEvent {
    public final int key;
    public final int action; // GLFW.GLFW_PRESS, GLFW.GLFW_RELEASE, GLFW.GLFW_REPEAT

    public KeyPressEvent(int key, int action) {
        this.key = key;
        this.action = action;
    }

    public boolean isPress()   { return action == GLFW.GLFW_PRESS; }
    public boolean isRelease() { return action == GLFW.GLFW_RELEASE; }
    public boolean isRepeat()  { return action == GLFW.GLFW_REPEAT; }
}
