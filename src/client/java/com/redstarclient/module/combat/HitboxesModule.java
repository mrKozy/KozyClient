// ☭ Red Star Client — HitboxesModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;

/**
 * Expands entity hitboxes client-side for effortless crosshair alignment
 * on players, crystals, and mobs.
 */
public class HitboxesModule extends Module {

    private final ImFloat   expandSize    = new ImFloat(0.35f);
    private final ImBoolean expandPlayers = new ImBoolean(true);
    private final ImBoolean expandCrystals= new ImBoolean(true);

    public HitboxesModule() {
        super("Hitboxes", "Vergroessert die Trefferboxen von Spielern und Crystals.", ModuleCategory.COMBAT, -1);
    }

    public float getExpansion(Entity entity) {
        if (!isEnabled()) return 0.0f;
        if (entity instanceof Player && expandPlayers.get()) {
            return expandSize.get();
        }
        if (entity instanceof EndCrystal && expandCrystals.get()) {
            return expandSize.get();
        }
        return 0.0f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Erweiterung (Bloecke)##hb", expandSize.getData(), 0.05f, 1.5f, "+%.2fm");
        ImGui.checkbox("Spieler vergroessern##hb", expandPlayers);
        ImGui.checkbox("End Crystals vergroessern##hb", expandCrystals);
        ImGui.spacing();
        ImGui.textDisabled("Macht Treffer im Nahkampf und Crystal-PvP kinderleicht.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("expandSize", expandSize.get());
        cfg.putSetting("expandPlayers", expandPlayers.get());
        cfg.putSetting("expandCrystals", expandCrystals.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        expandSize.set(cfg.getSetting("expandSize", expandSize.get()));
        expandPlayers.set(cfg.getSetting("expandPlayers", expandPlayers.get()));
        expandCrystals.set(cfg.getSetting("expandCrystals", expandCrystals.get()));
    }
}
