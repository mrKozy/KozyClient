// Red Star Client — AsianHatModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Asian Hat ESP (China Hat):
 * Renders a conical bamboo / coolie hat hovering above the player's head,
 * dynamically tilting with the player's head pitch and rotation.
 */
public class AsianHatModule extends Module {

    private final ImBoolean onSelf         = new ImBoolean(true);
    private final ImBoolean onOthers       = new ImBoolean(true);
    private final ImInt     segments       = new ImInt(18);
    private final ImInt     colorMode      = new ImInt(0); // 0 = Soviet Red/Gold, 1 = Chroma Rainbow, 2 = Custom RGB
    private final ImBoolean drawMiddleRing = new ImBoolean(true);

    private final float[] customColor = new float[]{1.0f, 0.2f, 0.2f, 1.0f};

    public AsianHatModule() {
        super("AsianHat", "Rendert einen asiatischen Reishut (China Hat) über dem Spielerkopf (neigt sich mit dem Kopf).", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (onSelf.get()) {
            renderHatFor(mc.player, tickDelta);
        }

        if (onOthers.get()) {
            for (Player p : mc.level.players()) {
                if (p != mc.player && p.isAlive()) {
                    renderHatFor(p, tickDelta);
                }
            }
        }
    }

    private void renderHatFor(Player p, float tickDelta) {
        // Interpolated player head position
        double px = Mth.lerp(tickDelta, p.xOld, p.getX());
        double py = Mth.lerp(tickDelta, p.yOld, p.getY()) + p.getEyeHeight() + 0.10;
        double pz = Mth.lerp(tickDelta, p.zOld, p.getZ());

        float pitch = Mth.lerp(tickDelta, p.xRotO, p.getXRot());
        float yaw = Mth.lerp(tickDelta, p.yRotO, p.getYRot());

        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        // Calculate head orientation vectors
        // Forward: direction player is looking
        double fx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double fy = -Math.sin(pitchRad);
        double fz = Math.cos(yawRad) * Math.cos(pitchRad);
        Vec3 forward = new Vec3(fx, fy, fz).normalize();

        // Right vector: perpendicular to forward on horizontal plane
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad)).normalize();

        // Up vector: perpendicular to forward and right (tilted normal)
        Vec3 up = right.cross(forward).normalize();

        double hatRadius = 0.65;
        double hatHeight = 0.32;

        Vec3 baseCenter = new Vec3(px, py, pz);
        Vec3 apex = baseCenter.add(up.scale(hatHeight));

        int segs = Math.max(8, Math.min(32, segments.get()));
        Vec3[] basePoints = new Vec3[segs];
        Vec3[] midPoints = new Vec3[segs];

        // Generate circular points tilted with head normal
        Vec3 vAxis = up.cross(right).normalize(); // in-plane orthogonal vector

        for (int i = 0; i < segs; i++) {
            double angle = (2.0 * Math.PI * i) / segs;
            double cos = Math.cos(angle);
            double sin = Math.sin(angle);

            Vec3 offset = right.scale(cos * hatRadius).add(vAxis.scale(sin * hatRadius));
            basePoints[i] = baseCenter.add(offset);
            midPoints[i] = apex.add(basePoints[i]).scale(0.5);
        }

        // Determine line colors
        int ribColor = getRibColor();
        int ringColor = getRingColor();

        try {
            // 1. Draw ribs connecting apex to base perimeter
            for (int i = 0; i < segs; i++) {
                net.minecraft.gizmos.Gizmos.line(apex, basePoints[i], ribColor, 1.8f).setAlwaysOnTop();
            }

            // 2. Draw base perimeter ring
            for (int i = 0; i < segs; i++) {
                Vec3 p1 = basePoints[i];
                Vec3 p2 = basePoints[(i + 1) % segs];
                net.minecraft.gizmos.Gizmos.line(p1, p2, ringColor, 2.2f).setAlwaysOnTop();
            }

            // 3. Draw middle woven concentric ring
            if (drawMiddleRing.get()) {
                for (int i = 0; i < segs; i++) {
                    Vec3 m1 = midPoints[i];
                    Vec3 m2 = midPoints[(i + 1) % segs];
                    net.minecraft.gizmos.Gizmos.line(m1, m2, ringColor, 1.4f).setAlwaysOnTop();
                }
            }
        } catch (Throwable ignored) {}
    }

    private int getRibColor() {
        if (colorMode.get() == 1) {
            // Chroma Rainbow
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
            return (0xFF << 24) | (rgb & 0x00FFFFFF);
        } else if (colorMode.get() == 2) {
            // Custom RGB
            return RenderUtils.toARGB(customColor[0], customColor[1], customColor[2], customColor[3]);
        }
        // Soviet Red
        return RenderUtils.SOVIET_RED;
    }

    private int getRingColor() {
        if (colorMode.get() == 1) {
            float hue = ((System.currentTimeMillis() + 500L) % 4000L) / 4000.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
            return (0xFF << 24) | (rgb & 0x00FFFFFF);
        } else if (colorMode.get() == 2) {
            return RenderUtils.toARGB(customColor[0], customColor[1], customColor[2], customColor[3]);
        }
        // Worker Gold
        return RenderUtils.SOVIET_GOLD;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Auf eigenem Spieler (Self) anzeigen##ashat", onSelf);
        ImGui.checkbox("Auf anderen Spielern anzeigen##ashat", onOthers);
        ImGui.checkbox("Mittleren Reif (Geflecht-Ring) zeichnen##ashat", drawMiddleRing);
        ImGui.sliderInt("Rippen-Detail (Segmente)##ashat", segments.getData(), 8, 32);
        ImGui.separator();

        String[] modes = new String[]{"Soviet Rot/Gold", "Chroma Rainbow", "Eigenes RGB"};
        if (ImGui.combo("Farb-Modus##ashat", colorMode, modes)) {}

        if (colorMode.get() == 2) {
            ImGui.colorEdit4("Hut-Farbe##ashat", customColor);
        }
        ImGui.spacing();
        ImGui.textDisabled("Klassischer kegelförmiger China-Hat ESP mit dynamischer Kopfneigung.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("onSelf", onSelf.get());
        cfg.putSetting("onOthers", onOthers.get());
        cfg.putSetting("segments", segments.get());
        cfg.putSetting("colorMode", colorMode.get());
        cfg.putSetting("drawMiddleRing", drawMiddleRing.get());
        cfg.putSetting("colR", customColor[0]);
        cfg.putSetting("colG", customColor[1]);
        cfg.putSetting("colB", customColor[2]);
        cfg.putSetting("colA", customColor[3]);
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        onSelf.set(cfg.getSetting("onSelf", onSelf.get()));
        onOthers.set(cfg.getSetting("onOthers", onOthers.get()));
        segments.set(cfg.getSetting("segments", segments.get()));
        colorMode.set(cfg.getSetting("colorMode", colorMode.get()));
        drawMiddleRing.set(cfg.getSetting("drawMiddleRing", drawMiddleRing.get()));
        customColor[0] = cfg.getSetting("colR", customColor[0]);
        customColor[1] = cfg.getSetting("colG", customColor[1]);
        customColor[2] = cfg.getSetting("colB", customColor[2]);
        customColor[3] = cfg.getSetting("colA", customColor[3]);
    }
}
