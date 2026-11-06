// ☭ Red Star Client — NoFallModule
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/**
 * Cancels fall damage safely with anti-cheat bypasses.
 * Modes:
 * - Grim2371: Ported from LiquidBounce NoFallGrim2371.
 * - PacketSpoof: Continuous ground status spoofing.
 */
public class NoFallModule extends Module {

    private static final String[] MODES = { "Grim 2.3.71+ (Jump Status)", "Packet Spoof" };
    private final ImInt mode = new ImInt(0);

    public NoFallModule() {
        super("NoFall", "Cancels fall damage safely with AC bypasses.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null || !isEnabled()) return;
        if (mc.player.isFallFlying()) return;

        if (mode.get() == 0) { // Grim 2.3.71+ Mode
            if (!mc.player.onGround() && mc.player.fallDistance > 2.5f) {
                // Send StatusOnly with onGround=true and current horizontal collision state
                mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true, mc.player.horizontalCollision));
                // Force jump input to prevent ground collision velocity check flag
                mc.player.jumpFromGround();
            }
        } else { // Packet Spoof Mode
            if (mc.player.fallDistance > 2.0f) {
                mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(true, false));
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Mode##nofall", mode, MODES);

        if (mode.get() == 0) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Grim 2.3.71+ Mode: Sends status sync + jump input at > 2.5 blocks.");
            ImGui.textDisabled("Bypasses GrimAC fall damage checks without setback.");
        } else {
            ImGui.textDisabled("Packet Spoof: Standard onGround = true status spoof.");
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
