package com.redstarclient.cosmetics;

import com.redstarclient.cosmetics.kit.Bone;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;

public final class CharacterJoints {

    private CharacterJoints() {}

    public static float angle(AvatarRenderState state, Bone bone, float swing) {
        float fade = !state.isFallFlying && !state.isVisuallySwimming ? 1.0F - state.swimAmount * 0.8F : 0.0F;
        if (fade <= 0.0F) {
            return 0.0F;
        } else if (bone.leg()) {
            return knee(state, bone) * fade;
        } else {
            return bone.arm() ? -elbow(state, bone, swing) * fade : 0.0F;
        }
    }

    private static float knee(AvatarRenderState state, Bone bone) {
        if (state.isPassenger) {
            return 0.3F;
        }
        float scale = state.scale > 0.001f ? state.scale : 1.0f;
        float speed = Math.min(1.0F, state.walkAnimationSpeed / scale);
        float phase = state.walkAnimationPos * 0.6662F + (bone == Bone.LEFT_LEG ? (float) Math.PI : 0.0F);
        float flex = speed * (0.1F + 0.85F * Math.max(0.0F, Mth.sin(phase)));
        if (state.isCrouching) {
            flex += 0.62F;
        }
        return Math.min(1.7F, flex);
    }

    private static float elbow(AvatarRenderState state, Bone bone, float swing) {
        float flex = Math.abs(swing) * 0.35F;
        if (state.isCrouching) {
            flex += 0.2F;
        }
        return Math.min(1.6F, flex);
    }
}
