// ☭ Red Star Client — GuiMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.CrosshairModule;
import com.redstarclient.module.render.NoRenderModule;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public class GuiMixin {

    @Inject(method = "extractCrosshair", at = @At("HEAD"), cancellable = true, require = 0)
    private void redstar$hideVanillaCrosshair(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(CrosshairModule.class)) {
            ci.cancel();
        }
    }

    @Inject(method = "extractTextureOverlay", at = @At("HEAD"), cancellable = true, require = 0)
    private void redstar$noPumpkinBlur(GuiGraphicsExtractor graphics, Identifier shaderLocation, float alpha, CallbackInfo ci) {
        NoRenderModule noRender = RedStarClient.MODULES.get(NoRenderModule.class);
        if (noRender != null && noRender.isNoPumpkin() && shaderLocation != null && shaderLocation.getPath().contains("pumpkinblur")) {
            ci.cancel();
        }
    }
}
