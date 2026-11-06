// ☭ Red Star Client — GameRendererMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.NoHurtCamModule;
import com.redstarclient.module.render.ZoomModule;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // MC 1.21.4 render frame pass
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("TAIL"), require = 0)
    private void redstar$onRender1214(net.minecraft.client.DeltaTracker tickDelta, boolean tick, CallbackInfo ci) {
        if (RedStarClient.GUI != null) {
            RedStarClient.GUI.renderFrame();
        }
    }

    // MC 26.3 render frame pass
    @Inject(method = "render()V", at = @At("TAIL"), require = 0)
    private void redstar$onRender263(CallbackInfo ci) {
        if (RedStarClient.GUI != null) {
            RedStarClient.GUI.renderFrame();
        }
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void redstar$noHurtCam(com.mojang.blaze3d.vertex.PoseStack poseStack, float tickDelta, CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(NoHurtCamModule.class)) {
            ci.cancel();
        }
    }

    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void redstar$zoomFov(Camera camera, float tickDelta, boolean changingFov,
                                  CallbackInfoReturnable<Float> cir) {
        ZoomModule zoom = RedStarClient.MODULES.get(ZoomModule.class);
        if (zoom != null && (zoom.isZooming() || zoom.isTransitioning())) {
            cir.setReturnValue(zoom.getCurrentFOV(cir.getReturnValue(), tickDelta));
        }
    }
}
