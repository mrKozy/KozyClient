// ☭ Red Star Client — LevelExtractorMixin
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.ESPModule;
import com.redstarclient.module.render.StreamProofModule;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Ensures that entities behind walls / unexposed chunk sections are extracted
 * for rendering when ESP is enabled, defeating chunk occlusion culling.
 * Also extracts 3D world gizmos (ESP boxes, StorageESP containers, Tracers)
 * directly into the main thread gizmo collector each frame.
 */
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

    @Inject(method = "isEntityVisible", at = @At("HEAD"), cancellable = true)
    private void redstar$espMakeVisible(Entity entity, Frustum frustum, double d, double e, double f, CallbackInfoReturnable<Boolean> cir) {
        if (StreamProofModule.shouldHideVisuals()) return;

        ESPModule esp = RedStarClient.MODULES.get(ESPModule.class);
        if (esp != null && esp.isEnabled()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && entity == mc.player) return;
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractGizmos", at = @At("HEAD"))
    private void redstar$extractGizmos(CallbackInfo ci) {
        if (StreamProofModule.shouldHideVisuals()) return;

        LevelExtractor self = (LevelExtractor) (Object) this;
        try (net.minecraft.gizmos.Gizmos.TemporaryCollection col = self.collectPerFrameMainThreadGizmos()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.player != null) {
                float tickDelta = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
                Camera camera = mc.gameRenderer.mainCamera();
                RedStarClient.MODULES.onRenderWorld3D(null, camera, tickDelta);
            }
        } catch (Throwable ignored) {}
    }
}
