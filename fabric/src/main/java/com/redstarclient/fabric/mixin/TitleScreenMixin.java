// ☭ Red Star Client — TitleScreenMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LogoRenderer;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    private static final ResourceLocation MARX_BG = ResourceLocation.fromNamespaceAndPath(RedStarClient.MOD_ID, "textures/gui/marx.png");
    private static final ResourceLocation LOGO_ICON = ResourceLocation.fromNamespaceAndPath(RedStarClient.MOD_ID, "textures/gui/icon.png");

    @Shadow private SplashRenderer splash;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void redstar$onInit(CallbackInfo ci) {
        this.splash = new SplashRenderer("Workers of the World, Unite!");
    }

    @Inject(method = "renderPanorama", at = @At("HEAD"), cancellable = true)
    private void redstar$customPanorama(GuiGraphics guiGraphics, float partialTick, CallbackInfo ci) {
        ci.cancel();

        // 1. Dark solid background #0D0505
        guiGraphics.fill(0, 0, this.width, this.height, 0xFF0D0505);

        // 2. Right-aligned Karl Marx silhouette (16:9 aspect ratio like in launcher)
        int imgHeight = this.height;
        int imgWidth = (int) (imgHeight * (16.0 / 9.0));
        int imgX = this.width - imgWidth;
        int imgY = 0;

        try {
            guiGraphics.blit(RenderType::guiTextured, MARX_BG, imgX, imgY, 0.0f, 0.0f, imgWidth, imgHeight, imgWidth, imgHeight);
        } catch (Throwable ignored) {}

        // 3. Dark gradient overlay from left to right (smooth fade into Marx silhouette)
        guiGraphics.fillGradient(0, 0, this.width, this.height, 0xF20D0505, 0x440D0505);
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/LogoRenderer;renderLogo(Lnet/minecraft/client/gui/GuiGraphics;IF)V"))
    private void redstar$renderRedStarLogo(LogoRenderer logoRenderer, GuiGraphics guiGraphics, int width, float alpha) {
        int centerX = width / 2;
        int topY = 22;

        // 1. Draw Red Star Logo Icon
        int iconSize = 36;
        int iconX = centerX - iconSize / 2;
        try {
            guiGraphics.blit(RenderType::guiTextured, LOGO_ICON, iconX, topY, 0.0f, 0.0f, iconSize, iconSize, iconSize, iconSize);
        } catch (Throwable ignored) {}

        // 2. Title Text: "☭ RED STAR CLIENT ☭" in Soviet Gold
        String title = "§c☭ §6§lRED STAR CLIENT §c☭";
        guiGraphics.drawCenteredString(this.font, title, centerX, topY + iconSize + 4, 0xFFEBB81E);

        // 3. Subtitle
        String subtitle = "§7Workers of the World, Unite! §8| §cRed Star Launcher";
        guiGraphics.drawCenteredString(this.font, subtitle, centerX, topY + iconSize + 16, 0xFFAAAAAA);
    }
}
