// ☭ Red Star Client — CriticalsModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Forces critical strikes on every attack.
 * Modes ported from LiquidBounce:
 * - Grim: Micro-fall offset in air (-0.000001) bypasses GrimAC simulation.
 * - NoCheatPlus: 3-packet sequence (0.11, 0.1100013579, 0.0000013579).
 * - Falling: 3-packet micro falling sequence.
 * - Vanilla: Standard 0.2 packet hop.
 * - MiniHop: Client-side jump motion.
 */
public class CriticalsModule extends Module {

    private static final String[] MODES = {
        "Grim (In-Air Bypass)",
        "NoCheatPlus (NCP)",
        "Falling",
        "Vanilla",
        "MiniHop"
    };
    private final ImInt mode = new ImInt(0);

    public CriticalsModule() {
        super("Criticals", "Forces critical strikes on attacks with AC bypasses.", ModuleCategory.COMBAT, -1);
    }

    /** Called before an attack is dispatched. */
    public void onAttack() {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null || mc.player.isInWater()) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        switch (mode.get()) {
            case 0 -> { // Grim
                // If player is in air, drop slightly below server position.
                // Vanilla counts as crit while change is too small for Grim physics checks.
                if (!mc.player.onGround()) {
                    sendPacket(x, y - 0.000001, z, false);
                }
            }
            case 1 -> { // NoCheatPlus
                if (mc.player.onGround()) {
                    sendPacket(x, y + 0.11, z, false);
                    sendPacket(x, y + 0.1100013579, z, false);
                    sendPacket(x, y + 0.0000013579, z, false);
                }
            }
            case 2 -> { // Falling
                if (mc.player.onGround()) {
                    sendPacket(x, y + 0.0625, z, false);
                    sendPacket(x, y + 0.0625013579, z, false);
                    sendPacket(x, y + 0.0000013579, z, false);
                }
            }
            case 3 -> { // Vanilla
                if (mc.player.onGround()) {
                    sendPacket(x, y + 0.2, z, false);
                    sendPacket(x, y + 0.01, z, false);
                }
            }
            case 4 -> { // MiniHop
                if (mc.player.onGround()) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.15, mc.player.getDeltaMovement().z);
                }
            }
        }
    }

    private void sendPacket(double x, double y, double z, boolean onGround) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getConnection() != null && mc.player != null) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, onGround, mc.player.horizontalCollision));
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Mode##crit", mode, MODES);

        switch (mode.get()) {
            case 0 -> {
                ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Grim Mode: Bypasses GrimAC 2.3.71+ when attacking during jump/fall.");
                ImGui.textDisabled("Sends undetectable -0.000001 packet drop in air.");
            }
            case 1 -> {
                ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "NCP Mode: 3-packet micro-offset bypass for NoCheatPlus & Matrix.");
            }
            case 2 -> {
                ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Falling Mode: Micro-ground offsets for updated anticheats.");
            }
            case 3 -> {
                ImGui.textDisabled("Vanilla Mode: Standard packet packets (Works on servers with no anticheat).");
            }
            case 4 -> {
                ImGui.textDisabled("MiniHop: Physical micro-jump client-side.");
            }
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("mode", mode.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        mode.set(cfg.getSetting("mode", mode.get()));
    }
}
