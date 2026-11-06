// ☭ Red Star Client — DimensionTypeMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.TimeChangerModule;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public class DimensionTypeMixin {

    @Inject(method = "timeOfDay", at = @At("HEAD"), cancellable = true)
    private void redstar$onTimeOfDay(long dayTime, CallbackInfoReturnable<Float> cir) {
        TimeChangerModule tc = RedStarClient.MODULES.get(TimeChangerModule.class);
        if (tc != null && tc.isEnabled()) {
            double d = Mth.frac((double) tc.getOverrideTime() / 24000.0 - 0.25);
            double e = 0.5 - Math.cos(d * Math.PI) / 2.0;
            cir.setReturnValue((float) ((d * 2.0 + e) / 3.0));
        }
    }
}
