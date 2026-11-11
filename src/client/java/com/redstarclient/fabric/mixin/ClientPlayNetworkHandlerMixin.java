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
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null && packet.id() == mc.player.getId()) {
            VelocityModule vel = RedStarClient.MODULES.get(VelocityModule.class);
            if (vel != null && vel.isEnabled()) {
                net.minecraft.world.phys.Vec3 m = packet.movement();
                // Check if this velocity packet is from fall damage
                vel.setFallDamage(m.x == 0.0 && m.z == 0.0 && m.y < 0);

                if (vel.isJumpReset()) {
                    // In JumpReset mode, let vanilla receive velocity normally.
                    // Physics jump will naturally counter it without modifying packets!
                    return;
                }

                float h = vel.getHorizontalFactor();
                float v = vel.getVerticalFactor();
                ci.cancel();
                if (h > 0 || v > 0) {
                    mc.player.lerpMotion(new net.minecraft.world.phys.Vec3(m.x * h, m.y * v, m.z * h));
                }
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

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    private void redstar$onSound(net.minecraft.network.protocol.game.ClientboundSoundPacket packet, CallbackInfo ci) {
        com.redstarclient.module.render.SoundESPModule soundESP = RedStarClient.MODULES.get(com.redstarclient.module.render.SoundESPModule.class);
        if (soundESP != null && soundESP.isEnabled()) {
            soundESP.onSoundPacket(packet);
        }
    }

    @Inject(method = "handleSoundEntityEvent", at = @At("HEAD"))
    private void redstar$onSoundEntity(net.minecraft.network.protocol.game.ClientboundSoundEntityPacket packet, CallbackInfo ci) {
        com.redstarclient.module.render.SoundESPModule soundESP = RedStarClient.MODULES.get(com.redstarclient.module.render.SoundESPModule.class);
        if (soundESP != null && soundESP.isEnabled()) {
            soundESP.onSoundEntityPacket(packet);
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
