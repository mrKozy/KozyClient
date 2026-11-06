// ☭ Red Star Client — CriticalsModule
package com.redstarclient.module.combat;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

/** Ensures every attack is a critical hit by simulating mini-hops. */
public class CriticalsModule extends Module {

    private static final String[] MODES = { "Packet", "MiniHop" };
    private final ImInt mode = new ImInt(0);

    public CriticalsModule() {
        super("Criticals", "Forces critical strikes on every attack.", ModuleCategory.COMBAT, -1);
    }

    /** Called before an attack is dispatched. */
    public void onAttack() {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.player.onGround() || mc.player.isInWater() || mc.getConnection() == null) return;

        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        if (mode.get() == 0) { // Packet mode
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y + 0.0625, z, false, false));
            mc.getConnection().send(new ServerboundMovePlayerPacket.Pos(x, y, z, false, false));
        } else { // MiniHop mode
            mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 0.15, mc.player.getDeltaMovement().z);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Crit Mode##crit", mode, MODES);
        ImGui.textDisabled("Packet mode bypasses visual jumping.");
    }
}
