// ☭ Red Star Client — KeybindContentsMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.module.combat.AntiKickModule;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.KeybindContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * OpSec Keybind Fingerprint Protection:
 * Prevents malicious servers from querying client-bound keys via text translation components
 * to fingerprint installed mods or user configuration.
 */
@Mixin(KeybindContents.class)
public class KeybindContentsMixin {

    @Shadow @Final
    private String name;

    @Inject(method = "getNestedComponent", at = @At("HEAD"), cancellable = true)
    private void redstar$protectKeybind(CallbackInfoReturnable<Component> cir) {
        AntiKickModule ak = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(AntiKickModule.class) : null;
        if (ak != null && ak.isEnabled() && ak.isKeybindProtection()) {
            if (name != null && !name.startsWith("key.keyboard.") && !name.startsWith("key.mouse.") && !name.startsWith("key.categories.")) {
                cir.setReturnValue(Component.literal(name));
            }
        }
    }
}
