// ☭ Red Star Client — SpeedModule
package com.redstarclient.module.movement;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.module.combat.AntiKickModule;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Increases horizontal movement speed.
 * Modes:
 * - Normal: Direct velocity multiplication (with AntiKick support).
 * - GrimCollide: Exploits GrimAC 2.3.71+ collision leniency (0.08 per entity).
 */
public class SpeedModule extends Module {

    private static final String[] MODES = { "Normal", "GrimCollide (GrimAC)" };
    private final ImInt     mode            = new ImInt(0);
    private final ImFloat   speedMultiplier = new ImFloat(1.8f);
    private final ImBoolean onlyOnGround    = new ImBoolean(true);
    private final ImFloat   grimSpeed       = new ImFloat(0.08f);
    private final ImFloat   grimBox         = new ImFloat(0.5f);

    public SpeedModule() {
        super("Speed", "Increases movement speed with anti-cheat bypasses.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || !isEnabled()) return;

        AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
        if (ak != null && ak.isSafetyPaused()) return;

        if (mode.get() == 1) { // GrimCollide Mode
            // Must have movement input
            Vec3 input = mc.player.getDeltaMovement();
            if (Math.abs(input.x) < 0.001 && Math.abs(input.z) < 0.001) return;

            AABB box = mc.player.getBoundingBox().inflate(grimBox.get());
            int collisions = 0;
            for (Entity entity : mc.level.getEntities(mc.player, box, e -> e instanceof LivingEntity && !(e instanceof ArmorStand))) {
                if (box.intersects(entity.getBoundingBox())) {
                    collisions++;
                }
            }

            if (collisions > 0) {
                double yaw = Math.toRadians(mc.player.getYRot());
                double boost = grimSpeed.get() * collisions;
                mc.player.push(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
            }
            return;
        }

        // Normal Mode
        if (onlyOnGround.get() && !mc.player.onGround()) return;

        Vec3 vel = mc.player.getDeltaMovement();
        if (Math.abs(vel.x) > 0.01 || Math.abs(vel.z) > 0.01) {
            float mult = speedMultiplier.get();
            if (ak != null && ak.isGrimMode()) {
                mult = Math.min(mult, 1.18f); // Stay within anti-cheat predictive limit
            }

            mc.player.setDeltaMovement(
                    vel.x * mult,
                    vel.y,
                    vel.z * mult
            );
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Mode##spd", mode, MODES);

        if (mode.get() == 1) {
            ImGui.sliderFloat("Boost Speed##spd", grimSpeed.getData(), 0.01f, 0.08f, "%.3f b/t");
            ImGui.sliderFloat("Box Inflate##spd", grimBox.getData(), 0.1f, 1.0f, "%.2f");
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "GrimCollide: Bypasses GrimAC 2.3.71+ via entity collision.");
            ImGui.textDisabled("Gives maximum vanilla collision leniency when moving near players/mobs.");
        } else {
            ImGui.sliderFloat("Multiplier##spd", speedMultiplier.getData(), 1.0f, 5.0f, "%.2fx");
            ImGui.checkbox("Only On Ground##spd", onlyOnGround);

            AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
            if (ak != null && ak.isGrimMode()) {
                ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Anti-Kick active: Speed clamped to safe 1.18x");
            }
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("mode", mode.get());
        cfg.putSetting("speedMultiplier", speedMultiplier.get());
        cfg.putSetting("onlyOnGround", onlyOnGround.get());
        cfg.putSetting("grimSpeed", grimSpeed.get());
        cfg.putSetting("grimBox", grimBox.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        mode.set(cfg.getSetting("mode", mode.get()));
        speedMultiplier.set(cfg.getSetting("speedMultiplier", speedMultiplier.get()));
        onlyOnGround.set(cfg.getSetting("onlyOnGround", onlyOnGround.get()));
        grimSpeed.set(cfg.getSetting("grimSpeed", grimSpeed.get()));
        grimBox.set(cfg.getSetting("grimBox", grimBox.get()));
    }
}
