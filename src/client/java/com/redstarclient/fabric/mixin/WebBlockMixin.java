// ☭ Red Star Client — WebBlockMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.movement.AntiCobwebModule;
import com.redstarclient.module.movement.NoSlowModule;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WebBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WebBlock.class)
public class WebBlockMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void redstar$onEntityInsideCobweb(BlockState state, Level world, BlockPos pos, Entity entity, InsideBlockEffectApplier handler, boolean bl, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (entity == mc.player && mc.player != null) {
            AntiCobwebModule ac = RedStarClient.MODULES.get(AntiCobwebModule.class);
            NoSlowModule ns = RedStarClient.MODULES.get(NoSlowModule.class);
            if ((ac != null && ac.isEnabled()) || (ns != null && ns.isEnabled() && ns.isWebsEnabled())) {
                ci.cancel();
            }
        }
    }
}
