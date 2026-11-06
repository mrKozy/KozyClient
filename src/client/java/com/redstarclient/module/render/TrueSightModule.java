// ☭ Red Star Client — TrueSightModule (from Wurst 7)
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * TrueSight (from Wurst 7):
 * Renders invisible entities (players with invisibility potions, mobs, armor stands)
 * fully visible to the client.
 */
public class TrueSightModule extends Module {

    private final ImBoolean players = new ImBoolean(true);
    private final ImBoolean mobs    = new ImBoolean(true);

    public TrueSightModule() {
        super("TrueSight", "Macht unsichtbare Spieler und Mobs (z.B. durch Unsichtbarkeitstrank) sichtbar (Wurst 7).", ModuleCategory.RENDER, -1);
    }

    public boolean includesPlayers() {
        return players.get();
    }

    public boolean includesMobs() {
        return mobs.get();
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Unsichtbare Spieler sichtbar machen##truesight", players);
        ImGui.checkbox("Unsichtbare Mobs & Entities sichtbar machen##truesight", mobs);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Deckt Spieler auf, die sich mit Unsichtbarkeitstraenken anschleichen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("players", players.get());
        cfg.putSetting("mobs", mobs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        players.set(cfg.getSetting("players", players.get()));
        mobs.set(cfg.getSetting("mobs", mobs.get()));
    }
}
