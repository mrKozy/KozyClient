// ☭ Red Star Client — MinecraftClientMixin (NeoForge)
package com.redstarclient.neoforge.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.player.FastPlaceModule;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void redstar$onTick(CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(FastPlaceModule.class)) {
            this.rightClickDelay = 0;
        }
    }
}
