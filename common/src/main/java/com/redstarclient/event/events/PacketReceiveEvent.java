// ☭ Red Star Client — PacketReceiveEvent
package com.redstarclient.event.events;

/** Fired when an incoming packet is received. Can be cancelled. */
public class PacketReceiveEvent {
    public final Object packet;
    private boolean cancelled = false;

    public PacketReceiveEvent(Object packet) { this.packet = packet; }
    public void cancel() { this.cancelled = true; }
    public boolean isCancelled() { return cancelled; }
}
