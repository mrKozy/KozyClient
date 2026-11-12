// KozyClient — SpotifyWidget
package com.redstarclient.gui.widget;

import com.redstarclient.gui.Icons;
import com.redstarclient.render.TextureLoader;
import com.redstarclient.util.SpotifyUtils;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;

/**
 * Modern floating glassmorphic Spotify Player featuring high-resolution
 * album cover art, track info, play/pause indicator, and animated audio wave bars.
 */
public class SpotifyWidget {

    private static final int FLAGS =
            ImGuiWindowFlags.NoTitleBar   |
            ImGuiWindowFlags.NoResize     |
            ImGuiWindowFlags.NoScrollbar  |
            ImGuiWindowFlags.AlwaysAutoResize;

    public void render(int posX, int posY, boolean showWaveform) {
        if (!SpotifyUtils.hasTrack()) return;

        String artist = SpotifyUtils.getArtist();
        String track  = SpotifyUtils.getTrackName();
        boolean playing = SpotifyUtils.isPlaying();

        ImGui.setNextWindowPos((float) posX, (float) posY);

        // Glassmorphic dark card with Spotify accent
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.07f, 0.08f, 0.10f, 0.90f);
        ImGui.pushStyleColor(ImGuiCol.Border, 0.11f, 0.73f, 0.33f, 0.65f); // Spotify Green
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 8.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8.0f, 8.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.2f);

        if (ImGui.begin("##spotify_floating_card", FLAGS)) {
            // 1. Album Cover Art (46x46 square)
            String coverUrl = SpotifyUtils.getCoverArtUrl();
            int coverTexId = (coverUrl != null) ? TextureLoader.getUrlTextureId(coverUrl) : 0;

            float coverSize = 46.0f;
            if (coverTexId > 0) {
                ImGui.image(coverTexId, coverSize, coverSize);
                ImGui.sameLine();
            } else {
                // Stylish placeholder icon if cover is loading or offline
                ImGui.pushStyleColor(ImGuiCol.Button, 0.12f, 0.14f, 0.16f, 1.0f);
                ImGui.button(Icons.MUSIC + "##coverPlaceholder", coverSize, coverSize);
                ImGui.popStyleColor();
                ImGui.sameLine();
            }

            // 2. Track & Artist Info
            ImGui.beginGroup();

            // Header: ♪ SPOTIFY [PLAYING / PAUSED]
            String status = playing ? "▶" : "❚❚";
            ImGui.textColored(0.11f, 0.73f, 0.33f, 1.0f, Icons.MUSIC + " SPOTIFY " + status);

            // Track name (Bold white)
            String displayTrack = (track != null && !track.isEmpty()) ? track : "Unknown Track";
            if (displayTrack.length() > 28) {
                displayTrack = displayTrack.substring(0, 26) + "...";
            }
            ImGui.textColored(1.0f, 1.0f, 1.0f, 1.0f, displayTrack);

            // Artist name (Gray)
            String displayArtist = (artist != null && !artist.isEmpty()) ? artist : "Unknown Artist";
            if (displayArtist.length() > 32) {
                displayArtist = displayArtist.substring(0, 30) + "...";
            }
            ImGui.textColored(0.68f, 0.70f, 0.75f, 0.85f, displayArtist);

            // 3. Mini Animated Waveform Equalizer
            if (showWaveform) {
                long time = System.currentTimeMillis();
                for (int i = 0; i < 14; i++) {
                    float barHeight = 3.0f;
                    if (playing) {
                        double phase = (time / 160.0) + (i * 0.50);
                        barHeight = (float) (3.0 + Math.abs(Math.sin(phase)) * 7.0);
                    }
                    if (i > 0) ImGui.sameLine(0, 2.0f);

                    float[] col = (i % 2 == 0)
                            ? new float[]{0.11f, 0.73f, 0.33f, 1.0f}
                            : new float[]{0.35f, 0.90f, 0.45f, 0.85f};

                    ImGui.pushStyleColor(ImGuiCol.Button, col[0], col[1], col[2], col[3]);
                    ImGui.button("##wave_" + i, 3.0f, barHeight);
                    ImGui.popStyleColor();
                }
            }

            ImGui.endGroup();
        }
        ImGui.end();

        ImGui.popStyleVar(3);
        ImGui.popStyleColor(2);
    }
}
