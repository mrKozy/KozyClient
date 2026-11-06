// ☭ Red Star Client — BlockRenderManagerMixin (Fabric X-Ray)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.XRayModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockRenderManagerMixin {

    @Shadow public abstract Block getBlock();

    @Inject(method = "getRenderShape", at = @At("HEAD"), cancellable = true)
    private void redstar$xrayRenderShape(CallbackInfoReturnable<RenderShape> cir) {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;
        XRayModule xray = RedStarClient.MODULES.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            Block block = this.getBlock();
            if (block != null && !xray.isBlockVisible(block)) {
                cir.setReturnValue(RenderShape.INVISIBLE);
            }
        }
    }

    @Inject(method = "skipRendering", at = @At("HEAD"), cancellable = true)
    private void redstar$xraySkipRendering(BlockState adjacentState, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;
        XRayModule xray = RedStarClient.MODULES.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            if (adjacentState != null && !xray.isBlockVisible(adjacentState.getBlock())) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "getShadeBrightness", at = @At("HEAD"), cancellable = true)
    private void redstar$xrayShade(BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;
        XRayModule xray = RedStarClient.MODULES.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(1.0f);
        }
    }

    @Inject(method = "propagatesSkylightDown", at = @At("HEAD"), cancellable = true)
    private void redstar$xrayLight(CallbackInfoReturnable<Boolean> cir) {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;
        XRayModule xray = RedStarClient.MODULES.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getLightDampening", at = @At("HEAD"), cancellable = true)
    private void redstar$xrayDampening(CallbackInfoReturnable<Integer> cir) {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;
        XRayModule xray = RedStarClient.MODULES.get(XRayModule.class);
        if (xray != null && xray.isEnabled()) {
            cir.setReturnValue(0);
        }
    }
}
