package com.redstarclient.fabric.mixin;

import com.mojang.authlib.minecraft.TelemetrySession;
import com.mojang.authlib.yggdrasil.YggdrasilUserApiService;
import com.redstarclient.RedStarClient;
import com.redstarclient.module.combat.AntiKickModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.Executor;

/**
 * OpSec Privacy Protection: Disables telemetry sessions sent to external tracking endpoints.
 */
@Mixin(value = YggdrasilUserApiService.class, remap = false)
public class YggdrasilUserApiServiceMixin {

    @Inject(method = "newTelemetrySession", at = @At("HEAD"), cancellable = true)
    private void redstar$disableTelemetry(Executor executor, CallbackInfoReturnable<TelemetrySession> cir) {
        AntiKickModule ak = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(AntiKickModule.class) : null;
        if (ak == null || (ak.isEnabled() && ak.isTelemetryBlocking())) {
            cir.setReturnValue(TelemetrySession.DISABLED);
        }
    }
}
