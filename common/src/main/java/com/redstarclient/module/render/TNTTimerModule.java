// ☭ Red Star Client — TNTTimerModule
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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
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
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        List<PrimedTnt> tntList = mc.level.getEntitiesOfClass(PrimedTnt.class,
                mc.player.getBoundingBox().inflate(renderRange.get()));
        if (tntList.isEmpty()) return;

        Font font = mc.font;
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        for (PrimedTnt tnt : tntList) {
            float fuseSec = tnt.getFuse() / 20.0f;
            String text = String.format("%.2fs", Math.max(0.0f, fuseSec));

            int color;
            if (!showColor.get()) {
                color = RenderUtils.SOVIET_GOLD;
            } else if (fuseSec > 2.5f) {
                color = 0xFF55FF55; // Safe green
            } else if (fuseSec > 1.0f) {
                color = 0xFFFFAA00; // Warning gold
            } else {
                color = 0xFFFF2222; // Critical red
            }

            double dx = tnt.getX() - camera.getPosition().x;
            double dy = (tnt.getY() + 1.25) - camera.getPosition().y;
            double dz = tnt.getZ() - camera.getPosition().z;

            poseStack.pushPose();
            poseStack.translate(dx, dy, dz);

            // Billboard towards player camera
            poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
            poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

            float scale = 0.028f;
            poseStack.scale(-scale, -scale, scale);

            Matrix4f matrix = poseStack.last().pose();
            float textWidth = font.width(text);
            float halfWidth = textWidth / 2.0f;

            // Render black drop-shadow / background plate
            font.drawInBatch(text, -halfWidth, 0, color, false, matrix, bufferSource,
                    Font.DisplayMode.SEE_THROUGH, 0x90000000, 0xF000F0);

            poseStack.popPose();
        }
        bufferSource.endBatch();
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        if (!showHudAlert.get()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.options.hideGui) return;

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
            String alert = "★ DANGER: TNT in " + String.format("%.1fs", fuseSec) + " (" + distM + "m) ★";

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
