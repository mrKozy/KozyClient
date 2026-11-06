// ☭ Red Star Client — AutoReconnectModule
package com.redstarclient.module.player;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.DisconnectEvent;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Automatically reconnects to the last server after a disconnect. */
public class AutoReconnectModule extends Module {

    private final ImInt delaySeconds = new ImInt(5);
    private final ImInt maxRetries   = new ImInt(10);

    private int     retryCount   = 0;
    private String  lastAddress  = null;
    private int     lastPort     = 25565;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public AutoReconnectModule() {
        super("AutoReconnect", "Reconnects automatically after disconnecting.", ModuleCategory.PLAYER, -1);
        // Subscribe to disconnect events
        RedStarClient.EVENT_BUS.subscribe(DisconnectEvent.class, this::onDisconnect);
    }

    private void onDisconnect(DisconnectEvent event) {
        if (!isEnabled()) return;
        if (retryCount >= maxRetries.get()) { retryCount = 0; return; }

        retryCount++;
        System.out.println("[☭ RedStar] Disconnected: " + event.reason +
                " — reconnecting in " + delaySeconds.get() + "s (attempt " + retryCount + ")");

        scheduler.schedule(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc == null || lastAddress == null) return;
            mc.execute(() -> {
                // Reconnect to last known server
                net.minecraft.client.gui.screens.ConnectScreen.startConnecting(
                        mc.gui != null ? mc.gui.screen() : null,
                        mc,
                        net.minecraft.client.multiplayer.resolver.ServerAddress.parseString(lastAddress + ":" + lastPort),
                        new net.minecraft.client.multiplayer.ServerData("RedStar", lastAddress + ":" + lastPort, net.minecraft.client.multiplayer.ServerData.Type.OTHER),
                        false, null
                );
            });
        }, delaySeconds.get(), TimeUnit.SECONDS);
    }

    /** Called by the connection mixin to record the server before connecting. */
    public void setLastServer(String address, int port) {
        this.lastAddress = address;
        this.lastPort    = port;
        this.retryCount  = 0;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Delay (sec)##arc",   delaySeconds.getData(), 1,  30);
        ImGui.sliderInt("Max Retries##arc",   maxRetries.getData(),   1,  50);
        if (lastAddress != null) {
            ImGui.textDisabled("Last server: " + lastAddress + ":" + lastPort);
        }
        ImGui.text("Retries: " + retryCount + " / " + maxRetries.get());
    }
}
