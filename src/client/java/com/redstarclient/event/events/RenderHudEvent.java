// ☭ Red Star Client — RenderHudEvent
package com.redstarclient.event.events;

import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Fired during HUD/GUI rendering (2D overlay pass). */
public class RenderHudEvent {
    public final GuiGraphicsExtractor graphics;
    public final float tickDelta;
    public RenderHudEvent(GuiGraphicsExtractor graphics, float tickDelta) {
        this.graphics = graphics;
        this.tickDelta = tickDelta;
    }
}
