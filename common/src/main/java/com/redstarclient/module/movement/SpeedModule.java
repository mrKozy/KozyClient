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
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/** Increases horizontal movement speed. Integrates with AntiKick to prevent speed kicks. */
public class SpeedModule extends Module {

    private final ImFloat   speedMultiplier = new ImFloat(1.8f);
    private final ImBoolean onlyOnGround    = new ImBoolean(true);

    public SpeedModule() {
        super("Speed", "Drastically increases movement speed.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
        if (ak != null && ak.isSafetyPaused()) return;

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
        ImGui.sliderFloat("Multiplier##spd", speedMultiplier.getData(), 1.0f, 5.0f, "%.2fx");
        ImGui.checkbox("Only On Ground##spd", onlyOnGround);

        AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
        if (ak != null && ak.isGrimMode()) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Anti-Kick active: Speed clamped to safe 1.18x");
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("speedMultiplier", speedMultiplier.get());
        cfg.putSetting("onlyOnGround", onlyOnGround.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        speedMultiplier.set(cfg.getSetting("speedMultiplier", speedMultiplier.get()));
        onlyOnGround.set(cfg.getSetting("onlyOnGround", onlyOnGround.get()));
    }
}
