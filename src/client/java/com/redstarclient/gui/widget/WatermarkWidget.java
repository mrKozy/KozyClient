// ☭ Red Star Client — WatermarkWidget
package com.redstarclient.gui.widget;

import com.redstarclient.RedStarClient;
import com.redstarclient.render.TextureLoader;
import com.redstarclient.theme.CommunistTheme;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.Minecraft;

/**
 * Modern glassmorphic rounded pill watermark featuring the official Red Star Client emblem.
 */
public class WatermarkWidget {

    private static final int FLAGS =
            ImGuiWindowFlags.NoTitleBar   |
            ImGuiWindowFlags.NoResize     |
            ImGuiWindowFlags.NoScrollbar  |
            ImGuiWindowFlags.AlwaysAutoResize;

    public void render() {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideWatermark()) {
            return;
        }

        ImGui.setNextWindowPos(14.0f, 14.0f);

        // Glassmorphic pill styling
        ImGui.pushStyleColor(ImGuiCol.WindowBg, 0.05f, 0.03f, 0.04f, 0.82f);
        ImGui.pushStyleColor(ImGuiCol.Border, CommunistTheme.SOVIET_RED[0], CommunistTheme.SOVIET_RED[1], CommunistTheme.SOVIET_RED[2], 0.55f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, 8.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 8.0f, 6.0f);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 1.0f);

        if (ImGui.begin("##watermark_pill", FLAGS)) {
            // Render transparent high-res logo emblem
            int logoId = TextureLoader.getLogoTextureId();
            if (logoId > 0) {
                ImGui.image(logoId, 24.0f, 24.0f);
                ImGui.sameLine();
            }

            ImGui.alignTextToFramePadding();
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 1.0f,
                    "RED STAR");

            ImGui.sameLine();
            ImGui.textDisabled("|");

            ImGui.sameLine();
            ImGui.textColored(CommunistTheme.TEXT_PRIMARY[0], CommunistTheme.TEXT_PRIMARY[1], CommunistTheme.TEXT_PRIMARY[2], 0.9f,
                    "v" + RedStarClient.VERSION);

            // FPS indicator
            Minecraft mc = Minecraft.getInstance();
            if (mc != null) {
                ImGui.sameLine();
                ImGui.textDisabled("|");
                ImGui.sameLine();
                ImGui.textColored(0.35f, 0.90f, 0.45f, 0.95f, mc.getFps() + " FPS");
            }

            ImGui.sameLine();
            ImGui.textDisabled("|");
            ImGui.sameLine();
            ImGui.textColored(CommunistTheme.SOVIET_GOLD[0], CommunistTheme.SOVIET_GOLD[1], CommunistTheme.SOVIET_GOLD[2], 0.85f,
                    "[INSERT]");
        }
        ImGui.end();

        ImGui.popStyleVar(3);
        ImGui.popStyleColor(2);
    }
}
