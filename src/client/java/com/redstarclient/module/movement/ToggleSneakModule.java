// ☭ Red Star Client — ToggleSneakModule
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;

/**
 * Keeps sneak active without needing to hold the sneak key.
 * Critical for BedWars fast bridging and preventing accidental falls.
 */
public class ToggleSneakModule extends Module {

    private final ImBoolean holdInMenus = new ImBoolean(false);

    public ToggleSneakModule() {
        super("ToggleSneak", "Maintains sneak mode automatically without holding Shift.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        if (mc.gui != null && mc.gui.screen() != null && !holdInMenus.get()) return;
        mc.options.keyShift.setDown(true);
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.options != null) {
            mc.options.keyShift.setDown(false);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Keep Sneaking in Menus##ts", holdInMenus);
        ImGui.textDisabled("Holds Shift key down continuously while toggled.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("holdInMenus", holdInMenus.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        holdInMenus.set(cfg.getSetting("holdInMenus", holdInMenus.get()));
    }
}
