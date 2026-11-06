// ☭ Red Star Client — ChatMixin (Fabric)
package com.redstarclient.fabric.mixin;

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

    @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void redstar$onChat(
            Component message,
            net.minecraft.network.chat.MessageSignature signature,
            net.minecraft.client.multiplayer.chat.GuiMessageSource source,
            net.minecraft.client.multiplayer.chat.GuiMessageTag tag,
            CallbackInfo ci) {
        String msgStr = message.getString();
        ChatEvent event = new ChatEvent(msgStr);
        RedStarClient.EVENT_BUS.post(event);
        if (event.isCancelled()) {
            ci.cancel();
            return;
        }

        com.redstarclient.module.player.AutoGGModule autoGG = RedStarClient.MODULES.get(com.redstarclient.module.player.AutoGGModule.class);
        if (autoGG != null && autoGG.isEnabled()) {
            autoGG.onChatMessage(msgStr);
        }
    }
}
