// ☭ Red Star Client — BreadcrumbsModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;

/** Renders a glowing colored breadcrumb trail behind the player. */
public class BreadcrumbsModule extends Module {

    private final ImInt maxPoints = new ImInt(100);
    private final LinkedList<Vec3> trail = new LinkedList<>();

    public BreadcrumbsModule() {
        super("Breadcrumbs", "Draws a movement trail line behind the player.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        Vec3 pos = mc.player.position();
        if (trail.isEmpty() || trail.getLast().distanceTo(pos) > 0.5) {
            trail.add(pos);
            while (trail.size() > maxPoints.get()) {
                trail.removeFirst();
            }
        }
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        if (trail.size() < 2) return;
        Vec3 prev = null;
        for (Vec3 p : trail) {
            if (prev != null) {
                RenderUtils.drawLine3D(poseStack, camera, prev, p, RenderUtils.SOVIET_GOLD);
            }
            prev = p;
        }
    }

    @Override
    public void onDisable() {
        trail.clear();
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Max Points##bc", maxPoints.getData(), 20, 500);
        if (ImGui.button("Clear Trail##bc")) {
            trail.clear();
        }
    }
}
