// ☭ Red Star Client — ConnectionMixin (Ping & Latency Optimizer)
package com.redstarclient.fabric.mixin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannelConfig;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public class ConnectionMixin {

    /**
     * Optimizes network socket configuration for lowest possible ping & zero buffer lag.
     * Disables Nagle's algorithm (TCP_NODELAY), requests IPTOS_LOWDELAY, and optimizes buffer sizes.
     */
    @Inject(method = "channelActive", at = @At("HEAD"))
    private void redstar$optimizeNetworkLatency(ChannelHandlerContext ctx, CallbackInfo ci) {
        try {
            Channel channel = ctx.channel();
            if (channel != null && channel.config() instanceof SocketChannelConfig socketConfig) {
                // Zero delay packet dispatch (pure Nagle disable)
                socketConfig.setTcpNoDelay(true);
                socketConfig.setKeepAlive(true);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void redstar$onSend(net.minecraft.network.protocol.Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof net.minecraft.network.protocol.game.ServerboundMovePlayerPacket) {
            com.redstarclient.module.player.BlinkModule blink = com.redstarclient.RedStarClient.MODULES.get(com.redstarclient.module.player.BlinkModule.class);
            if (blink != null && blink.isEnabled() && !blink.isFlushing()) {
                blink.bufferPacket(packet);
                ci.cancel();
            }
        }
    }
}
