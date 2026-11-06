// ☭ Red Star Client — SpotifyWidget
package com.redstarclient.gui.widget;

import com.redstarclient.theme.CommunistTheme;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

/**
 * ImGui-based Spotify now playing widget.
 */
public class SpotifyWidget {

    private static final int FLAGS =
            ImGuiWindowFlags.NoTitleBar   |
            ImGuiWindowFlags.NoResize     |
            ImGuiWindowFlags.NoScrollbar  |
            ImGuiWindowFlags.AlwaysAutoResize;

    public void render(String artist, String trackName, boolean isPlaying) {
        if (trackName == null || trackName.isEmpty()) return;

        ImGui.setNextWindowPos(10f, 40f);
        if (ImGui.begin("##spotify_widget", FLAGS)) {
            String status = isPlaying ? "▶ " : "⏸ ";
            ImGui.textColored(CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 1f, "☭ " + status);
            ImGui.sameLine();
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1f,
                    (artist != null ? artist + " - " : "") + trackName);
        }
        ImGui.end();
    }
}
