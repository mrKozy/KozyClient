// ☭ Red Star Client — ClientPlayNetworkHandlerMixin (NeoForge)
package com.redstarclient.neoforge.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.DisconnectEvent;
import com.redstarclient.module.combat.VelocityModule;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"), cancellable = true)
    private void redstar$onVelocity(ClientboundSetEntityMotionPacket packet, CallbackInfo ci) {
        VelocityModule vel = RedStarClient.MODULES.get(VelocityModule.class);
        if (vel != null && vel.isEnabled()) {
            if (vel.getHorizontalFactor() == 0 && vel.getVerticalFactor() == 1.0f) {
                ci.cancel();
            }
        }
    }

    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void redstar$onDisconnect(Component reason, CallbackInfo ci) {
        RedStarClient.EVENT_BUS.post(new DisconnectEvent(reason.getString()));
    }
}
