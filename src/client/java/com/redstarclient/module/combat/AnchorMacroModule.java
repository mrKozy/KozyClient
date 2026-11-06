// â˜­ Red Star Client â€” AnchorMacroModule (AntiCheat Bypass Edition)
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Anchor Macro & Safe Anchor:
 * Automatically places, charges (Glowstone), and detonates Respawn Anchors
 * in Overworld / End PvP with GrimAC / Vulcan AntiCheat bypass, Silent Rotations,
 * and Safe-Anchor explosion blast absorption.
 */
public class AnchorMacroModule extends Module {

    private final ImFloat   range           = new ImFloat(3.0f);
    private final ImBoolean safeAnchor      = new ImBoolean(true);
    private final ImBoolean autoCharge      = new ImBoolean(true);
    private final ImBoolean antiCheatMode   = new ImBoolean(true);
    private final ImBoolean silentRotations = new ImBoolean(true);
    private final ImBoolean rayTraceCheck   = new ImBoolean(true);

    private long lastActionTime = 0L;

    public AnchorMacroModule() {
        super("AnchorMacro", "Platziert, laedt und zuendet Respawn-Anker automatisch mit GrimAC-Bypass & Safe-Anchor.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();
        if (now - lastActionTime < 65) return;

        float effectiveRange = antiCheatMode.get() ? Math.min(2.95f, range.get()) : range.get();

        BlockPos playerPos = mc.player.blockPosition();
        int r = (int) Math.ceil(effectiveRange);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    BlockState state = mc.level.getBlockState(pos);

                    if (state.is(Blocks.RESPAWN_ANCHOR)) {
                        double distSq = mc.player.distanceToSqr(Vec3.atCenterOf(pos));
                        if (distSq > effectiveRange * effectiveRange) continue;

                        Vec3 hitVec = Vec3.atCenterOf(pos).add(0, 0.5, 0);

                        // Line of sight raytrace verification
                        if (rayTraceCheck.get() && !PlayerUtils.canSeeVec(mc.player, hitVec)) {
                            continue;
                        }

                        int charges = state.getValue(RespawnAnchorBlock.CHARGE);

                        if (charges == 0 && autoCharge.get()) {
                            // Charge with Glowstone
                            int glowSlot = InventoryUtils.findItemInHotbar(mc.player, Items.GLOWSTONE);
                            if (glowSlot != -1) {
                                lastActionTime = now;
                                int prev = mc.player.getInventory().getSelectedSlot();
                                mc.player.getInventory().setSelectedSlot(glowSlot);

                                // Silent rotation to top face
                                if (silentRotations.get()) {
                                    float[] rots = PlayerUtils.getRotationsToVec(mc.player.getEyePosition(1f), hitVec);
                                    PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
                                }

                                BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
                                mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                                mc.player.swing(InteractionHand.MAIN_HAND);

                                mc.player.getInventory().setSelectedSlot(prev);
                                return;
                            }
                        } else if (charges > 0) {
                            // Safe Anchor check: don't detonate if player health is critically low without totem
                            if (safeAnchor.get() && mc.player.getHealth() < 10.0f && Math.sqrt(distSq) < 2.5) {
                                if (!mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                                    continue;
                                }
                            }

                            // Detonate anchor!
                            lastActionTime = now;

                            if (silentRotations.get()) {
                                float[] rots = PlayerUtils.getRotationsToVec(mc.player.getEyePosition(1f), hitVec);
                                PlayerUtils.sendSilentRotation(mc, rots[0], rots[1]);
                            }

                            BlockHitResult hit = new BlockHitResult(hitVec, Direction.UP, pos, false);
                            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
                            mc.player.swing(InteractionHand.MAIN_HAND);
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("AntiCheat / GrimAC Bypass##anc", antiCheatMode);
        ImGui.checkbox("Silent Rotations##anc", silentRotations);
        ImGui.checkbox("Raytrace Line-of-Sight##anc", rayTraceCheck);
        ImGui.separator();
        ImGui.sliderFloat("Reichweite##anc", range.getData(), 2.0f, 6.0f, "%.2fm");
        ImGui.checkbox("Auto-Charge mit Glowstone##anc", autoCharge);
        ImGui.checkbox("Safe Anchor (Schadensschutz)##anc", safeAnchor);
        ImGui.spacing();
        ImGui.textDisabled("Zuendet Anker in Overworld/End fuer massive Explosionen bei vollstaendiger AC-Sicherheit.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("safeAnchor", safeAnchor.get());
        cfg.putSetting("autoCharge", autoCharge.get());
        cfg.putSetting("antiCheatMode", antiCheatMode.get());
        cfg.putSetting("silentRotations", silentRotations.get());
        cfg.putSetting("rayTraceCheck", rayTraceCheck.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        safeAnchor.set(cfg.getSetting("safeAnchor", safeAnchor.get()));
        autoCharge.set(cfg.getSetting("autoCharge", autoCharge.get()));
        antiCheatMode.set(cfg.getSetting("antiCheatMode", antiCheatMode.get()));
        silentRotations.set(cfg.getSetting("silentRotations", silentRotations.get()));
        rayTraceCheck.set(cfg.getSetting("rayTraceCheck", rayTraceCheck.get()));
    }
}
