// ☭ Red Star Client — ReachModule
package com.redstarclient.module.combat;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;

/** Increases player attack and block reach distance. */
public class ReachModule extends Module {

    private final ImFloat attackReach = new ImFloat(3.8f);
    private final ImFloat buildReach  = new ImFloat(4.5f);

    public ReachModule() {
        super("Reach", "Extends attack and interaction reach distance.", ModuleCategory.COMBAT, -1);
    }

    public float getAttackReach() { return isEnabled() ? attackReach.get() : 3.0f; }
    public float getBuildReach()  { return isEnabled() ? buildReach.get() : 4.5f; }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Attack Reach##rch", attackReach.getData(), 3.0f, 6.0f);
        ImGui.sliderFloat("Block Reach##rch",  buildReach.getData(),  4.5f, 7.0f);
        ImGui.textDisabled("Default vanilla attack reach is 3.0 blocks.");
    }
}
