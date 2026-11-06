// ☭ Red Star Client — KillAuraModule
package com.redstarclient.module.combat;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import com.redstarclient.util.TimerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;

/**
 * Intelligent Combat KillAura:
 * - 1.9+ Attack Cooldown (Max Damage & Sweep) or 1.8 CPS Spam
 * - Silent Auto-Aim / Rotation tracking
 * - Automatic Criticals & AutoSword integration
 * - AntiKick & GrimAC range/angle clamp compatibility
 */
public class KillAuraModule extends Module {

    private static final String[] ATTACK_MODES = { "Cooldown (1.9+ Max Damage)", "Spam CPS (1.8 PvP)" };

    private final ImInt     attackMode   = new ImInt(0);
    private final ImFloat   range        = new ImFloat(3.1f);
    private final ImInt     maxCPS       = new ImInt(12);
    private final ImBoolean autoRotate   = new ImBoolean(true);
    private final ImBoolean playersOnly  = new ImBoolean(false);
    private final ImBoolean throughWalls = new ImBoolean(false);

    private final TimerUtils cpsTimer = new TimerUtils();
    private LivingEntity currentTarget = null;

    public KillAuraModule() {
        super("KillAura", "Greift Gegner in Reichweite vollautomatisch mit maximalem Schaden an.", ModuleCategory.COMBAT, GLFW.GLFW_KEY_R);
    }

    public LivingEntity getCurrentTarget() {
        return currentTarget;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null || !isEnabled()) {
            currentTarget = null;
            return;
        }

        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafetyPaused()) {
            currentTarget = null;
            return;
        }

        float effectiveRange = range.get();
        if (antiKick != null && antiKick.isClampReach()) {
            effectiveRange = Math.min(effectiveRange, antiKick.getSafeMaxReach());
        }

        boolean canAttackThroughWalls = throughWalls.get();
        if (antiKick != null && antiKick.isGrimMode()) {
            canAttackThroughWalls = false; // Never attack through walls on GrimAC
        }

        Entity rawTarget = PlayerUtils.getClosestEntity(mc.player, effectiveRange, playersOnly.get());
        if (!(rawTarget instanceof LivingEntity le) || le.isDeadOrDying()) {
            currentTarget = null;
            return;
        }

        if (!canAttackThroughWalls && !PlayerUtils.canSeeEntity(mc.player, le)) {
            currentTarget = null;
            return;
        }

        currentTarget = le;

        // Auto-Aim / Look at target
        if (autoRotate.get()) {
            Vec3 targetEye = le.getEyePosition();
            Vec3 playerEye = mc.player.getEyePosition();
            Vec3 diff = targetEye.subtract(playerEye);
            double diffXZ = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
            float targetYaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
            float targetPitch = (float) -Math.toDegrees(Math.atan2(diff.y, diffXZ));

            // Smoothly rotate
            mc.player.setYRot(targetYaw);
            mc.player.setXRot(targetPitch);
        }

        boolean shouldAttack = false;

        if (attackMode.get() == 0) {
            // 1.9+ Cooldown mode: only attack when weapon cooldown bar is fully charged (>= 95%)
            if (mc.player.getAttackStrengthScale(0.5f) >= 0.95f) {
                shouldAttack = true;
            }
        } else {
            // 1.8 CPS mode
            int effectiveCPS = maxCPS.get();
            if (antiKick != null && antiKick.isGrimMode()) {
                effectiveCPS = Math.min(effectiveCPS, 14);
            }
            if (cpsTimer.canAct(effectiveCPS)) {
                shouldAttack = true;
            }
        }

        if (shouldAttack) {
            // 1. Auto-switch to best weapon
            AutoSwordModule autoSword = RedStarClient.MODULES.get(AutoSwordModule.class);
            if (autoSword != null && autoSword.isEnabled()) {
                autoSword.onAttack();
            }

            // 2. Trigger Criticals packet hop
            CriticalsModule crits = RedStarClient.MODULES.get(CriticalsModule.class);
            if (crits != null && crits.isEnabled()) {
                crits.onAttack();
            }

            // 3. Dispatch Attack
            mc.gameMode.attack(mc.player, le);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void onDisable() {
        currentTarget = null;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Angriffs-Modus##ka", attackMode, ATTACK_MODES);

        if (attackMode.get() == 0) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "1.9+ Cooldown: Schlaegt exakt bei 100% Waffenkraft fuer max. Schaden.");
        } else {
            ImGui.sliderInt("Max CPS##ka", maxCPS.getData(), 1, 20);
        }

        ImGui.sliderFloat("Reichweite##ka", range.getData(), 1.0f, 6.0f, "%.2f Bloecke");
        ImGui.checkbox("Auto-Blickrichtung (Aim Tracking)##ka", autoRotate);
        ImGui.checkbox("Nur Spieler angreifen##ka", playersOnly);
        ImGui.checkbox("Durch Waende schlagen##ka", throughWalls);

        if (currentTarget != null) {
            ImGui.separator();
            ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "Aktuelles Ziel: " + currentTarget.getName().getString() + " (" + String.format("%.1f", currentTarget.getHealth()) + " HP)");
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("attackMode", attackMode.get());
        cfg.putSetting("range", range.get());
        cfg.putSetting("maxCPS", maxCPS.get());
        cfg.putSetting("autoRotate", autoRotate.get());
        cfg.putSetting("playersOnly", playersOnly.get());
        cfg.putSetting("throughWalls", throughWalls.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        attackMode.set(cfg.getSetting("attackMode", attackMode.get()));
        range.set(cfg.getSetting("range", range.get()));
        maxCPS.set(cfg.getSetting("maxCPS", maxCPS.get()));
        autoRotate.set(cfg.getSetting("autoRotate", autoRotate.get()));
        playersOnly.set(cfg.getSetting("playersOnly", playersOnly.get()));
        throughWalls.set(cfg.getSetting("throughWalls", throughWalls.get()));
    }
}
