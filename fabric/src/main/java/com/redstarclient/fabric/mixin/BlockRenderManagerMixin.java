// ☭ Red Star Client — BlockRenderManagerMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.XRayModule;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockRenderDispatcher.class)
public class BlockRenderManagerMixin {

    @Inject(method = "renderBatched", at = @At("HEAD"), cancellable = true)
    private void redstar$xrayFilter(BlockState state, BlockPos pos, BlockAndTintGetter level,
                                     PoseStack poseStack, VertexConsumer consumer,
                                     boolean checkSides, RandomSource random, CallbackInfo ci) {
        XRayModule xray = RedStarClient.MODULES.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            if (!xray.isBlockVisible(state.getBlock())) {
                ci.cancel();
            }
        }
    }
}
