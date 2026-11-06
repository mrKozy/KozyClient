// ☭ Red Star Client — GuiMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.CrosshairModule;
import com.redstarclient.module.render.NoRenderModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void redstar$hideVanillaCrosshair(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(CrosshairModule.class)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderTextureOverlay", at = @At("HEAD"), cancellable = true)
    private void redstar$noPumpkinBlur(GuiGraphics graphics, ResourceLocation shaderLocation, float alpha, CallbackInfo ci) {
        NoRenderModule noRender = RedStarClient.MODULES.get(NoRenderModule.class);
        if (noRender != null && noRender.isNoPumpkin() && shaderLocation != null && shaderLocation.getPath().contains("pumpkinblur")) {
            ci.cancel();
        }
    }
}
