// ☭ Red Star Client — ChatMixin (NeoForge)
package com.redstarclient.neoforge.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.event.events.ChatEvent;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatComponent.class)
public class ChatMixin {

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"), cancellable = true)
    private void redstar$onChat(Component message, CallbackInfo ci) {
        ChatEvent event = new ChatEvent(message.getString());
        RedStarClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}
