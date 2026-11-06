// ☭ Red Star Client — GameRendererMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.NoHurtCamModule;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true, require = 0)
    private void redstar$noHurtCam(CameraRenderState cameraRenderState, com.mojang.blaze3d.vertex.PoseStack poseStack, CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(NoHurtCamModule.class)) {
            ci.cancel();
        }
    }
}
