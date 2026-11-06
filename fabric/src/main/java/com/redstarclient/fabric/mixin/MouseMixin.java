// ☭ Red Star Client — MouseMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.ZoomModule;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void redstar$onMouseScroll(long window, double xoffset, double yoffset, CallbackInfo ci) {
        ZoomModule zoom = RedStarClient.MODULES.get(ZoomModule.class);
        if (zoom != null && zoom.isZooming() && zoom.isScrollAdjust()) {
            zoom.onMouseScroll(yoffset);
            ci.cancel();
        }
    }
}
