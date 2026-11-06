// ☭ Red Star Client — ClientPlayNetworkHandlerMixin (Fabric)
package com.redstarclient.fabric.mixin;

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

    @Inject(method = "handleMovePlayer", at = @At("HEAD"))
    private void redstar$onMovePlayer(net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket packet, CallbackInfo ci) {
        com.redstarclient.module.combat.AntiKickModule ak = RedStarClient.MODULES.get(com.redstarclient.module.combat.AntiKickModule.class);
        if (ak != null && ak.isEnabled()) {
            ak.notifySetback();
        }
    }

    @Inject(method = "handleSetTime", at = @At("HEAD"), cancellable = true)
    private void redstar$onSetTime(net.minecraft.network.protocol.game.ClientboundSetTimePacket packet, CallbackInfo ci) {
        com.redstarclient.module.render.TimeChangerModule tc = RedStarClient.MODULES.get(com.redstarclient.module.render.TimeChangerModule.class);
        if (tc != null && tc.isEnabled()) {
            ci.cancel();
        }
    }

    // TODO: MC 1.21.4 - onDisconnect method no longer exists, needs reimplementation
    /*
    @Inject(method = "onDisconnect", at = @At("HEAD"))
    private void redstar$onDisconnect(Component reason, CallbackInfo ci) {
        RedStarClient.EVENT_BUS.post(new DisconnectEvent(reason.getString()));
    }
    */
}
