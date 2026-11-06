// ☭ Red Star Client — RenderHudEvent
package com.redstarclient.event.events;

import net.minecraft.client.gui.GuiGraphics;

/** Fired during HUD/GUI rendering (2D overlay pass). */
public class RenderHudEvent {
    public final GuiGraphics graphics;
    public final float tickDelta;
    public RenderHudEvent(GuiGraphics graphics, float tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }
}
