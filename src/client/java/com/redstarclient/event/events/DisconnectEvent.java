// ☭ Red Star Client — DisconnectEvent
package com.redstarclient.event.events;

/** Fired when the player disconnects from a server. */
public class DisconnectEvent {
    public final String reason;
    public DisconnectEvent(String reason) { this.reason = reason; }
}
