package com.redstarclient.cosmetics;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.RedStarClient;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;

public final class CharacterLayer extends RenderLayer<AvatarRenderState, PlayerModel> {

    public CharacterLayer(RenderLayerParent<AvatarRenderState, PlayerModel> parent) {
        super(parent);
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, AvatarRenderState state, float yRot, float xRot) {
        CosmeticsModule mod = RedStarClient.MODULES != null ? RedStarClient.MODULES.get(CosmeticsModule.class) : null;
        if (mod == null || !mod.isEnabled() || mod.getCharacterSkin() == CosmeticsModule.CharacterSkin.NONE) {
            return;
        }

        WornCharacter worn = CharacterCache.getWorn(mod.getCharacterSkin());
        if (worn != null) {
            CharacterRenderer.submit(worn, this.getParentModel(), poseStack, collector, light, state);
        }
    }
}
