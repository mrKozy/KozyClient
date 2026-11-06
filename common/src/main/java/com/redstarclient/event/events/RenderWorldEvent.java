// ☭ Red Star Client — RenderWorldEvent
package com.redstarclient.event.events;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;

/**
 * Fired during the 3D world render pass (after entities, before gizmos).
 * Carries the PoseStack + Camera needed for proper world-space rendering.
 */
public class RenderWorldEvent {

    public final PoseStack poseStack;
    public final Camera    camera;
    public final float     tickDelta;

    public RenderWorldEvent(PoseStack poseStack, Camera camera, float tickDelta) {
        this.poseStack = poseStack;
        this.camera    = camera;
        this.tickDelta = tickDelta;
    }
}
