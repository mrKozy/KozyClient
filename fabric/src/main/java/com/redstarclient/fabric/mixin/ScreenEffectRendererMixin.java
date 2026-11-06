// ☭ Red Star Client — ScreenEffectRendererMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.NoRenderModule;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Inject(method = "renderFire", at = @At("HEAD"), cancellable = true)
    private static void redstar$noFire(PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        NoRenderModule noRender = RedStarClient.MODULES.get(NoRenderModule.class);
        if (noRender != null && noRender.isNoFire()) {
            ci.cancel();
        }
    }
}
