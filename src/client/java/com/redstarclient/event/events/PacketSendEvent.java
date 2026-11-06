// ☭ Red Star Client — PacketSendEvent
package com.redstarclient.event.events;

/** Fired before an outgoing packet is sent. Can be cancelled. */
public class PacketSendEvent {
    public final Object packet;
    private boolean cancelled = false;

    public PacketSendEvent(Object packet) { this.packet = packet; }
    public void cancel() { this.cancelled = true; }
    public boolean isCancelled() { return cancelled; }
}
