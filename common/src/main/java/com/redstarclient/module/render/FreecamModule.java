// ☭ Red Star Client — FreecamModule (Undetected Edition)
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import com.redstarclient.render.RenderUtils;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Undetected Noclip Freecam:
 * Completely detaches camera locally while pinning the player body securely
 * at the origin point with zero anomalous movement packets sent to the server.
 * Restores seamlessly with proper chunk culling and rotation sync.
 */
public class FreecamModule extends Module {

    private final ImFloat moveSpeed = new ImFloat(1.2f);
    private long enableTimestamp = 0L;

    // Detached Camera Position
    public double camX, camY, camZ;
    public double prevCamX, prevCamY, prevCamZ;
    public float  camYaw, camPitch;

    // Player original anchor
    private Vec3  savedPos;
    private float savedYaw, savedPitch;

    public FreecamModule() {
        super("Freecam", "Loest die Kamera ab fuer freien Flug (NoClip). Echter Spieler bleibt sicher stehen.", ModuleCategory.RENDER, GLFW.GLFW_KEY_N);
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            setEnabled(false);
            return;
        }

        enableTimestamp = System.currentTimeMillis();
        savedPos   = mc.player.position();
        savedYaw   = mc.player.getYRot();
        savedPitch = mc.player.getXRot();

        camX = savedPos.x;
        camY = mc.player.getEyeY();
        camZ = savedPos.z;

        prevCamX = camX;
        prevCamY = camY;
        prevCamZ = camZ;

        camYaw   = savedYaw;
        camPitch = savedPitch;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) {
            setEnabled(false);
            return;
        }

        // Direct key check in tick loop to guarantee escape / exit via N, M, or ESC
        if (mc.getWindow() != null && mc.screen == null) {
            long win = mc.getWindow().getWindow();
            if (win != 0 && (System.currentTimeMillis() - enableTimestamp > 250)) {
                boolean nDown   = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_N) == GLFW.GLFW_PRESS;
                boolean mDown   = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_M) == GLFW.GLFW_PRESS;
                boolean escDown = GLFW.glfwGetKey(win, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS;
                if (nDown || mDown || escDown) {
                    setEnabled(false);
                    return;
                }
            }
        }

        if (savedPos == null) {
            savedPos   = mc.player.position();
            savedYaw   = mc.player.getYRot();
            savedPitch = mc.player.getXRot();
            camX = savedPos.x;
            camY = mc.player.getEyeY();
            camZ = savedPos.z;
            prevCamX = camX;
            prevCamY = camY;
            prevCamZ = camZ;
        }

        // Keep real player anchored safely to prevent server fly/movement flags
        mc.player.setPos(savedPos.x, savedPos.y, savedPos.z);
        mc.player.setDeltaMovement(0, 0, 0);
        mc.player.fallDistance = 0;

        prevCamX = camX;
        prevCamY = camY;
        prevCamZ = camZ;

        camYaw   = mc.player.getYRot();
        camPitch = mc.player.getXRot();

        // Local noclip flight controls
        boolean forward  = mc.options.keyUp.isDown();
        boolean backward = mc.options.keyDown.isDown();
        boolean left     = mc.options.keyLeft.isDown();
        boolean right    = mc.options.keyRight.isDown();
        boolean up       = mc.options.keyJump.isDown();
        boolean down     = mc.options.keyShift.isDown();

        double speed = moveSpeed.get();
        double rad = Math.toRadians(camYaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double dx = 0;
        double dz = 0;

        if (forward) {
            dx -= sin * speed;
            dz += cos * speed;
        }
        if (backward) {
            dx += sin * speed;
            dz -= cos * speed;
        }
        // Correct strafe signs: Left (A) moves left, Right (D) moves right
        if (left) {
            dx += cos * speed;
            dz += sin * speed;
        }
        if (right) {
            dx -= cos * speed;
            dz -= sin * speed;
        }

        camX += dx;
        camZ += dz;

        if (up)   camY += speed;
        if (down) camY -= speed;
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.player != null) {
            if (savedPos != null) {
                mc.player.setPos(savedPos.x, savedPos.y, savedPos.z);
                mc.player.xo = savedPos.x;
                mc.player.yo = savedPos.y;
                mc.player.zo = savedPos.z;
                mc.player.xOld = savedPos.x;
                mc.player.yOld = savedPos.y;
                mc.player.zOld = savedPos.z;
                mc.player.setDeltaMovement(0, 0, 0);
                mc.player.setYRot(savedYaw);
                mc.player.setXRot(savedPitch);
                mc.player.yRotO = savedYaw;
                mc.player.xRotO = savedPitch;
            }
            mc.player.noPhysics = false;
            if (mc.player.input != null) {
                mc.player.input.forwardImpulse = 0f;
                mc.player.input.leftImpulse = 0f;
                mc.player.input.keyPresses = net.minecraft.world.entity.player.Input.EMPTY;
            }
        }
        savedPos = null;
        camX = 0;
        camY = 0;
        camZ = 0;
        prevCamX = 0;
        prevCamY = 0;
        prevCamZ = 0;

        if (mc != null && mc.levelRenderer != null) {
            mc.levelRenderer.allChanged();
        }
    }

    @Override
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null || mc.options.hideGui) return;
        int screenW = mc.getWindow().getGuiScaledWidth();
        String alertText = "★ FREECAM AKTIV ★ [Druecke 'N', 'M' oder 'ESC' zum Beenden]";
        int textW = mc.font.width(alertText);
        int x = (screenW - textW) / 2;
        int y = 14;
        RenderUtils.drawTextWithBackground(graphics, alertText, x, y, RenderUtils.SOVIET_GOLD, RenderUtils.BG_DARK_RED);
    }

    public double getInterpolatedX(float delta) {
        return prevCamX + (camX - prevCamX) * delta;
    }

    public double getInterpolatedY(float delta) {
        return prevCamY + (camY - prevCamY) * delta;
    }

    public double getInterpolatedZ(float delta) {
        return prevCamZ + (camZ - prevCamZ) * delta;
    }

    public float getMoveSpeed() {
        return moveSpeed.get();
    }

    public Vec3 getSavedPos() {
        return savedPos;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Flug-Geschwindigkeit##fc", moveSpeed.getData(), 0.1f, 5.0f, "%.1fx");
        ImGui.spacing();
        if (ImGui.button("★ FREECAM BEENDEN##fcBtn", 200f, 26f)) {
            setEnabled(false);
        }
        ImGui.spacing();
        ImGui.textDisabled("WASD: Horizontal fliegen (A = links, D = rechts)");
        ImGui.textDisabled("Leertaste / Umschalt: Hoch / Runter");
        ImGui.textDisabled("Hotkeys zum Beenden: N, M oder Escape");
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "● Stealth: Echter Koerper bleibt geschuetzt am Boden.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.enabled = false; // Niemals standardmaessig aktiv beim Start!
        cfg.putSetting("moveSpeed", moveSpeed.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        this.enabled = false; // Immer deaktiviert bei Config-Laden!
        moveSpeed.set(cfg.getSetting("moveSpeed", moveSpeed.get()));
    }
}
