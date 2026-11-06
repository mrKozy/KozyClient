// ☭ Red Star Client — ChatFilterModule
package com.redstarclient.module.player;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.ChatEvent;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.LinkedList;
import java.util.regex.Pattern;

/** Filters duplicate, spam, and keyword-matching chat messages. */
public class ChatFilterModule extends Module {

    private final ImBoolean blockDuplicates  = new ImBoolean(true);
    private final ImBoolean blockUrls        = new ImBoolean(false);
    private final ImInt     duplicateWindowMs = new ImInt(5000);
    private final ImString  blockedKeywords  = new ImString("", 512);

    private final LinkedList<DuplicateEntry> recentMessages = new LinkedList<>();

    private static final Pattern URL_PATTERN = Pattern.compile(
            "(https?://|www\\.)\\S+", Pattern.CASE_INSENSITIVE);

    public ChatFilterModule() {
        super("ChatFilter", "Filters spam and duplicate chat messages.", ModuleCategory.PLAYER, -1);
        RedStarClient.EVENT_BUS.subscribe(ChatEvent.class, this::onChat);
    }

    private void onChat(ChatEvent event) {
        if (!isEnabled()) return;
        String msg = event.message;

        // Duplicate filter
        if (blockDuplicates.get()) {
            long cutoff = System.currentTimeMillis() - duplicateWindowMs.get();
            recentMessages.removeIf(e -> e.timestamp < cutoff);
            for (DuplicateEntry de : recentMessages) {
                if (de.message.equals(msg)) { event.cancel(); return; }
            }
            recentMessages.addLast(new DuplicateEntry(msg, System.currentTimeMillis()));
        }

        // URL filter
        if (blockUrls.get() && URL_PATTERN.matcher(msg).find()) {
            event.cancel();
            return;
        }

        // Keyword filter
        String keywords = blockedKeywords.get().trim();
        if (!keywords.isEmpty()) {
            for (String kw : keywords.split(",")) {
                kw = kw.trim().toLowerCase();
                if (!kw.isEmpty() && msg.toLowerCase().contains(kw)) {
                    event.cancel();
                    return;
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Block Duplicates##cf",  blockDuplicates);
        ImGui.sliderInt("Duplicate Window (ms)##cf", duplicateWindowMs.getData(), 500, 30000);
        ImGui.checkbox("Block URLs##cf",        blockUrls);
        ImGui.inputText("Blocked Keywords##cf", blockedKeywords);
        ImGui.textDisabled("Separate keywords with commas.");
    }

    private static class DuplicateEntry {
        final String message;
        final long   timestamp;
        DuplicateEntry(String m, long t) { message = m; timestamp = t; }
    }
}
