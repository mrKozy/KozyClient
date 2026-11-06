// ☭ Red Star Client — LevelRendererMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.BlockOutlineModule;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @ModifyVariable(method = "submitHitOutline", at = @At("HEAD"), ordinal = 0, argsOnly = true, require = 0)
    private int redstar$customBlockOutlineColor(int originalColor) {
        BlockOutlineModule module = RedStarClient.MODULES.get(BlockOutlineModule.class);
        if (module != null && module.isEnabled()) {
            return module.getOutlineColor();
        }
        return originalColor;
    }
}
