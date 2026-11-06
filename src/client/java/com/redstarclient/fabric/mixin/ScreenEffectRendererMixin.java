// ☭ Red Star Client — ScreenEffectRendererMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.NoRenderModule;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Inject(method = "submitFire", at = @At("HEAD"), cancellable = true, require = 0)
    private static void redstar$noFire(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, TextureAtlasSprite sprite, CallbackInfo ci) {
        NoRenderModule noRender = RedStarClient.MODULES.get(NoRenderModule.class);
        if (noRender != null && noRender.isNoFire()) {
            ci.cancel();
        }
    }
}
