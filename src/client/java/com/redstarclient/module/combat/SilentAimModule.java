// ☭ Red Star Client — SilentAimModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * Silent Aim:
 * Calculates angles to target for attacks and projectile releases silently
 * without forcing client camera snap, evading screen recording and spectator checks.
 */
public class SilentAimModule extends Module {

    private final ImFloat   range       = new ImFloat(4.5f);
    private final ImBoolean playersOnly = new ImBoolean(true);

    private float serverYaw   = 0f;
    private float serverPitch = 0f;
    private boolean hasTarget = false;

    public SilentAimModule() {
        super("SilentAim", "Richtet Angriffe unsichtbar auf den Gegner aus, ohne die Kamera zu drehen.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        Entity target = PlayerUtils.getClosestEntity(mc.player, range.get(), playersOnly.get());
        if (target != null) {
            float[] rots = PlayerUtils.getRotationsToEntity(mc.player, target);
            serverYaw   = rots[0];
            serverPitch = rots[1];
            hasTarget   = true;
        } else {
            hasTarget = false;
        }
    }

    public boolean hasTarget() {
        return isEnabled() && hasTarget;
    }

    public float getServerYaw() {
        return serverYaw;
    }

    public float getServerPitch() {
        return serverPitch;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Reichweite##sa", range.getData(), 2.0f, 6.0f, "%.1fm");
        ImGui.checkbox("Nur Spieler anvisieren##sa", playersOnly);
        ImGui.spacing();
        ImGui.textDisabled("Deine Kamera bleibt voellig frei – Server erhaelt perfekte Ziel-Winkel.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("playersOnly", playersOnly.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        playersOnly.set(cfg.getSetting("playersOnly", playersOnly.get()));
    }
}
