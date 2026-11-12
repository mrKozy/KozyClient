// ☭ Red Star Client — ModuleCategory
package com.redstarclient.module;

import com.redstarclient.gui.Icons;

/** Categories shown in the ImGui navigation sidebar. */
public enum ModuleCategory {
    COMBAT   ("Combat / Sword", Icons.SWORDS),
    CRYSTAL  ("Crystal PvP",    Icons.BOLT),
    MACE     ("Mace PvP",       Icons.BOLT),
    CART     ("Cart & Spear",   Icons.COMPASS),
    POT_UHC  ("Pot & UHC",      Icons.SHIELD),
    MOVEMENT ("Movement",       Icons.RUNNING),
    RENDER   ("Visuals",        Icons.EYE),
    PLAYER   ("Player & World", Icons.USER),
    INFO     ("Info HUD",       Icons.CHART),
    INVENTORY("Inventory",      Icons.BOX);

    public final String displayName;
    public final String icon;

    ModuleCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
}
