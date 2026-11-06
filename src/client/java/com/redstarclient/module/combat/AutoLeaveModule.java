// ☭ Red Star Client — AutoLeaveModule (from Wurst 7)
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

/**
 * AutoLeave (from Wurst 7):
 * Automatically disconnects or executes a rescue command when player health
 * falls below a configured threshold, saving gear and preventing deaths.
 */
public class AutoLeaveModule extends Module {

    private static final String[] MODES = { "Disconnect (Sofort)", "Befehl (/spawn)", "Befehl (/hub)", "Eigener Befehl" };

    private final ImInt    mode            = new ImInt(0);
    private final ImInt    healthThreshold = new ImInt(6);
    private final ImString customCommand   = new ImString("/spawn", 128);

    public AutoLeaveModule() {
        super("AutoLeave", "Verlaesst den Server oder sendet /spawn bei kritisch niedriger Gesundheit (Wurst 7).", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) return;

        if (mc.player.getHealth() <= healthThreshold.get() && mc.player.getHealth() > 0) {
            int hp = (int) mc.player.getHealth();

            if (mode.get() == 0) {
                // Disconnect
                mc.getConnection().getConnection().disconnect(Component.literal("§c[AutoLeave] §fGesundheit auf " + hp + " HP gefallen! Verbindung sicher getrennt."));
            } else if (mode.get() == 1) {
                mc.player.connection.sendCommand("spawn");
            } else if (mode.get() == 2) {
                mc.player.connection.sendCommand("hub");
            } else {
                String cmd = customCommand.get().trim();
                if (cmd.startsWith("/")) cmd = cmd.substring(1);
                if (!cmd.isEmpty()) {
                    mc.player.connection.sendCommand(cmd);
                }
            }

            setEnabled(false);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Gesundheits-Schwelle (HP)##alv", healthThreshold.getData(), 1, 19);
        ImGui.combo("Aktion bei niedriger Gesundheit##alv", mode, MODES);
        if (mode.get() == 3) {
            ImGui.inputText("Eigener Befehl##alv", customCommand);
        }
        ImGui.spacing();
        ImGui.textDisabled("Rettet dein Inventar in brenzligen Kaempfen vor dem Tod.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("healthThreshold", healthThreshold.get());
        cfg.putSetting("mode", mode.get());
        cfg.putSetting("customCommand", customCommand.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        healthThreshold.set(cfg.getSetting("healthThreshold", healthThreshold.get()));
        mode.set(cfg.getSetting("mode", mode.get()));
        customCommand.set(cfg.getSetting("customCommand", customCommand.get()));
    }
}
