package com.redstarclient.cosmetics;

import com.redstarclient.cosmetics.designs.Aoi;
import com.redstarclient.cosmetics.designs.Luna;
import com.redstarclient.cosmetics.designs.Mika;
import com.redstarclient.cosmetics.designs.Miku;
import com.redstarclient.cosmetics.designs.Sakura;
import com.redstarclient.cosmetics.designs.Tralalero;
import com.redstarclient.cosmetics.designs.TungTungSahur;
import com.redstarclient.cosmetics.kit.CharacterBuilder;
import com.redstarclient.cosmetics.kit.CharacterDesign;
import com.redstarclient.cosmetics.kit.CharacterModel;
import com.redstarclient.cosmetics.kit.Detail;
import com.redstarclient.module.render.CosmeticsModule;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CharacterCache {

    private static final Map<CosmeticsModule.CharacterSkin, Entry> ENTRIES = new ConcurrentHashMap<>();

    private static class Entry {
        final CharacterDesign design;
        volatile CharacterModel model;
        volatile WornCharacter worn;
        volatile boolean baking;

        Entry(CharacterDesign design) {
            this.design = design;
        }
    }

    private CharacterCache() {}

    public static CharacterDesign getDesign(CosmeticsModule.CharacterSkin skin) {
        return switch (skin) {
            case TUNG_TUNG_SAHUR -> new TungTungSahur();
            case SAKURA -> new Sakura();
            case LUNA -> new Luna();
            case MIKA -> new Mika();
            case AOI -> new Aoi();
            case MIKU -> new Miku();
            case TRALALERO -> new Tralalero();
            default -> null;
        };
    }

    public static WornCharacter getWorn(CosmeticsModule.CharacterSkin skin) {
        if (skin == CosmeticsModule.CharacterSkin.NONE) return null;

        Entry entry = ENTRIES.computeIfAbsent(skin, s -> {
            CharacterDesign design = getDesign(s);
            return design != null ? new Entry(design) : null;
        });

        if (entry == null) return null;

        if (entry.worn != null) {
            return entry.worn;
        }

        if (entry.model == null) {
            if (!entry.baking) {
                entry.baking = true;
                Thread t = new Thread(() -> {
                    try {
                        CharacterModel m = CharacterBuilder.build(entry.design, Detail.MEDIUM);
                        entry.model = m;
                    } catch (Throwable t1) {
                        t1.printStackTrace();
                    } finally {
                        entry.baking = false;
                    }
                }, "Character-Baker-" + skin.name());
                t.setDaemon(true);
                t.start();
            }
            return null;
        }

        // Texture upload must happen on render thread
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getTextureManager() != null) {
            try {
                CharacterModel m = entry.model;
                Identifier texId = Identifier.fromNamespaceAndPath("redstarclient", "textures/character/" + m.id + ".png");
                CharacterTexture tex = new CharacterTexture(texId.toString(), m.atlasWidth, m.atlasHeight, m.atlasMips);
                mc.getTextureManager().register(texId, tex);
                entry.worn = new WornCharacter(m, texId);
                return entry.worn;
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        return null;
    }
}
