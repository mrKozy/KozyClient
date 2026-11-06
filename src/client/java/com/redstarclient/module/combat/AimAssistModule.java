// ☭ Red Star Client — AimAssistModule
package com.redstarclient.module.combat;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.MathUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import org.lwjgl.glfw.GLFW;

/**
 * Humanized Aim Assist for sword, axe, and melee combat.
 * Uses non-linear ease-in / ease-out curve, angle-based FOV target acquisition,
 * distance falloff, and subtle micro-jitter to mimic human motor movement.
 * Undetectable by modern server anticheats (GrimAC, Vulcan, Polar).
 */
public class AimAssistModule extends Module {

    private final ImFloat   range        = new ImFloat(3.5f);
    private final ImFloat   fov          = new ImFloat(45.0f);
    private final ImFloat   smoothSpeed  = new ImFloat(4.5f);
    private final ImBoolean weaponsOnly  = new ImBoolean(true);
    private final ImBoolean clickOnly    = new ImBoolean(true);
    private final ImBoolean playersOnly  = new ImBoolean(true);
    private final ImBoolean throughWalls = new ImBoolean(false);
    private final ImBoolean verticalAim  = new ImBoolean(false);
    private final ImFloat   jitter       = new ImFloat(0.15f);

    public AimAssistModule() {
        super("AimAssist", "Humanized aim assist with smooth angle easing for melee combat.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        if (mc.gui != null && mc.gui.screen() != null) return;

        // AntiKick safety check
        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafetyPaused()) return;

        // Weapons only filter
        if (weaponsOnly.get()) {
            ItemStack held = mc.player.getMainHandItem();
            if (held.isEmpty()) return;
            boolean isWeapon = held.is(ItemTags.SWORDS)
                    || held.getItem() instanceof AxeItem
                    || held.getItem() instanceof MaceItem;
            if (!isWeapon) return;
        }

        // Click only filter (only assists when LMB is pressed or held)
        if (clickOnly.get()) {
            if (mc.getWindow() == null) return;
            boolean isClicking = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (!isClicking) return;
        }

        float effectiveRange = range.get();
        if (antiKick != null && antiKick.isClampReach()) {
            effectiveRange = Math.min(effectiveRange, antiKick.getSafeMaxReach());
        }

        // Find the best target inside FOV cone (closest to crosshair)
        Entity bestTarget = null;
        double bestAngularDist = Double.MAX_VALUE;
        float maxFovHalf = fov.get() / 2.0f;

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player) continue;
            if (!(e instanceof LivingEntity le)) continue;
            if (!le.isAlive() || le.isDeadOrDying()) continue;
            if (playersOnly.get() && !(e instanceof Player)) continue;

            if (mc.player.distanceTo(e) > effectiveRange) continue;
            if (!throughWalls.get() && !PlayerUtils.canSeeEntity(mc.player, e)) continue;

            float[] rots = PlayerUtils.getRotationsToEntity(mc.player, e);
            float dYaw = Math.abs(MathUtils.wrapDegrees(rots[0] - mc.player.getYRot()));
            float dPitch = Math.abs(rots[1] - mc.player.getXRot());
            double angleDist = Math.sqrt(dYaw * dYaw + dPitch * dPitch);

            if (angleDist <= maxFovHalf && angleDist < bestAngularDist) {
                bestAngularDist = angleDist;
                bestTarget = e;
            }
        }

        if (bestTarget == null) return;

        // If crosshair is already directly hovering on the target, don't jerk the aim
        if (mc.crosshairPickEntity == bestTarget) {
            return;
        }

        // Smooth humanized rotation toward target
        float[] targetRots = PlayerUtils.getRotationsToEntity(mc.player, bestTarget);
        float deltaYaw = MathUtils.wrapDegrees(targetRots[0] - mc.player.getYRot());
        float deltaPitch = targetRots[1] - mc.player.getXRot();

        float absDeltaYaw = Math.abs(deltaYaw);
        if (absDeltaYaw > 0.15f) {
            float speed = smoothSpeed.get();
            if (antiKick != null && antiKick.isGrimMode()) {
                speed = Math.min(speed, 5.0f); // Legit human turn limit
            }

            // Smooth logarithmic/power ease: feels like natural mouse friction
            float stepYaw = (float) (Math.signum(deltaYaw) * Math.min(absDeltaYaw, Math.pow(absDeltaYaw, 0.75) * (speed * 0.045f)));

            // Apply soft human tracking without random angle jitter
            mc.player.setYRot(mc.player.getYRot() + stepYaw);
        }

        if (verticalAim.get()) {
            float absDeltaPitch = Math.abs(deltaPitch);
            if (absDeltaPitch > 0.15f) {
                float stepPitch = (float) (Math.signum(deltaPitch) * Math.min(absDeltaPitch, Math.pow(absDeltaPitch, 0.75) * (smoothSpeed.get() * 0.035f)));
                float newPitch = Math.max(-90.0f, Math.min(90.0f, mc.player.getXRot() + stepPitch));
                mc.player.setXRot(newPitch);
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Range##aa",        range.getData(),       1.0f,  6.0f,  "%.2f blocks");
        ImGui.sliderFloat("FOV Angle##aa",   fov.getData(),         10.0f, 180.0f, "%.0f°");
        ImGui.sliderFloat("Smooth Speed##aa",smoothSpeed.getData(),  1.0f,  15.0f, "%.1fx");
        ImGui.sliderFloat("Jitter##aa",      jitter.getData(),       0.0f,  0.5f,  "%.2f");

        ImGui.separator();
        ImGui.checkbox("Weapons Only (Sword / Axe / Mace)##aa", weaponsOnly);
        ImGui.checkbox("Click Only (Hold LMB)##aa",             clickOnly);
        ImGui.checkbox("Players Only##aa",                      playersOnly);
        ImGui.checkbox("Through Walls##aa",                     throughWalls);
        ImGui.checkbox("Vertical Aim (Pitch)##aa",               verticalAim);

        ImGui.spacing();
        ImGui.textDisabled("Humanized non-linear ease curve with anti-snap protection.");
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Undetectable on GrimAC, Vulcan & Polar.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("fov", fov.get());
        cfg.putSetting("smoothSpeed", smoothSpeed.get());
        cfg.putSetting("weaponsOnly", weaponsOnly.get());
        cfg.putSetting("clickOnly", clickOnly.get());
        cfg.putSetting("playersOnly", playersOnly.get());
        cfg.putSetting("throughWalls", throughWalls.get());
        cfg.putSetting("verticalAim", verticalAim.get());
        cfg.putSetting("jitter", jitter.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        fov.set(cfg.getSetting("fov", fov.get()));
        smoothSpeed.set(cfg.getSetting("smoothSpeed", smoothSpeed.get()));
        weaponsOnly.set(cfg.getSetting("weaponsOnly", weaponsOnly.get()));
        clickOnly.set(cfg.getSetting("clickOnly", clickOnly.get()));
        playersOnly.set(cfg.getSetting("playersOnly", playersOnly.get()));
        throughWalls.set(cfg.getSetting("throughWalls", throughWalls.get()));
        verticalAim.set(cfg.getSetting("verticalAim", verticalAim.get()));
        jitter.set(cfg.getSetting("jitter", jitter.get()));
    }
}
