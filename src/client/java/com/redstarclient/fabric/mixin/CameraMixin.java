// ☭ Red Star Client — CameraMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.FreecamModule;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow protected abstract void setPosition(double x, double y, double z);
    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void redstar$onUpdateCamera(net.minecraft.client.DeltaTracker deltaTracker, CallbackInfo ci) {
        FreecamModule freecam = RedStarClient.MODULES.get(FreecamModule.class);
        if (freecam != null && freecam.isEnabled()) {
            float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
            double renderX = freecam.getInterpolatedX(partialTick);
            double renderY = freecam.getInterpolatedY(partialTick);
            double renderZ = freecam.getInterpolatedZ(partialTick);
            this.setPosition(renderX, renderY, renderZ);
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.player != null) {
                this.setRotation(mc.player.getViewYRot(partialTick), mc.player.getViewXRot(partialTick));
            }
        }
    }

    @Inject(method = "getMaxZoom", at = @At("HEAD"), cancellable = true)
    private void redstar$onGetMaxZoom(float desired, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Float> cir) {
        com.redstarclient.module.render.CameraClipModule clip = RedStarClient.MODULES.get(com.redstarclient.module.render.CameraClipModule.class);
        if (clip != null && clip.isEnabled()) {
            cir.setReturnValue(clip.getDistance());
        }
    }
}
