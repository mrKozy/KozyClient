// ☭ Red Star Client — FramerateLimitTrackerMixin (FPS Optimizer)
package com.redstarclient.fabric.mixin;

import com.mojang.blaze3d.platform.FramerateLimitTracker;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FramerateLimitTracker.class)
public class FramerateLimitTrackerMixin {

    @Shadow @Final private Options options;
    @Shadow private int framerateLimit;

    /**
     * Prevents Minecraft 1.21.4 from dropping to 10 FPS on inactivity, iconification,
     * background window, or menu screens. Guarantees maximum framerate performance.
     */
    @Inject(method = "getFramerateLimit", at = @At("HEAD"), cancellable = true)
    private void redstar$unlockFramerate(CallbackInfoReturnable<Integer> cir) {
        int limit = this.framerateLimit;
        if (limit <= 0 && this.options != null && this.options.framerateLimit() != null) {
            limit = this.options.framerateLimit().get();
        }
        // 260 is treated by Minecraft as uncapped / unlimited
        cir.setReturnValue(limit > 0 ? limit : 260);
    }
}
