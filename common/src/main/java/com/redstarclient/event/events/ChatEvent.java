// ☭ Red Star Client — ChatEvent
package com.redstarclient.event.events;

/** Fired when a chat message is received. Can be cancelled to suppress display. */
public class ChatEvent {
    public final String message;
    private boolean cancelled = false;

    public ChatEvent(String message) { this.message = message; }
    public void cancel() { this.cancelled = true; }
    public boolean isCancelled() { return cancelled; }
}
