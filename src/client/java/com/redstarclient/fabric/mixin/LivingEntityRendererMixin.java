package com.redstarclient.fabric.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.RedStarClient;
import com.redstarclient.cosmetics.CharacterLayer;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void redstar$hidePlayerModel(
        LivingEntityRenderState state,
        boolean bodyVisible,
        boolean translucent,
        boolean glowing,
        CallbackInfoReturnable<RenderType> cir
    ) {
        if (state instanceof AvatarRenderState) {
            CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
            if (mod != null && mod.isEnabled() && mod.getCharacterSkin() != CosmeticsModule.CharacterSkin.NONE) {
                cir.setReturnValue(null);
            }
        }
    }

    @WrapOperation(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"
        )
    )
    private void redstar$hideLayers(
        RenderLayer<?, ?> layer,
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int light,
        EntityRenderState state,
        float yRot,
        float xRot,
        Operation<Void> original
    ) {
        if (state instanceof AvatarRenderState) {
            CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
            if (mod != null && mod.isEnabled() && mod.getCharacterSkin() != CosmeticsModule.CharacterSkin.NONE) {
                if (!(layer instanceof CharacterLayer)) {
                    return; // Skip vanilla armor and cape layers when wearing custom 3D character
                }
            }
        }
        original.call(layer, poseStack, collector, light, state, yRot, xRot);
    }
}
