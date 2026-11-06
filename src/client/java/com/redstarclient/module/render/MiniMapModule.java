// ☭ Red Star Client — MiniMapModule
package com.redstarclient.module.render;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.MiniMapRenderer;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

/**
 * Real-time mini map radar displaying surface blocks, player position,
 * viewing direction, and nearby entities.
 */
public class MiniMapModule extends Module {

    private final ImInt radius = new ImInt(32); // Block radius (diameter = 65)
    private final ImInt size = new ImInt(128); // Screen size in pixels
    private final ImBoolean showEntities = new ImBoolean(true);
    private final ImBoolean showCoords = new ImBoolean(true);

    private final MiniMapRenderer renderer = new MiniMapRenderer();

    public MiniMapModule() {
        super("MiniMap", "Radar minimap showing terrain, direction, and entities.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        int r = radius.get();
        BlockPos playerPos = mc.player.blockPosition();
        renderer.update(mc.level, playerPos, r);

        int mapSize = size.get();
        int margin = 10;
        int startX = graphics.guiWidth() - mapSize - margin;
        int startY = margin;

        // Border & background
        graphics.fill(startX - 2, startY - 2, startX + mapSize + 2, startY + mapSize + 2, RenderUtils.SOVIET_GOLD);
        graphics.fill(startX, startY, startX + mapSize, startY + mapSize, RenderUtils.BG_DARK_RED);

        // Draw terrain map pixels
        int diameter = r * 2 + 1;
        int[] colors = renderer.getMapColors();
        if (colors != null && colors.length == diameter * diameter) {
            float step = (float) mapSize / diameter;
            for (int z = 0; z < diameter; z += 2) { // 2-step sampling for crisp HUD performance
                for (int x = 0; x < diameter; x += 2) {
                    int c = colors[z * diameter + x];
                    int px = startX + (int)(x * step);
                    int py = startY + (int)(z * step);
                    int pw = Math.max(1, (int)(step * 2));
                    graphics.fill(px, py, px + pw, py + pw, c);
                }
            }
        }

        // Draw nearby entities as dots
        if (showEntities.get()) {
            for (Entity e : mc.level.entitiesForRendering()) {
                if (e == mc.player || !(e instanceof LivingEntity) || !e.isAlive()) continue;
                double dx = e.getX() - mc.player.getX();
                double dz = e.getZ() - mc.player.getZ();
                if (Math.abs(dx) <= r && Math.abs(dz) <= r) {
                    int ex = startX + (int) ((dx + r) / (r * 2f) * mapSize);
                    int ey = startY + (int) ((dz + r) / (r * 2f) * mapSize);

                    int dotColor = (e instanceof Player) ? RenderUtils.PLAYER_GOLD :
                                   (e instanceof Monster) ? RenderUtils.HOSTILE_RED :
                                   RenderUtils.PASSIVE_GREEN;

                    graphics.fill(ex - 1, ey - 1, ex + 2, ey + 2, dotColor);
                }
            }
        }

        // Draw player center marker (Red Star symbol / gold arrow)
        int cx = startX + mapSize / 2;
        int cy = startY + mapSize / 2;
        graphics.fill(cx - 2, cy - 2, cx + 3, cy + 3, RenderUtils.SOVIET_RED);
        graphics.fill(cx - 1, cy - 1, cx + 2, cy + 2, RenderUtils.SOVIET_GOLD);

        // Optional coordinates below minimap
        if (showCoords.get()) {
            String coordStr = String.format("%d, %d, %d", playerPos.getX(), playerPos.getY(), playerPos.getZ());
            int textW = mc.font.width(coordStr);
            RenderUtils.drawTextWithBackground(graphics, coordStr, startX + (mapSize - textW) / 2, startY + mapSize + 4,
                    RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Radius (blocks)##mm", radius.getData(), 16, 64);
        ImGui.sliderInt("Display Size (px)##mm", size.getData(), 80, 200);
        ImGui.checkbox("Show Entities##mm", showEntities);
        ImGui.checkbox("Show Coords##mm", showCoords);
    }
}
