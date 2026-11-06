// ☭ Red Star Client — MinecraftClientMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.gui.ImGuiManager;
import com.redstarclient.module.combat.AntiKickModule;
import com.redstarclient.module.player.FastPlaceModule;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Shadow private int rightClickDelay;

    @Inject(method = "tick", at = @At("HEAD"))
    private void redstar$onMinecraftTick(CallbackInfo ci) {
        if (RedStarClient.MODULES.isEnabled(FastPlaceModule.class)) {
            AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
            if (ak != null && ak.isEnabled() && ak.isGrimMode()) {
                // Legitimate pacing to prevent FastPlace flags on Grim
                if (this.rightClickDelay > 1) {
                    this.rightClickDelay = 1;
                }
            } else {
                this.rightClickDelay = 0;
            }
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void redstar$onMinecraftInit(CallbackInfo ci) {
        // Initialize ImGui after Minecraft window is created
        Minecraft minecraft = (Minecraft) (Object) this;
        long windowHandle = minecraft.getWindow().handle();
        ImGuiManager.initialize(windowHandle);
    }

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/GpuSurface;present()V"), require = 0)
    private void redstar$onRenderFrame(boolean tick, CallbackInfo ci) {
        if (RedStarClient.GUI != null) {
            org.lwjgl.opengl.GL30.glBindFramebuffer(org.lwjgl.opengl.GL30.GL_FRAMEBUFFER, 0);
            RedStarClient.GUI.renderFrame();
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void redstar$onMinecraftClose(CallbackInfo ci) {
        // Shutdown ImGui when Minecraft closes
        ImGuiManager.shutdown();
    }
}
