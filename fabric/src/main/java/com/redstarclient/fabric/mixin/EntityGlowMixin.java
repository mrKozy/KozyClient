// ☭ Red Star Client — EntityGlowMixin
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.ESPModule;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes entities glow when ESP is in GLOW mode.
 * isCurrentlyGlowing() returning true triggers Minecraft's built-in
 * outline rendering (the yellow glow through walls).
 */
@Mixin(Entity.class)
public class EntityGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void redstar$forceGlow(CallbackInfoReturnable<Boolean> cir) {
        ESPModule esp = RedStarClient.MODULES.get(ESPModule.class);
        if (esp == null || !esp.isGlowEnabled()) return;

        Entity self = (Entity) (Object) this;

        // Don't glow the local player themselves
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.player != null && self == mc.player) return;
        if (!self.isAlive()) return;

        // Apply filter based on entity type
        if (self instanceof Player  && !esp.includesPlayers())     return;
        if (self instanceof Monster && !esp.includesHostileMobs())  return;
        if (self instanceof Animal  && !esp.includesPassiveMobs())  return;

        cir.setReturnValue(true);
    }

    @Inject(method = "isInvisibleTo", at = @At("HEAD"), cancellable = true)
    private void redstar$trueSight(Player player, CallbackInfoReturnable<Boolean> cir) {
        com.redstarclient.module.render.TrueSightModule ts = RedStarClient.MODULES.get(com.redstarclient.module.render.TrueSightModule.class);
        if (ts != null && ts.isEnabled()) {
            Entity self = (Entity) (Object) this;
            if (self instanceof Player && ts.includesPlayers()) {
                cir.setReturnValue(false);
            } else if (!(self instanceof Player) && ts.includesMobs()) {
                cir.setReturnValue(false);
            }
        }
    }
}
