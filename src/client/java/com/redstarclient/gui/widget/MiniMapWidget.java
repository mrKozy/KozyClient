// ☭ Red Star Client — MiniMapWidget
package com.redstarclient.gui.widget;

import com.redstarclient.theme.CommunistTheme;
import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;

/**
 * ImGui-based mini map radar widget.
 */
public class MiniMapWidget {

    private static final int FLAGS =
            ImGuiWindowFlags.NoTitleBar   |
            ImGuiWindowFlags.NoResize     |
            ImGuiWindowFlags.NoScrollbar  |
            ImGuiWindowFlags.AlwaysAutoResize;

    public void render(int x, int y, int size, int[] colors, int diameter) {
        ImGui.setNextWindowPos(x, y);
        ImGui.setNextWindowSize(size + 16, size + 32);
        if (ImGui.begin("##minimap_widget", FLAGS)) {
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1f, "☭ RADAR");
            ImGui.separator();
            // Draw placeholder / radar bounding box
            ImGui.dummy(size, size);
        }
        ImGui.end();
    }
}
