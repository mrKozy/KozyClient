// ☭ Red Star Client — PlayerMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.movement.SafeWalkModule;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @Inject(method = "isStayingOnGroundSurface", at = @At("HEAD"), cancellable = true)
    private void redstar$safeWalk(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player && RedStarClient.MODULES.isEnabled(SafeWalkModule.class)) {
            cir.setReturnValue(true);
        }
    }
}
