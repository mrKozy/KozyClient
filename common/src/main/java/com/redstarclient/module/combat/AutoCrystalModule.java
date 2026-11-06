// ☭ Red Star Client — AutoCrystalModule (AntiCheat Bypass Edition)
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Advanced Crystal PvP Engine (AutoCrystal):
 * High-speed End Crystal placement on Obsidian/Bedrock and instant detonation
 * with GrimAC / Vulcan / Polar AntiCheat bypass, Silent Rotations, Raytrace wallcheck,
 * anti-suicide checks and alternating tick pacing.
 */
public class AutoCrystalModule extends Module {

    private final ImFloat   breakRange      = new ImFloat(3.0f);
    private final ImFloat   placeRange      = new ImFloat(3.2f);
    private final ImInt     breakDelayMs    = new ImInt(65);
    private final ImInt     placeDelayMs    = new ImInt(75);
    private final ImBoolean autoSwitch      = new ImBoolean(true);
    private final ImBoolean antiSuicide     = new ImBoolean(true);
    private final ImBoolean antiCheatMode   = new ImBoolean(true);
    private final ImBoolean silentRotations = new ImBoolean(true);
    private final ImBoolean rayTraceCheck   = new ImBoolean(true);

    private long lastBreakTime = 0L;
    private long lastPlaceTime = 0L;
    private boolean alternateBreak = true;

    public AutoCrystalModule() {
        super("AutoCrystal", "Platziert und zuendet End Crystals automatisch mit GrimAC-Bypass & Silent Rotations.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();

        float effectiveBreak = antiCheatMode.get() ? Math.min(2.95f, breakRange.get()) : breakRange.get();
        float effectivePlace = antiCheatMode.get() ? Math.min(3.40f, placeRange.get()) : placeRange.get();

        boolean brokeThisTick = false;

        // 1. Break existing crystals near player
        if (now - lastBreakTime >= breakDelayMs.get()) {
            EndCrystal closestCrystal = null;
            double closestDist = effectiveBreak;

            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof EndCrystal crystal && !crystal.isRemoved()) {
                    double dist = mc.player.distanceTo(crystal);
                    if (dist <= closestDist) {
                        // Anti-Cheat Line of Sight verification
                        if (rayTraceCheck.get() && !PlayerUtils.canSeeEntity(mc.player, crystal)) {
                            continue;
                        }

                        // Anti-suicide check: don't detonate if crystal is directly touching player's feet
                        if (antiSuicide.get() && dist < 1.3 && mc.player.getHealth() < 10.0f) {
                            if (!mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                                continue;
                            }
                        }

                        closestDist = dist;
                        closestCrystal = crystal;
                    }
                }
            }

            if (closestCrystal != null) {
                lastBreakTime = now;
                brokeThisTick = true;

                // Silent rotation to crystal center
                if (silentRotations.get()) {
                    float[] rots = PlayerUtils.getRotationsToEntity(mc.player, closestCrystal);
                    PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
                }

                mc.gameMode.attack(mc.player, closestCrystal);
                mc.player.swing(InteractionHand.MAIN_HAND);
            }
        }

        // In Anti-Cheat mode, pace break and place across alternating ticks
        if (antiCheatMode.get() && brokeThisTick) {
            return;
        }

        // 2. Place crystals on valid obsidian/bedrock near enemies
        if (now - lastPlaceTime >= placeDelayMs.get()) {
            Entity target = PlayerUtils.getClosestEntity(mc.player, 8.0, false);
            if (target == null) return;

            int crystalSlot = InventoryUtils.findItemInHotbar(mc.player, Items.END_CRYSTAL);
            if (crystalSlot == -1 && !mc.player.getOffhandItem().is(Items.END_CRYSTAL)) return;

            BlockPos bestPos = findBestPlacementBlock(mc, target, effectivePlace);
            if (bestPos != null) {
                Vec3 hitVec = Vec3.atCenterOf(bestPos).add(0, 0.5, 0);

                // Anti-Cheat Line of sight check for placement face
                if (rayTraceCheck.get() && !PlayerUtils.canSeeVec(mc.player, hitVec)) {
                    return;
                }

                lastPlaceTime = now;

                InteractionHand hand = mc.player.getOffhandItem().is(Items.END_CRYSTAL)
                        ? InteractionHand.OFF_HAND
                        : InteractionHand.MAIN_HAND;

                int prevSlot = mc.player.getInventory().selected;
                if (hand == InteractionHand.MAIN_HAND && autoSwitch.get() && crystalSlot != -1) {
                    mc.player.getInventory().selected = crystalSlot;
                }

                // Silent rotation to placement block top face
                if (silentRotations.get()) {
                    float[] rots = PlayerUtils.getRotationsToVec(mc.player.getEyePosition(1f), hitVec);
                    PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
                }

                BlockHitResult hitResult = new BlockHitResult(hitVec, Direction.UP, bestPos, false);
                mc.gameMode.useItemOn(mc.player, hand, hitResult);
                mc.player.swing(hand);

                if (hand == InteractionHand.MAIN_HAND && autoSwitch.get() && crystalSlot != -1) {
                    mc.player.getInventory().selected = prevSlot;
                }
            }
        }
    }

    private BlockPos findBestPlacementBlock(Minecraft mc, Entity target, float maxRange) {
        BlockPos playerPos = mc.player.blockPosition();
        int r = (int) Math.ceil(maxRange);

        BlockPos bestPos = null;
        double minTargetDist = Double.MAX_VALUE;

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (mc.player.distanceToSqr(Vec3.atCenterOf(pos)) > maxRange * maxRange) continue;

                    BlockState state = mc.level.getBlockState(pos);
                    if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK)) {
                        BlockPos above = pos.above();
                        if (mc.level.getBlockState(above).isAir()) {
                            double distToTarget = target.distanceToSqr(Vec3.atCenterOf(above));
                            if (distToTarget < minTargetDist) {
                                minTargetDist = distToTarget;
                                bestPos = pos;
                            }
                        }
                    }
                }
            }
        }
        return bestPos;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("AntiCheat / GrimAC Bypass##ac", antiCheatMode);
        ImGui.checkbox("Silent Rotations (Unsichtbare Drehung)##ac", silentRotations);
        ImGui.checkbox("Raytrace & Wand-Check (Line of Sight)##ac", rayTraceCheck);
        ImGui.separator();
        ImGui.sliderFloat("Break-Reichweite##ac", breakRange.getData(), 2.0f, 6.0f, "%.2fm");
        ImGui.sliderFloat("Place-Reichweite##ac", placeRange.getData(), 2.0f, 6.0f, "%.2fm");
        ImGui.sliderInt("Break-Verzoegerung (ms)##ac", breakDelayMs.getData(), 20, 250);
        ImGui.sliderInt("Place-Verzoegerung (ms)##ac", placeDelayMs.getData(), 20, 250);
        ImGui.separator();
        ImGui.checkbox("Auto-Switch zu Crystal##ac", autoSwitch);
        ImGui.checkbox("Anti-Suicide Schutz##ac", antiSuicide);
        ImGui.spacing();
        ImGui.textDisabled("GrimAC Bypass begrenzt Reichweite auf <=2.95m und sendet Raytrace-Winkel.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("breakRange", breakRange.get());
        cfg.putSetting("placeRange", placeRange.get());
        cfg.putSetting("breakDelayMs", breakDelayMs.get());
        cfg.putSetting("placeDelayMs", placeDelayMs.get());
        cfg.putSetting("autoSwitch", autoSwitch.get());
        cfg.putSetting("antiSuicide", antiSuicide.get());
        cfg.putSetting("antiCheatMode", antiCheatMode.get());
        cfg.putSetting("silentRotations", silentRotations.get());
        cfg.putSetting("rayTraceCheck", rayTraceCheck.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        breakRange.set(cfg.getSetting("breakRange", breakRange.get()));
        placeRange.set(cfg.getSetting("placeRange", placeRange.get()));
        breakDelayMs.set(cfg.getSetting("breakDelayMs", breakDelayMs.get()));
        placeDelayMs.set(cfg.getSetting("placeDelayMs", placeDelayMs.get()));
        autoSwitch.set(cfg.getSetting("autoSwitch", autoSwitch.get()));
        antiSuicide.set(cfg.getSetting("antiSuicide", antiSuicide.get()));
        antiCheatMode.set(cfg.getSetting("antiCheatMode", antiCheatMode.get()));
        silentRotations.set(cfg.getSetting("silentRotations", silentRotations.get()));
        rayTraceCheck.set(cfg.getSetting("rayTraceCheck", rayTraceCheck.get()));
    }
}
