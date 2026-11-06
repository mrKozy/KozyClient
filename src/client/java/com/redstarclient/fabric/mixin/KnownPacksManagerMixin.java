// ☭ Red Star Client — KnownPacksManagerMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.combat.AntiKickModule;
import net.minecraft.client.multiplayer.KnownPacksManager;
import net.minecraft.server.packs.repository.KnownPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * OpSec Known-Packs Protection:
 * In Minecraft 1.20.5 - 1.21.4, modded clients send all installed mod IDs
 * in the configuration handshake to the server.
 * This mixin strips all non-vanilla packs so servers cannot detect Fabric or installed mods.
 */
@Mixin(KnownPacksManager.class)
public class KnownPacksManagerMixin {

    @Inject(method = "trySelectingPacks", at = @At("RETURN"), cancellable = true)
    private void redstar$filterKnownPacks(List<KnownPack> serverRequested, CallbackInfoReturnable<List<KnownPack>> cir) {
        try {
            AntiKickModule ak = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(AntiKickModule.class) : null;
            if (ak != null && ak.isEnabled() && ak.isKnownPacksProtection()) {
                List<KnownPack> original = cir.getReturnValue();
                if (original == null) return;

                List<KnownPack> filtered = new ArrayList<>(original.size());
                for (KnownPack pack : original) {
                    if (pack != null && "minecraft".equals(pack.namespace())) {
                        filtered.add(pack);
                    }
                }
                if (!filtered.isEmpty()) {
                    cir.setReturnValue(filtered);
                }
            }
        } catch (Throwable ignored) {}
    }
}
