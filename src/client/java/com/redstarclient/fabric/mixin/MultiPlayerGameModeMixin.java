// ☭ Red Star Client — MultiPlayerGameModeMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.combat.ComboCounterModule;
import com.redstarclient.module.combat.ReachDisplayModule;
import com.redstarclient.module.render.ParticleMultiplierModule;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class MultiPlayerGameModeMixin {

    @Shadow private int destroyDelay;

    @Inject(method = "continueDestroyBlock", at = @At("HEAD"))
    private void redstar$fastBreak(net.minecraft.core.BlockPos pos, net.minecraft.core.Direction direction, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        com.redstarclient.module.player.FastBreakModule fastBreak = RedStarClient.MODULES.get(com.redstarclient.module.player.FastBreakModule.class);
        if (fastBreak != null && fastBreak.isRemoveDelay()) {
            this.destroyDelay = 0;
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void redstar$onAttack(Player player, Entity target, CallbackInfo ci) {
        if (player == null || target == null) return;

        double distance = player.distanceTo(target);

        ReachDisplayModule reach = RedStarClient.MODULES.get(ReachDisplayModule.class);
        if (reach != null && reach.isEnabled()) {
            reach.onEntityHit(distance);
        }

        ComboCounterModule combo = RedStarClient.MODULES.get(ComboCounterModule.class);
        if (combo != null && combo.isEnabled()) {
            combo.onHitLanded();
        }

        ParticleMultiplierModule part = RedStarClient.MODULES.get(ParticleMultiplierModule.class);
        if (part != null && part.isEnabled()) {
            part.onAttackEntity(target);
        }

        com.redstarclient.module.combat.WTapModule wtap = RedStarClient.MODULES.get(com.redstarclient.module.combat.WTapModule.class);
        if (wtap != null) {
            wtap.onAttackLanded();
        }
    }

    @org.spongepowered.asm.mixin.injection.Inject(method = "destroyBlock", at = @org.spongepowered.asm.mixin.injection.At("HEAD"))
    private void redstar$onDestroyBlock(net.minecraft.core.BlockPos pos, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> cir) {
        com.redstarclient.module.player.AutoTunnelModule tunnel = RedStarClient.MODULES.get(com.redstarclient.module.player.AutoTunnelModule.class);
        if (tunnel != null && tunnel.isEnabled()) {
            tunnel.onBlockBroken(pos, null);
        }
    }
}
