// ☭ Red Star Client — AutoHitCrystalModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

/**
 * AutoHitCrystal (Reactive Detonation):
 * Dedicated high-speed crystal trigger. Automatically detonates any crystal
 * within attack reach the exact millisecond an enemy steps into its lethal blast radius.
 */
public class AutoHitCrystalModule extends Module {

    private final ImFloat   maxReach        = new ImFloat(3.0f);
    private final ImFloat   enemyRadius     = new ImFloat(4.2f);
    private final ImBoolean antiCheatMode   = new ImBoolean(true);
    private final ImBoolean silentRotations = new ImBoolean(true);
    private final ImBoolean rayTraceCheck   = new ImBoolean(true);
    private final ImBoolean antiSuicide     = new ImBoolean(true);

    private long lastHitTime = 0L;

    public AutoHitCrystalModule() {
        super("AutoHitCrystal", "Zuendet End Crystals reaktiv, sobald ein Gegner im Explosionsradius steht.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastHitTime < 50) return; // Tick pacing

        float reach = antiCheatMode.get() ? Math.min(2.95f, maxReach.get()) : maxReach.get();

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e instanceof EndCrystal crystal && !crystal.isRemoved()) {
                double distToPlayer = mc.player.distanceTo(crystal);
                if (distToPlayer > reach) continue;

                // Anti-Cheat Line of Sight check
                if (rayTraceCheck.get() && !PlayerUtils.canSeeEntity(mc.player, crystal)) {
                    continue;
                }

                // Anti-suicide check
                if (antiSuicide.get() && distToPlayer < 1.4 && mc.player.getHealth() < 10.0f) {
                    if (!mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                        continue;
                    }
                }

                // Check if an enemy is in lethal blast radius of the crystal
                boolean enemyNear = false;
                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity == mc.player || !(entity instanceof LivingEntity living) || !living.isAlive()) continue;
                    if (TeamCheckModule.isTeammate(entity)) continue;

                    if (crystal.distanceTo(entity) <= enemyRadius.get()) {
                        enemyNear = true;
                        break;
                    }
                }

                if (enemyNear) {
                    lastHitTime = now;

                    if (silentRotations.get()) {
                        float[] rots = PlayerUtils.getRotationsToEntity(mc.player, crystal);
                        PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
                    }

                    mc.gameMode.attack(mc.player, crystal);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    return;
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("AntiCheat / GrimAC Safe (<=2.95m)##ahc", antiCheatMode);
        ImGui.checkbox("Silent Rotations##ahc", silentRotations);
        ImGui.checkbox("Raytrace & Wand-Check##ahc", rayTraceCheck);
        ImGui.checkbox("Anti-Suicide Schutz##ahc", antiSuicide);
        ImGui.separator();
        ImGui.sliderFloat("Schlag-Reichweite##ahc", maxReach.getData(), 2.0f, 6.0f, "%.2fm");
        ImGui.sliderFloat("Gegner-Explosionsradius##ahc", enemyRadius.getData(), 2.0f, 6.0f, "%.2fm");
        ImGui.spacing();
        ImGui.textDisabled("Detoniert Kristalle sofort, sobald ein Gegner den Explosionsradius betritt.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("maxReach", maxReach.get());
        cfg.putSetting("enemyRadius", enemyRadius.get());
        cfg.putSetting("antiCheatMode", antiCheatMode.get());
        cfg.putSetting("silentRotations", silentRotations.get());
        cfg.putSetting("rayTraceCheck", rayTraceCheck.get());
        cfg.putSetting("antiSuicide", antiSuicide.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        maxReach.set(cfg.getSetting("maxReach", maxReach.get()));
        enemyRadius.set(cfg.getSetting("enemyRadius", enemyRadius.get()));
        antiCheatMode.set(cfg.getSetting("antiCheatMode", antiCheatMode.get()));
        silentRotations.set(cfg.getSetting("silentRotations", silentRotations.get()));
        rayTraceCheck.set(cfg.getSetting("rayTraceCheck", rayTraceCheck.get()));
        antiSuicide.set(cfg.getSetting("antiSuicide", antiSuicide.get()));
    }
}
