package com.redstarclient.cosmetics.knife;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class KnifeRenderer {

    private static final Identifier KNIFE_TEXTURE = Identifier.fromNamespaceAndPath("redstarclient", "textures/misc/knife_base.png");
    private static final RenderType SOLID_KNIFE = RenderTypes.entitySolid(KNIFE_TEXTURE);

    private KnifeRenderer() {}

    public static boolean renderKnife(
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int light,
        ItemStack stack,
        CosmeticsModule.SwordModel swordModel,
        int side
    ) {
        if (swordModel == CosmeticsModule.SwordModel.DEFAULT) {
            return false;
        }

        KnifeSkin skin = KnifeSkin.of(swordModel.name());
        if (skin == null) {
            return false;
        }

        KnifeMaterial mat = KnifeMaterial.of(stack);
        int[] palette = mat.palette;
        int boostedLight = Math.max(light, 0x00F000F0);

        boolean inspecting = InspectHandler.isInspecting();
        float progress = inspecting ? InspectHandler.getProgress() : 0.0f;
        float weight = inspecting ? InspectHandler.getWeight() : 0.0f;

        float[] at = new float[16];
        if (weight > 0.0f) {
            skin.inspect.sample(progress, at);
        }

        poseStack.pushPose();
        if (side < 0) {
            poseStack.scale(-1.0f, 1.0f, 1.0f);
        }

        KnifeSkin.Sink sink = new KnifeSkin.Sink() {
            @Override
            public void part(PoseStack part, KnifeMesh mesh, KnifeMesh.Shine shine) {
                collector.submitCustomGeometry(part, SOLID_KNIFE, (p, out) -> {
                    mesh.emit(p, out, palette, boostedLight, shine);
                });
            }

            @Override
            public void glow(PoseStack part, KnifeMesh mesh, KnifeMesh.Shine shine) {
                collector.order(1).submitCustomGeometry(part, SOLID_KNIFE, (p, out) -> {
                    mesh.emitGlow(p, out, mat.glow, shine);
                });
            }

            @Override
            public void arm(PoseStack hand, boolean other) {}
        };

        skin.draw(poseStack, at, weight, false, 1.0f, sink, null);
        poseStack.popPose();
        return true;
    }
}
