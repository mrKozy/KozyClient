// ☭ Red Star Client — BlinkModule (from Wurst 7)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * Blink (from Wurst 7):
 * Suspends sending movement packets to the server.
 * To other players, you appear to be frozen in place.
 * When disabled, all buffered packets are flushed at once, teleporting you forward!
 */
public class BlinkModule extends Module {

    private final ImInt maxPackets = new ImInt(100);
    private final List<Packet<?>> packetQueue = new ArrayList<>();
    private boolean flushing = false;

    public BlinkModule() {
        super("Blink", "Haelt Bewegungspakete an und sendet sie beim Deaktivieren geballt ab (Wurst 7 Teleportation).", ModuleCategory.PLAYER, -1);
    }

    public boolean isFlushing() {
        return flushing;
    }

    public synchronized void bufferPacket(Packet<?> packet) {
        if (flushing) return;
        if (packetQueue.size() >= maxPackets.get()) {
            flush();
            return;
        }
        packetQueue.add(packet);
    }

    public synchronized void flush() {
        if (mc == null || mc.getConnection() == null || packetQueue.isEmpty()) return;
        flushing = true;
        try {
            for (Packet<?> p : packetQueue) {
                mc.getConnection().send(p);
            }
            packetQueue.clear();
        } finally {
            flushing = false;
        }
    }

    @Override
    public void onDisable() {
        flush();
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Max Packets Puffer##blink", maxPackets.getData(), 10, 500);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Aktuell gepufferte Pakete: " + packetQueue.size());
        ImGui.textDisabled("Hinweis: Zu viele Pakete koennen auf manchen Servern 'Timed Out' ausloesen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("maxPackets", maxPackets.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        maxPackets.set(cfg.getSetting("maxPackets", maxPackets.get()));
    }
}
