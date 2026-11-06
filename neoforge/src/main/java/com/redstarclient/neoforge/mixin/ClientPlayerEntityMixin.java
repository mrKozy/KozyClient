// ☭ Red Star Client — ClientPlayerEntityMixin (NeoForge)
package com.redstarclient.neoforge.mixin;

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

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void redstar$noFall(float fallDistance, float multiplier, DamageSource src, CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(NoFallModule.class)) {
            ci.cancel();
        }
    }

    @Inject(method = "canClipAtLedge", at = @At("HEAD"), cancellable = true)
    private void redstar$safeWalk(CallbackInfoReturnable<Boolean> cir) {
        if (RedStarClient.MODULES.isEnabled(SafeWalkModule.class)) {
            cir.setReturnValue(true);
        }
    }
}
