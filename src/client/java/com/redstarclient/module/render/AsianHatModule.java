// Red Star Client — AsianHatModule (Player Head Cosmetics ESP)
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
 * Head Cosmetics ESP (Asian Hat, Angel Halo, Devil Horns, Bunny Ears, Witch Hat):
 * Renders custom 3D cosmetic headwear hovering above player heads, dynamically
 * tilting with head pitch, yaw and player movement.
 */
public class AsianHatModule extends Module {

    private final ImBoolean onSelf         = new ImBoolean(true);
    private final ImBoolean onOthers       = new ImBoolean(true);
    private final ImInt     cosmeticStyle  = new ImInt(0); // 0 = Asian Hat, 1 = Angel Halo, 2 = Devil Horns, 3 = Bunny Ears, 4 = Witch Hat
    private final ImInt     segments       = new ImInt(18);
    private final ImInt     colorMode      = new ImInt(0); // 0 = Soviet Red/Gold, 1 = Chroma Rainbow, 2 = Custom RGB
    private final ImBoolean drawMiddleRing = new ImBoolean(true);

    private final float[] customColor = new float[]{1.0f, 0.2f, 0.2f, 1.0f};

    public AsianHatModule() {
        super("AsianHat", "Rendert 3D-Kopf-Kosmetika (China Hat, Heiligenschein, Hörner, Hasenohren) über Spielerköpfen.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (onSelf.get()) {
            renderCosmetics(mc.player, tickDelta);
        }

        if (onOthers.get()) {
            for (Player p : mc.level.players()) {
                if (p != mc.player && p.isAlive()) {
                    renderCosmetics(p, tickDelta);
                }
            }
        }
    }

    private void renderCosmetics(Player p, float tickDelta) {
        double px = Mth.lerp(tickDelta, p.xOld, p.getX());
        double py = Mth.lerp(tickDelta, p.yOld, p.getY()) + p.getEyeHeight() + 0.10;
        double pz = Mth.lerp(tickDelta, p.zOld, p.getZ());

        float pitch = Mth.lerp(tickDelta, p.xRotO, p.getXRot());
        float yaw = Mth.lerp(tickDelta, p.yRotO, p.getYRot());

        float pitchRad = (float) Math.toRadians(pitch);
        float yawRad = (float) Math.toRadians(yaw);

        // Direction vectors
        double fx = -Math.sin(yawRad) * Math.cos(pitchRad);
        double fy = -Math.sin(pitchRad);
        double fz = Math.cos(yawRad) * Math.cos(pitchRad);
        Vec3 forward = new Vec3(fx, fy, fz).normalize();
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad)).normalize();
        Vec3 up = right.cross(forward).normalize();
        Vec3 vAxis = up.cross(right).normalize();

        Vec3 headCenter = new Vec3(px, py, pz);
        int ribColor = getRibColor();
        int ringColor = getRingColor();

        int style = cosmeticStyle.get();
        try {
            switch (style) {
                case 0 -> renderAsianHat(headCenter, right, vAxis, up, ribColor, ringColor);
                case 1 -> renderAngelHalo(headCenter, right, vAxis, up, ringColor);
                case 2 -> renderDevilHorns(headCenter, right, forward, up, ribColor);
                case 3 -> renderBunnyEars(headCenter, right, up, ribColor, ringColor);
                case 4 -> renderWitchHat(headCenter, right, vAxis, up, ribColor, ringColor);
            }
        } catch (Throwable ignored) {}
    }

    private void renderAsianHat(Vec3 baseCenter, Vec3 right, Vec3 vAxis, Vec3 up, int ribColor, int ringColor) {
        double hatRadius = 0.65;
        double hatHeight = 0.32;
        Vec3 apex = baseCenter.add(up.scale(hatHeight));

        int segs = Math.max(8, Math.min(32, segments.get()));
        Vec3[] basePoints = new Vec3[segs];
        Vec3[] midPoints = new Vec3[segs];

        for (int i = 0; i < segs; i++) {
            double angle = (2.0 * Math.PI * i) / segs;
            Vec3 offset = right.scale(Math.cos(angle) * hatRadius).add(vAxis.scale(Math.sin(angle) * hatRadius));
            basePoints[i] = baseCenter.add(offset);
            midPoints[i] = apex.add(basePoints[i]).scale(0.5);
        }

        for (int i = 0; i < segs; i++) {
            net.minecraft.gizmos.Gizmos.line(apex, basePoints[i], ribColor, 1.8f).setAlwaysOnTop();
            net.minecraft.gizmos.Gizmos.line(basePoints[i], basePoints[(i + 1) % segs], ringColor, 2.2f).setAlwaysOnTop();
            if (drawMiddleRing.get()) {
                net.minecraft.gizmos.Gizmos.line(midPoints[i], midPoints[(i + 1) % segs], ringColor, 1.4f).setAlwaysOnTop();
            }
        }
    }

    private void renderAngelHalo(Vec3 headCenter, Vec3 right, Vec3 vAxis, Vec3 up, int ringColor) {
        Vec3 haloCenter = headCenter.add(up.scale(0.35));
        double haloRadius = 0.45;
        int segs = 20;
        Vec3[] ringPoints = new Vec3[segs];

        for (int i = 0; i < segs; i++) {
            double angle = (2.0 * Math.PI * i) / segs;
            Vec3 offset = right.scale(Math.cos(angle) * haloRadius).add(vAxis.scale(Math.sin(angle) * haloRadius));
            ringPoints[i] = haloCenter.add(offset);
        }

        for (int i = 0; i < segs; i++) {
            net.minecraft.gizmos.Gizmos.line(ringPoints[i], ringPoints[(i + 1) % segs], ringColor, 3.0f).setAlwaysOnTop();
        }
    }

    private void renderDevilHorns(Vec3 headCenter, Vec3 right, Vec3 forward, Vec3 up, int color) {
        // Left Horn
        Vec3 leftBase = headCenter.add(right.scale(-0.25)).add(forward.scale(0.1));
        Vec3 leftTip = leftBase.add(up.scale(0.35)).add(right.scale(-0.20)).add(forward.scale(0.12));
        net.minecraft.gizmos.Gizmos.line(leftBase, leftTip, color, 3.2f).setAlwaysOnTop();

        // Right Horn
        Vec3 rightBase = headCenter.add(right.scale(0.25)).add(forward.scale(0.1));
        Vec3 rightTip = rightBase.add(up.scale(0.35)).add(right.scale(0.20)).add(forward.scale(0.12));
        net.minecraft.gizmos.Gizmos.line(rightBase, rightTip, color, 3.2f).setAlwaysOnTop();
    }

    private void renderBunnyEars(Vec3 headCenter, Vec3 right, Vec3 up, int earColor, int innerColor) {
        // Left Ear
        Vec3 lBase = headCenter.add(right.scale(-0.20));
        Vec3 lTip = lBase.add(up.scale(0.60)).add(right.scale(-0.08));
        net.minecraft.gizmos.Gizmos.line(lBase, lTip, earColor, 3.5f).setAlwaysOnTop();
        net.minecraft.gizmos.Gizmos.line(lBase.add(up.scale(0.05)), lTip.add(up.scale(-0.05)), innerColor, 1.8f).setAlwaysOnTop();

        // Right Ear
        Vec3 rBase = headCenter.add(right.scale(0.20));
        Vec3 rTip = rBase.add(up.scale(0.60)).add(right.scale(0.08));
        net.minecraft.gizmos.Gizmos.line(rBase, rTip, earColor, 3.5f).setAlwaysOnTop();
        net.minecraft.gizmos.Gizmos.line(rBase.add(up.scale(0.05)), rTip.add(up.scale(-0.05)), innerColor, 1.8f).setAlwaysOnTop();
    }

    private void renderWitchHat(Vec3 baseCenter, Vec3 right, Vec3 vAxis, Vec3 up, int ribColor, int ringColor) {
        double brimRadius = 0.75;
        double tipHeight = 0.65;
        Vec3 apex = baseCenter.add(up.scale(tipHeight)).add(right.scale(-0.15)); // stylish curved tip

        int segs = 16;
        Vec3[] brimPoints = new Vec3[segs];
        for (int i = 0; i < segs; i++) {
            double angle = (2.0 * Math.PI * i) / segs;
            Vec3 offset = right.scale(Math.cos(angle) * brimRadius).add(vAxis.scale(Math.sin(angle) * brimRadius));
            brimPoints[i] = baseCenter.add(offset);
        }

        for (int i = 0; i < segs; i++) {
            net.minecraft.gizmos.Gizmos.line(apex, brimPoints[i], ribColor, 2.0f).setAlwaysOnTop();
            net.minecraft.gizmos.Gizmos.line(brimPoints[i], brimPoints[(i + 1) % segs], ringColor, 2.5f).setAlwaysOnTop();
        }
    }

    private int getRibColor() {
        if (colorMode.get() == 1) {
            float hue = (System.currentTimeMillis() % 4000L) / 4000.0f;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
            return (0xFF << 24) | (rgb & 0x00FFFFFF);
        } else if (colorMode.get() == 2) {
            return RenderUtils.toARGB(customColor[0], customColor[1], customColor[2], customColor[3]);
        }
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
        return RenderUtils.SOVIET_GOLD;
    }

    @Override
    public void renderImGuiConfig() {
        String[] styles = new String[]{"Asian Hat (China Hat)", "Angel Halo (Heiligenschein)", "Devil Horns (Hörner)", "Bunny Ears (Hasenohren)", "Witch Hat (Hexenhut)"};
        ImGui.combo("Kosmetik-Stil##ashat", cosmeticStyle, styles);
        ImGui.separator();

        ImGui.checkbox("Auf eigenem Spieler (Self) anzeigen##ashat", onSelf);
        ImGui.checkbox("Auf anderen Spielern anzeigen##ashat", onOthers);
        if (cosmeticStyle.get() == 0) {
            ImGui.checkbox("Mittleren Reif (Geflecht-Ring) zeichnen##ashat", drawMiddleRing);
            ImGui.sliderInt("Rippen-Detail (Segmente)##ashat", segments.getData(), 8, 32);
        }
        ImGui.separator();

        String[] modes = new String[]{"Soviet Rot/Gold", "Chroma Rainbow", "Eigenes RGB"};
        ImGui.combo("Farb-Modus##ashat", colorMode, modes);

        if (colorMode.get() == 2) {
            ImGui.colorEdit4("Farbe##ashat", customColor);
        }
        ImGui.spacing();
        ImGui.textDisabled("3D-Kopf-Kosmetika mit flüssiger Neigung bei Kopfbewegungen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("onSelf", onSelf.get());
        cfg.putSetting("onOthers", onOthers.get());
        cfg.putSetting("cosmeticStyle", cosmeticStyle.get());
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
        cosmeticStyle.set(cfg.getSetting("cosmeticStyle", cosmeticStyle.get()));
        segments.set(cfg.getSetting("segments", segments.get()));
        colorMode.set(cfg.getSetting("colorMode", colorMode.get()));
        drawMiddleRing.set(cfg.getSetting("drawMiddleRing", drawMiddleRing.get()));
        customColor[0] = cfg.getSetting("colR", customColor[0]);
        customColor[1] = cfg.getSetting("colG", customColor[1]);
        customColor[2] = cfg.getSetting("colB", customColor[2]);
        customColor[3] = cfg.getSetting("colA", customColor[3]);
    }
}
