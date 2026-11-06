// ☭ Red Star Client — CoordsFPSModule
package com.redstarclient.module.info;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.gui.hud.DraggableHudElement;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import org.lwjgl.glfw.GLFW;

/** Compact F3 replacement showing coordinates, direction, biome, and FPS. Supports dragging. */
public class CoordsFPSModule extends Module implements DraggableHudElement {

    private final ImInt posX = new ImInt(10);
    private final ImInt posY = new ImInt(30);

    private final ImBoolean showCoords  = new ImBoolean(true);
    private final ImBoolean showFacing  = new ImBoolean(true);
    private final ImBoolean showBiome   = new ImBoolean(true);
    private final ImBoolean showFPS     = new ImBoolean(true);

    private int lastWidth = 140;
    private int lastHeight = 60;

    public CoordsFPSModule() {
        super("Coords/FPS", "Compact coordinate and FPS display on HUD.", ModuleCategory.INFO, GLFW.GLFW_KEY_H);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        int x = posX.get();
        int y = posY.get();
        int color = RenderUtils.SOVIET_GOLD;
        int bg    = RenderUtils.BG_DARK_RED;

        BlockPos pos = mc.player.blockPosition();
        int maxW = 100;
        int curY = y;

        if (showCoords.get()) {
            boolean hide = com.redstarclient.module.render.StreamProofModule.shouldHideCoords();
            String coords = hide ? "XYZ: ***, ***, *** [STREAM MASK]" : ("XYZ: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
            maxW = Math.max(maxW, mc.font.width(coords) + 6);
            RenderUtils.drawTextWithBackground(graphics, coords, x, curY, color, bg);
            curY += 14;
        }

        if (showFacing.get()) {
            String facing = "Facing: " + mc.player.getDirection().getName();
            maxW = Math.max(maxW, mc.font.width(facing) + 6);
            RenderUtils.drawTextWithBackground(graphics, facing, x, curY, color, bg);
            curY += 14;
        }

        if (showBiome.get() && mc.level != null) {
            String biome = mc.level.getBiome(pos).unwrapKey().isPresent()
                    ? mc.level.getBiome(pos).unwrapKey().get().identifier().getPath()
                    : "unknown";
            String biomeText = "Biome: " + biome;
            maxW = Math.max(maxW, mc.font.width(biomeText) + 6);
            RenderUtils.drawTextWithBackground(graphics, biomeText, x, curY, color, bg);
            curY += 14;
        }

        if (showFPS.get()) {
            String fpsText = "FPS: " + mc.getFps();
            maxW = Math.max(maxW, mc.font.width(fpsText) + 6);
            RenderUtils.drawTextWithBackground(graphics, fpsText, x, curY, color, bg);
            curY += 14;
        }

        this.lastWidth = maxW;
        this.lastHeight = Math.max(curY - y, 16);
    }

    @Override
    public String getHudName() { return "Coords & FPS"; }

    @Override
    public int getHudX() { return posX.get(); }

    @Override
    public int getHudY() { return posY.get(); }

    @Override
    public int getHudWidth() { return lastWidth; }

    @Override
    public int getHudHeight() { return lastHeight; }

    @Override
    public void setHudPos(int x, int y) {
        posX.set(x);
        posY.set(y);
    }

    @Override
    public boolean isHudEnabled() { return isEnabled(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show Coords##cf",  showCoords);
        ImGui.checkbox("Show Facing##cf",  showFacing);
        ImGui.checkbox("Show Biome##cf",   showBiome);
        ImGui.checkbox("Show FPS##cf",     showFPS);
        ImGui.sliderInt("Position X##cf",  posX.getData(), 0, 1000);
        ImGui.sliderInt("Position Y##cf",  posY.getData(), 0, 800);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("posX", posX.get());
        cfg.putSetting("posY", posY.get());
        cfg.putSetting("showCoords", showCoords.get());
        cfg.putSetting("showFacing", showFacing.get());
        cfg.putSetting("showBiome", showBiome.get());
        cfg.putSetting("showFPS", showFPS.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        posX.set(cfg.getSetting("posX", posX.get()));
        posY.set(cfg.getSetting("posY", posY.get()));
        showCoords.set(cfg.getSetting("showCoords", showCoords.get()));
        showFacing.set(cfg.getSetting("showFacing", showFacing.get()));
        showBiome.set(cfg.getSetting("showBiome", showBiome.get()));
        showFPS.set(cfg.getSetting("showFPS", showFPS.get()));
    }
}
