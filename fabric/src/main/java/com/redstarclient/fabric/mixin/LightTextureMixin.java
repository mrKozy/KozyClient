// ☭ Red Star Client — LightTextureMixin (FullBright & Gamma Engine)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.FullBrightModule;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightTexture.class)
public class LightTextureMixin {

    @Inject(method = "getBrightness(FI)F", at = @At("HEAD"), cancellable = true)
    private static void redstar$onGetBrightness(float ambientLight, int lightLevel, CallbackInfoReturnable<Float> cir) {
        try {
            if (RedStarClient.MODULES != null && RedStarClient.MODULES.isEnabled(FullBrightModule.class)) {
                cir.setReturnValue(1.0f);
            }
        } catch (Throwable ignored) {}
    }
}
