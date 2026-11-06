// ☭ Red Star Client — NeoForge Entrypoint
package com.redstarclient.neoforge;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.RenderHudEvent;
import com.redstarclient.event.events.TickEvent;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(RedStarClient.MOD_ID)
public class RedStarClientNeoForge {

    public RedStarClientNeoForge(IEventBus modEventBus) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            RedStarClient.init();
            NeoForge.EVENT_BUS.register(this);
            System.out.println("[☭ Red Star Client] Initialized on NeoForge!");
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        RedStarClient.EVENT_BUS.post(new TickEvent(mc));
        RedStarClient.MODULES.onTick(mc);
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent.Post event) {
        float tickDelta = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        RedStarClient.EVENT_BUS.post(new RenderHudEvent(event.getGuiGraphics(), tickDelta));
        RedStarClient.MODULES.onRenderHud(event.getGuiGraphics(), tickDelta);
    }
}
