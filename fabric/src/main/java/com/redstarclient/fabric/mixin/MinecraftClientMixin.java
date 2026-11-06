// ☭ Red Star Client — MinecraftClientMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.gui.ImGuiManager;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void redstar$onMinecraftInit(CallbackInfo ci) {
        // Initialize ImGui after Minecraft window is created
        Minecraft minecraft = (Minecraft) (Object) this;
        long windowHandle = minecraft.getWindow().getWindow();
        ImGuiManager.initialize(windowHandle);
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void redstar$onMinecraftClose(CallbackInfo ci) {
        // Shutdown ImGui when Minecraft closes
        ImGuiManager.shutdown();
    }
}
