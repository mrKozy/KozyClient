// ☭ Red Star Client — TracerModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.ESPRenderer;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3fc;

/** Draws lines from the crosshair to nearby entities in world space. */
public class TracerModule extends Module {

    private static final String[] ORIGINS = { "Crosshair (Center)", "Body / Feet" };

    private final ImFloat   range      = new ImFloat(128f);
    private final ImBoolean players    = new ImBoolean(true);
    private final ImBoolean mobs       = new ImBoolean(false);
    private final ImInt     originMode = new ImInt(0); // 0 = Crosshair, 1 = Body

    public TracerModule() {
        super("Tracer", "Draws lines from crosshair to nearby entities.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || camera == null || !isEnabled()) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        Vec3 from;
        if (originMode.get() == 0) {
            // Originates directly from the screen center / crosshair:
            // Moving slightly along the forwardVector places the start point safely in front
            // of the camera near clipping plane, visible in 1st person and 3rd person!
            Vector3fc fwd = camera.forwardVector();
            from = camera.position().add(fwd.x() * 0.35, fwd.y() * 0.35, fwd.z() * 0.35);
        } else {
            from = mc.player.getEyePosition(tickDelta);
        }

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player || !e.isAlive()) continue;
            if (e instanceof Player && !players.get()) continue;
            if ((e instanceof Monster || e instanceof Animal) && !mobs.get()) continue;
            if (mc.player.distanceTo(e) > range.get()) continue;

            // Draw towards the entity center
            Vec3 to = e.getBoundingBox().getCenter();
            int color = ESPRenderer.getEntityColor(e);
            RenderUtils.drawLine3D(poseStack, camera, from, to, color);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Origin##trc", originMode, ORIGINS);
        ImGui.sliderFloat("Range##trc", range.getData(), 16f, 256f, "%.0f blocks");
        ImGui.checkbox("Players##trc", players);
        ImGui.checkbox("Mobs##trc", mobs);
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Crosshair mode renders in both 1st and 3rd person.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("players", players.get());
        cfg.putSetting("mobs", mobs.get());
        cfg.putSetting("originMode", originMode.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        players.set(cfg.getSetting("players", players.get()));
        mobs.set(cfg.getSetting("mobs", mobs.get()));
        originMode.set(cfg.getSetting("originMode", originMode.get()));
    }
}
