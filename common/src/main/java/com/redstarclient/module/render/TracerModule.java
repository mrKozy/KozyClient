// ☭ Red Star Client — TracerModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.ESPRenderer;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Draws lines from the player's center to nearby entities in world space. */
public class TracerModule extends Module {

    private final ImFloat   range   = new ImFloat(64f);
    private final ImBoolean players = new ImBoolean(true);
    private final ImBoolean mobs    = new ImBoolean(false);

    public TracerModule() {
        super("Tracer", "Draws lines from crosshair to nearby entities.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        Vec3 from = mc.player.getEyePosition(tickDelta);

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !e.isAlive()) continue;
            if (e instanceof Player  && !players.get()) continue;
            if ((e instanceof Monster || e instanceof Animal) && !mobs.get()) continue;
            if (mc.player.distanceTo(e) > range.get()) continue;

            Vec3 to    = e.getPosition(tickDelta);
            int  color = ESPRenderer.getEntityColor(e);
            RenderUtils.drawLine3D(poseStack, camera, from, to, color);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Range##trc",  range.getData(), 16f, 256f);
        ImGui.checkbox("Players##trc",   players);
        ImGui.checkbox("Mobs##trc",      mobs);
    }
}
