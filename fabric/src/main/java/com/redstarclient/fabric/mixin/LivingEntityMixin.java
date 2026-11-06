// ☭ Red Star Client — LivingEntityMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.movement.JesusModule;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void redstar$jesus(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LocalPlayer) {
            if (RedStarClient.MODULES.isEnabled(JesusModule.class)) {
                cir.setReturnValue(true);
            }
        }
    }
}
