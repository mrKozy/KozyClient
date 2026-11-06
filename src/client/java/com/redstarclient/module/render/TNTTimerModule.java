// â˜­ Red Star Client â€” TNTTimerModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.item.PrimedTnt;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Displays an animated floating countdown and HUD warning for ticking TNT entities.
 * Features 3D billboard text directly above the TNT in the world plus a HUD alert.
 */
public class TNTTimerModule extends Module {

    private final ImFloat   renderRange = new ImFloat(48.0f);
    private final ImBoolean showColor   = new ImBoolean(true);
    private final ImBoolean showHudAlert= new ImBoolean(true);

    public TNTTimerModule() {
        super("TNTTimer", "Shows animated timer above primed TNT & proximity danger alert.", ModuleCategory.RENDER, -1);
    }

    public boolean isShowColor() {
        return showColor.get();
    }

    public float getRenderRange() {
        return renderRange.get();
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        if (!showHudAlert.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || (mc.gui != null && mc.gui.hud.isHidden())) return;

        List<PrimedTnt> tntList = mc.level.getEntitiesOfClass(PrimedTnt.class,
                mc.player.getBoundingBox().inflate(16.0));
        if (tntList.isEmpty()) return;

        // Find closest primed TNT
        PrimedTnt closest = null;
        double minDst = Double.MAX_VALUE;
        for (PrimedTnt t : tntList) {
            double d = mc.player.distanceToSqr(t);
            if (d < minDst) {
                minDst = d;
                closest = t;
            }
        }

        if (closest != null) {
            float fuseSec = closest.getFuse() / 20.0f;
            int distM = (int) Math.sqrt(minDst);
            String alert = "â˜… DANGER: TNT in " + String.format("%.1fs", fuseSec) + " (" + distM + "m) â˜…";

            int alertColor = fuseSec < 1.2f ? 0xFFFF2222 : 0xFFFFAA00;
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int x = (screenWidth - mc.font.width(alert)) / 2;
            int y = 52;

            RenderUtils.drawTextWithBackground(graphics, alert, x, y, alertColor, 0xCC180202);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Sichtweite##tnt", renderRange.getData(), 8.0f, 64.0f, "%.0fm");
        ImGui.checkbox("Farbverlauf (Gruen -> Gelb -> Rot)##tnt", showColor);
        ImGui.checkbox("HUD-Gefahrenwarnung im Sichtfeld##tnt", showHudAlert);
        ImGui.spacing();
        ImGui.textDisabled("Zeigt 3D-Billboard Countdown direkt ueber scharfem TNT");
        ImGui.textDisabled("Perfekt fuer TNT-Jumps & BedWars Timing.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("renderRange", renderRange.get());
        cfg.putSetting("showColor", showColor.get());
        cfg.putSetting("showHudAlert", showHudAlert.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        renderRange.set(cfg.getSetting("renderRange", renderRange.get()));
        showColor.set(cfg.getSetting("showColor", showColor.get()));
        showHudAlert.set(cfg.getSetting("showHudAlert", showHudAlert.get()));
    }
}
