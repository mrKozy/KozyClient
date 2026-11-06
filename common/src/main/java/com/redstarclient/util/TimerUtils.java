// ☭ Red Star Client — TimerUtils
package com.redstarclient.util;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Timing utilities for CPS limiting and per-action cooldowns.
 */
public class TimerUtils {

    private long lastActionTime = 0L;

    /**
     * Returns true (and records the action) if the time since the last action
     * allows for {@code actionsPerSecond} actions within one second.
     */
    public boolean canAct(int actionsPerSecond) {
        if (actionsPerSecond <= 0) return false;
        long now     = System.currentTimeMillis();
        long interval = 1000L / actionsPerSecond;
        if (now - lastActionTime >= interval) {
            lastActionTime = now;
            return true;
        }
        return false;
    }

    public void reset() {
        lastActionTime = 0L;
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    // ── CPS Tracker ──────────────────────────────────────────────────────────

    /**
     * Tracks clicks per second using a rolling 1-second window.
     */
    public static class CPSTracker {
        private final LinkedList<Long> clicks = new LinkedList<>();

        /** Record a click event at the current timestamp. */
        public void click() {
            clicks.addLast(System.currentTimeMillis());
        }

        /** Returns the number of clicks recorded within the last 1000 ms. */
        public int getCPS() {
            long cutoff = System.currentTimeMillis() - 1000L;
            clicks.removeIf(t -> t < cutoff);
            return clicks.size();
        }
    }
}
