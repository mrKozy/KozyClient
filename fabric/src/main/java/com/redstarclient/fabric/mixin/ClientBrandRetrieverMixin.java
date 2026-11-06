// ☭ Red Star Client — ClientBrandRetrieverMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.combat.AntiKickModule;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Spoofs the client mod brand to "vanilla" (or chosen OpSec preset) so servers
 * cannot detect Fabric Loader or client modifications.
 */
@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {

    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true, remap = false)
    private static void redstar$onGetClientModName(CallbackInfoReturnable<String> cir) {
        AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
        if (ak != null && ak.isEnabled()) {
            cir.setReturnValue(ak.getSpoofedBrand());
        } else {
            cir.setReturnValue("vanilla");
        }
    }
}
