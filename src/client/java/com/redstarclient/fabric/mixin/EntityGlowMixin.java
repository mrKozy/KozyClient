// ☭ Red Star Client — EntityGlowMixin
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.ESPModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Makes entities glow when ESP is enabled.
 * isCurrentlyGlowing() returning true triggers Minecraft's built-in
 * outline rendering (the glowing outline through walls).
 * getTeamColor() provides custom colors (Player, Hostile, Passive, Item).
 */
@Mixin(Entity.class)
public class EntityGlowMixin {

    @Inject(method = "isCurrentlyGlowing", at = @At("HEAD"), cancellable = true)
    private void redstar$forceGlow(CallbackInfoReturnable<Boolean> cir) {
        if (com.redstarclient.module.render.StreamProofModule.shouldHideVisuals()) return;
        ESPModule esp = RedStarClient.MODULES.get(ESPModule.class);
        if (esp == null || !esp.isGlowEnabled()) return;

        Entity self = (Entity) (Object) this;

        // Don't glow the local player themselves
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && self == mc.player) return;
        if (!self.isAlive() && !(self instanceof ItemEntity)) return;

        // Apply filter based on entity type
        if (self instanceof Player) {
            if (!esp.includesPlayers()) return;
        } else if (self instanceof Monster || self instanceof Enemy) {
            if (!esp.includesHostileMobs()) return;
        } else if (self instanceof Animal || self instanceof WaterAnimal || self instanceof Villager) {
            if (!esp.includesPassiveMobs()) return;
        } else if (self instanceof ItemEntity) {
            if (!esp.includesItems()) return;
        } else {
            if (!esp.includesPassiveMobs()) return;
        }

        cir.setReturnValue(true);
    }

    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void redstar$espTeamColor(CallbackInfoReturnable<Integer> cir) {
        ESPModule esp = RedStarClient.MODULES.get(ESPModule.class);
        if (esp == null || !esp.isEnabled()) return;

        Entity self = (Entity) (Object) this;
        int color = esp.getColorRGBForEntity(self);
        if (color != 0) {
            cir.setReturnValue(color);
        }
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
