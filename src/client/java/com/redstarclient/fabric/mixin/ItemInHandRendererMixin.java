// ☭ Red Star Client — ItemInHandRendererMixin (1.7/1.8 Sword PvP Animations & Custom 3D Knife Replacement)
package com.redstarclient.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.SwordAnimationModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    @Shadow private float mainHandHeight;
    @Shadow private float oMainHandHeight;

    @Shadow protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm arm, float equipProgress);
    @Shadow protected abstract void swingArm(float swingProgress, PoseStack poseStack, int side, HumanoidArm arm);
    @Shadow public abstract void renderItem(LivingEntity entity, ItemStack itemStack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector buffer, int combinedLight);

    /**
     * Replaces the held sword with the authentic 3D Knife (Butterfly, Karambit, Katana)
     * textured with knife_base.png and animated with CS:GO inspect tricks.
     */
    @Inject(method = "renderItem", at = @At("HEAD"), cancellable = true)
    private void redstar$renderCustomKnife(
            LivingEntity entity,
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            SubmitNodeCollector buffer,
            int combinedLight,
            CallbackInfo ci
    ) {
        if (displayContext != ItemDisplayContext.FIRST_PERSON_RIGHT_HAND && displayContext != ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            return;
        }
        if (itemStack.isEmpty() || !itemStack.is(ItemTags.SWORDS)) {
            return;
        }
        com.redstarclient.module.render.CosmeticsModule cos = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(com.redstarclient.module.render.CosmeticsModule.class) : null;
        if (cos == null || !cos.isEnabled() || cos.getSwordModel() == com.redstarclient.module.render.CosmeticsModule.SwordModel.DEFAULT) {
            return;
        }

        boolean isRightHand = (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        int side = isRightHand ? 1 : -1;

        if (com.redstarclient.cosmetics.knife.KnifeRenderer.renderKnife(
                poseStack, buffer, combinedLight, itemStack, cos.getSwordModel(), side
        )) {
            ci.cancel();
        }
    }

    /**
     * Prevents sword from dropping / re-equipping downwards when attacking or clicking.
     * Restores classic 1.7 / 1.8 sword responsiveness.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void redstar$preventReEquipDip(CallbackInfo ci) {
        SwordAnimationModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(SwordAnimationModule.class) : null;
        if (mod != null && mod.isPreventReEquip()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && mc.player.getMainHandItem().is(ItemTags.SWORDS)) {
                this.mainHandHeight = 1.0f;
                this.oMainHandHeight = 1.0f;
            }
        }
    }

    /**
     * Custom 1.7.10 / 1.8 sword blocking and block-hit animations.
     */
    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void redstar$renderSwordBlockHit(
            AbstractClientPlayer player,
            float tickDelta,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equipProgress,
            PoseStack poseStack,
            SubmitNodeCollector buffer,
            int combinedLight,
            CallbackInfo ci) {

        SwordAnimationModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(SwordAnimationModule.class) : null;
        if (mod == null || !mod.isEnabled()) return;
        if (!stack.is(ItemTags.SWORDS)) return;

        boolean isMainHand = (hand == InteractionHand.MAIN_HAND);
        if (!isMainHand) return;

        Minecraft mc = Minecraft.getInstance();
        boolean isHoldingUse = mc.options.keyUse.isDown();
        boolean isBlocking = mod.isBlockHit() && isHoldingUse;

        boolean hasCustomTransform = mod.getScale() != 1.0f || mod.getPosX() != 0.0f || mod.getPosY() != 0.0f || mod.getPosZ() != 0.0f;
        if (!isBlocking && !hasCustomTransform) {
            return;
        }

        HumanoidArm arm = player.getMainArm();
        boolean isRightHand = (arm == HumanoidArm.RIGHT);
        int side = isRightHand ? 1 : -1;

        poseStack.pushPose();

        // Custom scale & position offsets
        if (mod.getScale() != 1.0f) {
            poseStack.scale(mod.getScale(), mod.getScale(), mod.getScale());
        }
        if (mod.getPosX() != 0.0f || mod.getPosY() != 0.0f || mod.getPosZ() != 0.0f) {
            poseStack.translate(mod.getPosX(), mod.getPosY(), mod.getPosZ());
        }

        // Always apply base item arm transform so the item is in the hand correctly
        this.applyItemArmTransform(poseStack, arm, equipProgress);

        if (isBlocking) {
            // 1.7.10 classic sword block position
            poseStack.translate(side * -0.14142136f, 0.08f, 0.14142136f);
            poseStack.mulPose(Axis.XP.rotationDegrees(-102.25f));
            poseStack.mulPose(Axis.YP.rotationDegrees(side * 13.365f));
            poseStack.mulPose(Axis.ZP.rotationDegrees(side * 78.05f));

            // 1.7.10 blockhit swing animation
            if (swingProgress > 0.0f) {
                float f = Mth.sin(swingProgress * swingProgress * (float) Math.PI);
                float f1 = Mth.sin(Mth.sqrt(swingProgress) * (float) Math.PI);
                poseStack.mulPose(Axis.YP.rotationDegrees(side * f * 20.0f));
                poseStack.mulPose(Axis.ZP.rotationDegrees(side * 13.365f));
                poseStack.mulPose(Axis.XP.rotationDegrees(side * -f1 * 40.0f));
            }
        } else {
            this.swingArm(swingProgress, poseStack, side, arm);
        }

        ItemDisplayContext displayContext = isRightHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        this.renderItem(player, stack, displayContext, poseStack, buffer, combinedLight);

        poseStack.popPose();
        ci.cancel();
    }
}
