// ☭ Red Star Client — DraggableHudElement
package com.redstarclient.gui.hud;

/**
 * Interface implemented by all HUD modules that can be moved with the mouse in the HUD Editor.
 */
public interface DraggableHudElement {
    String getHudName();
    int getHudX();
    int getHudY();
    int getHudWidth();
    int getHudHeight();
    void setHudPos(int x, int y);
    boolean isHudEnabled();
}
