// ☭ Red Star Client — TitleScreenMixin (Fabric)
package com.redstarclient.fabric.mixin;

import com.redstarclient.RedStarClient;
import com.redstarclient.render.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.SplashRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    private static final Identifier LOGO_TEXTURE = Identifier.fromNamespaceAndPath(RedStarClient.MOD_ID, "textures/gui/logo.png");

    private static long splashStartTime = -1L;
    private static boolean splashDone = false;

    @Shadow private SplashRenderer splash;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void redstar$onInit(CallbackInfo ci) {
        this.splash = new SplashRenderer(Component.literal("Workers of the World, Unite!"));
    }

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void redstar$renderIntroSplash(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (splashDone) return;

        if (splashStartTime == -1L) {
            splashStartTime = System.currentTimeMillis();
        }

        long elapsed = System.currentTimeMillis() - splashStartTime;
        long totalDuration = 2200L;
        long fadeStart = 1600L;

        if (elapsed >= totalDuration) {
            splashDone = true;
            return;
        }

        float alpha = 1.0f;
        if (elapsed > fadeStart) {
            alpha = 1.0f - (float) (elapsed - fadeStart) / (float) (totalDuration - fadeStart);
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));
        }

        int w = this.width;
        int h = this.height;

        // Dark sleek backdrop with smooth fade
        int bgAlpha = (int) (alpha * 0.96f * 255f);
        graphics.fill(0, 0, w, h, (bgAlpha << 24) | 0x07060A);

        int cx = w / 2;
        int cy = h / 2 - 24;

        // Render Red Star emblem (80x80)
        int size = 80;
        graphics.blit(LOGO_TEXTURE, cx - size / 2, cy - size / 2, cx + size / 2, cy + size / 2, 0.0f, 1.0f, 0.0f, 1.0f);

        // Pulsing ring / highlight border
        float pulse = (float) (Math.sin(elapsed / 180.0) * 0.15 + 0.85);
        int ringColor = RenderUtils.toARGB(0.85f, 0.12f, 0.16f, alpha * pulse);
        int goldColor = RenderUtils.toARGB(0.96f, 0.82f, 0.22f, alpha);

        // Titles
        graphics.centeredText(this.font, Component.literal("KOZY CLIENT"), cx, cy + 54, goldColor);
        graphics.centeredText(this.font, Component.literal("v3.0 — by mr.Kozy"), cx, cy + 68, ringColor);

        // Sleek progress bar
        int barW = 160;
        int barH = 3;
        int barX = cx - barW / 2;
        int barY = cy + 86;
        float progress = Math.min(1.0f, (float) elapsed / (float) fadeStart);

        graphics.fill(barX, barY, barX + barW, barY + barH, RenderUtils.toARGB(0.15f, 0.15f, 0.18f, alpha * 0.8f));
        graphics.fill(barX, barY, barX + (int) (barW * progress), barY + barH, goldColor);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!splashDone) {
            splashDone = true;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!splashDone) {
            splashDone = true;
            return true;
        }
        return super.keyPressed(event);
    }
}
