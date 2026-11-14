package com.redstarclient.cosmetics.knife;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class KnifeRenderer {

    private static final Identifier KNIFE_TEXTURE = Identifier.fromNamespaceAndPath("anubis", "textures/misc/knife_base.png");
    private static final RenderType SOLID_KNIFE = RenderTypes.entityCutout(KNIFE_TEXTURE);

    private static volatile boolean initialized = false;
    private static KnifeMesh butterflyBlade;
    private static KnifeMesh butterflyHandle1;
    private static KnifeMesh butterflyHandle2;
    private static KnifeMesh karambitMesh;
    private static KnifeMesh katanaMesh;

    private KnifeRenderer() {}

    public static void ensureInitialized() {
        if (initialized) return;
        synchronized (KnifeRenderer.class) {
            if (initialized) return;
            try {
                butterflyBlade = KnifeModels.butterflyBlade();
                butterflyHandle1 = KnifeModels.butterflyHandle(1);
                butterflyHandle2 = KnifeModels.butterflyHandle(-1);
                karambitMesh = KnifeModels.karambit();
                katanaMesh = KnifeModels.katana();
                initialized = true;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static boolean renderKnife(
        PoseStack poseStack,
        SubmitNodeCollector collector,
        int light,
        ItemStack stack,
        CosmeticsModule.SwordModel swordModel,
        int side,
        float swingProgress,
        float equipProgress
    ) {
        if (swordModel == CosmeticsModule.SwordModel.DEFAULT) {
            return false;
        }

        ensureInitialized();
        if (!initialized) return false;

        KnifeMaterial mat = KnifeMaterial.of(stack);
        int[] palette = mat.palette;
        int boostedLight = Math.max(light, 0x00F000F0); // Ensure clear, vibrant visibility

        boolean inspecting = InspectHandler.isInspecting();
        float t = inspecting ? InspectHandler.getProgress() : 0.0f;

        poseStack.pushPose();

        switch (swordModel) {
            case BUTTERFLY: {
                if (inspecting) {
                    // CS:GO Butterfly Knife Inspect Spin & Handle Flip Animation
                    float spinZ = t * 720.0f;
                    float wobbleX = (float) Math.sin(t * Math.PI * 2) * 15.0f;
                    float liftY = (float) Math.sin(t * Math.PI) * 0.08f;

                    poseStack.translate(side * 0.42f, -0.40f + liftY, -0.58f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-65.0f + wobbleX));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * 40.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * (-25.0f + spinZ)));

                    float scale = 0.024f;
                    poseStack.scale(scale, scale, scale);

                    // Blade
                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        butterflyBlade.emit(pose, out, palette, boostedLight);
                    });

                    // Handle 1 (Rotates around pin during trick)
                    float h1Rot = (float) Math.sin(t * Math.PI * 4) * 150.0f;
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.ZP.rotationDegrees(h1Rot));
                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        butterflyHandle1.emit(pose, out, palette, boostedLight);
                    });
                    poseStack.popPose();

                    // Handle 2 (Counter-rotates)
                    float h2Rot = -(float) Math.sin(t * Math.PI * 4) * 150.0f;
                    poseStack.pushPose();
                    poseStack.mulPose(Axis.ZP.rotationDegrees(h2Rot));
                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        butterflyHandle2.emit(pose, out, palette, boostedLight);
                    });
                    poseStack.popPose();
                } else {
                    // Normal idle stance
                    poseStack.translate(side * 0.42f, -0.42f, -0.62f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-65.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * 40.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * -25.0f));

                    float scale = 0.024f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        butterflyBlade.emit(pose, out, palette, boostedLight);
                        butterflyHandle1.emit(pose, out, palette, boostedLight);
                        butterflyHandle2.emit(pose, out, palette, boostedLight);
                    });
                }
                break;
            }
            case KARAMBIT: {
                if (inspecting) {
                    // CS:GO Karambit 360 Spin Around Finger Ring
                    float spin = t * 720.0f;
                    float liftY = (float) Math.sin(t * Math.PI) * 0.06f;

                    poseStack.translate(side * 0.38f, -0.36f + liftY, -0.56f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-45.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * 55.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * (-110.0f + spin)));

                    float scale = 0.018f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        karambitMesh.emit(pose, out, palette, boostedLight);
                    });
                } else {
                    // Normal idle reverse grip
                    poseStack.translate(side * 0.38f, -0.38f, -0.58f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-45.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * 55.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * -110.0f));

                    float scale = 0.018f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        karambitMesh.emit(pose, out, palette, boostedLight);
                    });
                }
                break;
            }
            case KATANA: {
                if (inspecting) {
                    // Katana Inspection Tilt & Sweep
                    float tilt = (float) Math.sin(t * Math.PI * 2) * 35.0f;
                    poseStack.translate(side * 0.45f, -0.48f, -0.66f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-50.0f + tilt));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * (25.0f + (float) Math.sin(t * Math.PI) * 20.0f)));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * (15.0f + tilt * 0.5f)));

                    float scale = 0.022f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        katanaMesh.emit(pose, out, palette, boostedLight);
                    });
                } else {
                    poseStack.translate(side * 0.45f, -0.52f, -0.70f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-50.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * 25.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * 15.0f));

                    float scale = 0.022f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        katanaMesh.emit(pose, out, palette, boostedLight);
                    });
                }
                break;
            }
            case RED_DAGGER: {
                if (inspecting) {
                    float spin = t * 360.0f;
                    poseStack.translate(side * 0.40f, -0.38f, -0.58f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-70.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * (45.0f + spin)));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * -20.0f));

                    float scale = 0.022f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        butterflyBlade.emit(pose, out, palette, boostedLight);
                        butterflyHandle1.emit(pose, out, palette, boostedLight);
                    });
                } else {
                    poseStack.translate(side * 0.40f, -0.40f, -0.60f);
                    poseStack.mulPose(Axis.XP.rotationDegrees(-70.0f));
                    poseStack.mulPose(Axis.YP.rotationDegrees(side * 45.0f));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(side * -20.0f));

                    float scale = 0.022f;
                    poseStack.scale(scale, scale, scale);

                    collector.submitCustomGeometry(poseStack, SOLID_KNIFE, (pose, out) -> {
                        butterflyBlade.emit(pose, out, palette, boostedLight);
                        butterflyHandle1.emit(pose, out, palette, boostedLight);
                    });
                }
                break;
            }
            default:
                poseStack.popPose();
                return false;
        }

        poseStack.popPose();
        return true;
    }
}
