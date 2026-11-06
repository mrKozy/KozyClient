// ☭ Red Star Client — TickEvent
package com.redstarclient.event.events;

import net.minecraft.client.Minecraft;

/** Fired every game tick. */
public class TickEvent {
    public final Minecraft mc;
    public TickEvent(Minecraft mc) { this.mc = mc; }
}
