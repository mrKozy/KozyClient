// ☭ Red Star Client — Fabric Entrypoint
package com.redstarclient.fabric;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.*;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.PoseStack;

@Environment(EnvType.CLIENT)
public class RedStarClientFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        RedStarClient.init();

        // ── Tick ──────────────────────────────────────────────────────────────
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RedStarClient.EVENT_BUS.post(new TickEvent(client));
            RedStarClient.MODULES.onTick(client);
            if (RedStarClient.GUI != null) {
                RedStarClient.GUI.checkDirectKeyInputs();
            }
        });

        // ── HUD render ────────────────────────────────────────────────────────
        HudElementRegistry.addLast(
            net.minecraft.resources.Identifier.fromNamespaceAndPath(RedStarClient.MOD_ID, "hud"),
            (graphics, tickCounter) -> {
                float partialTick = tickCounter.getGameTimeDeltaPartialTick(false);
                RedStarClient.EVENT_BUS.post(new RenderHudEvent(graphics, partialTick));
                RedStarClient.MODULES.onRenderHud(graphics, partialTick);
            }
        );

        // ── Built-in Wonder-Red PvP Pack (Embedded directly in client JAR) ───
        net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer(RedStarClient.MOD_ID).ifPresent(container -> {
            net.fabricmc.fabric.api.resource.ResourceManagerHelper.registerBuiltinResourcePack(
                net.minecraft.resources.Identifier.fromNamespaceAndPath(RedStarClient.MOD_ID, "redpvp"),
                container,
                net.minecraft.network.chat.Component.literal("☭ Red Star PvP (Wonder-Red)"),
                net.fabricmc.fabric.api.resource.ResourcePackActivationType.ALWAYS_ENABLED
            );
        });

        System.out.println("[☭ Red Star Client] Initialized on Fabric!");
    }
}
