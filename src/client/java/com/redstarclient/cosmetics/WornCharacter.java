package com.redstarclient.cosmetics;

import com.redstarclient.cosmetics.kit.CharacterModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

public final class WornCharacter {
    public final CharacterModel model;
    public final RenderType solid;
    public final RenderType decal;
    public final RenderType ghost;

    public WornCharacter(CharacterModel model, Identifier texture) {
        this.model = model;
        this.solid = RenderTypes.entityCutout(texture);
        this.decal = RenderTypes.entityCutoutZOffset(texture);
        this.ghost = RenderTypes.entityTranslucent(texture);
    }
}
