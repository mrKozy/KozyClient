// ☭ Red Star Client — KeyboardMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.KeyPressEvent;
import net.minecraft.client.KeyboardHandler;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public class KeyboardMixin {

    @Inject(method = "keyPress", at = @At("HEAD"), require = 0)
    private void redstar$onKey(long window, int action, net.minecraft.client.input.KeyEvent event, CallbackInfo ci) {
        int key = event.key();
        if (action == GLFW.GLFW_PRESS) {
            boolean consumed = RedStarClient.handleKeyInput(key);
            if (!consumed) {
                // Dispatch to module keybinds & inspect handler
                RedStarClient.MODULES.onKeyPress(key);
                com.redstarclient.cosmetics.knife.InspectHandler.onKeyPress(key);
                RedStarClient.EVENT_BUS.post(new KeyPressEvent(key, action));
            }
        }
    }
}
