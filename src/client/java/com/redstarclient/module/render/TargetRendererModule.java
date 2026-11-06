// ☭ Red Star Client — TargetRendererModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;

/**
 * Target Renderer:
 * Highlights your current combat target with a glowing 3D box and Soviet star marker.
 */
public class TargetRendererModule extends Module {

    private final ImBoolean showBox3D = new ImBoolean(true);

    public TargetRendererModule() {
        super("TargetRenderer", "Markiert dein aktuelles Kampf-Ziel mit leuchtendem 3D-Kasten.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Entity target = PlayerUtils.getClosestEntity(mc.player, 12.0, false);
        if (target != null && showBox3D.get()) {
            AABB bb = target.getBoundingBox();
            RenderUtils.drawBox3D(poseStack, camera, bb, RenderUtils.SOVIET_RED);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("3D Ziel-Box zeichnen##tr", showBox3D);
        ImGui.spacing();
        ImGui.textDisabled("Hebt das Primaerziel im Gefecht deutlich hervor.");
    }
}
