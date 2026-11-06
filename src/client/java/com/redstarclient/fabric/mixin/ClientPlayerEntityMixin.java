// ☭ Red Star Client — ClientPlayerEntityMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.movement.NoFallModule;
import com.redstarclient.module.movement.SafeWalkModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "aiStep", at = @At("HEAD"))
    private void redstar$freezeInFreecam(CallbackInfo ci) {
        com.redstarclient.module.render.FreecamModule freecam = RedStarClient.MODULES.get(com.redstarclient.module.render.FreecamModule.class);
        if (freecam != null && freecam.isEnabled()) {
            LocalPlayer player = (LocalPlayer) (Object) this;
            if (player.input != null) {
                player.input.keyPresses = net.minecraft.world.entity.player.Input.EMPTY;
            }
        }
    }

    @Inject(method = "modifyInput", at = @At("RETURN"), cancellable = true)
    private void redstar$noSlow(net.minecraft.world.phys.Vec2 moveVector, CallbackInfoReturnable<net.minecraft.world.phys.Vec2> cir) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        com.redstarclient.module.movement.NoSlowModule noSlow = RedStarClient.MODULES.get(com.redstarclient.module.movement.NoSlowModule.class);
        if (noSlow != null && noSlow.isEnabled() && noSlow.isItemsEnabled()) {
            if (player.isUsingItem() && !player.isPassenger()) {
                net.minecraft.world.phys.Vec2 original = player.input.getMoveVector();
                if (original.lengthSquared() > 0) {
                    float factor = noSlow.isGrimSafe() ? 0.88f : 0.98f;
                    cir.setReturnValue(original.scale(factor));
                }
            }
        }
    }

    @Inject(method = "isMovingSlowly", at = @At("HEAD"), cancellable = true)
    private void redstar$noSlowSneak(CallbackInfoReturnable<Boolean> cir) {
        com.redstarclient.module.movement.NoSlowModule noSlow = RedStarClient.MODULES.get(com.redstarclient.module.movement.NoSlowModule.class);
        if (noSlow != null && noSlow.isEnabled() && noSlow.isSneakEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "hurtTo", at = @At("HEAD"))
    private void redstar$onHurtTo(float health, CallbackInfo ci) {
        LocalPlayer player = (LocalPlayer) (Object) this;
        if (health < player.getHealth()) {
            com.redstarclient.module.combat.ComboCounterModule combo = RedStarClient.MODULES.get(com.redstarclient.module.combat.ComboCounterModule.class);
            if (combo != null) {
                combo.onDamageTaken();
            }
        }
    }
    /*
    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void redstar$noFall(float fallDistance, float multiplier, DamageSource src, CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(NoFallModule.class)) {
            ci.cancel();
        }
    }
    */

    // TODO: MC 1.21.4 - canClipAtLedge method no longer exists, needs reimplementation
    /*
    @Inject(method = "canClipAtLedge", at = @At("HEAD"), cancellable = true)
    private void redstar$safeWalk(CallbackInfoReturnable<Boolean> cir) {
        if (RedStarClient.MODULES.isEnabled(SafeWalkModule.class)) {
            cir.setReturnValue(true);
        }
    }
    */
}
