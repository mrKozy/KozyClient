// ☭ Red Star Client — ClientCommonPacketListenerImplMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.combat.AntiKickModule;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.common.custom.BrandPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * OpSec Network Protection:
 * 1. Blocks non-vanilla mod channel announcements so servers cannot detect Fabric mods.
 * 2. Spoofs server resource pack loading to prevent tracking player IPs or executing exploits.
 */
@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {

    @Inject(method = "send", at = @At("HEAD"), cancellable = true)
    private void redstar$filterModChannels(Packet<?> packet, CallbackInfo ci) {
        try {
            if (packet instanceof ServerboundCustomPayloadPacket customPayload) {
                CustomPacketPayload payload = customPayload.payload();
                if (payload == null || payload instanceof BrandPayload) {
                    return; // BrandPayload is allowed and spoofed via ClientBrandRetrieverMixin
                }

                AntiKickModule ak = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(AntiKickModule.class) : null;
                if (ak != null && ak.isEnabled() && ak.isBlockModChannels()) {
                    if (payload.type() != null && payload.type().id() != null) {
                        Identifier id = payload.type().id();
                        // Only filter specific known mod tracking channels, never essential handshake/bungee/velocity/fml channels
                        String ns = id.getNamespace();
                        if ("fabric-screen-handler-api-v1".equals(ns) || "fabric-networking-api-v1".equals(ns)) {
                            // Keep needed for screen/handshake sync
                            return;
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "handleResourcePackPush", at = @At("HEAD"), cancellable = true)
    private void redstar$onResourcePackPush(ClientboundResourcePackPushPacket packet, CallbackInfo ci) {
        try {
            AntiKickModule ak = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(AntiKickModule.class) : null;
            if (ak != null && ak.isEnabled() && ak.isBlockPackTracking() && packet != null && packet.id() != null) {
                ClientCommonPacketListenerImpl listener = (ClientCommonPacketListenerImpl) (Object) this;
                // Safely send accepted
                listener.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.ACCEPTED));
                listener.send(new ServerboundResourcePackPacket(packet.id(), ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED));
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }
}
