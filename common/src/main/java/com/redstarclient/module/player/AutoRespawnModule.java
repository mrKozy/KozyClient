// ☭ Red Star Client — AutoRespawnModule (from Wurst 7)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;

/**
 * AutoRespawn (from Wurst 7):
 * Automatically respawns whenever you die, bypassing the death screen.
 */
public class AutoRespawnModule extends Module {

    private final ImInt delayMs = new ImInt(0);
    private long deathTime = 0L;

    public AutoRespawnModule() {
        super("AutoRespawn", "Respawnt automatisch sofort nach dem Tod, ueberspringt den Todesbildschirm (Wurst 7).", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        if (mc.player.isDeadOrDying() || mc.screen instanceof DeathScreen) {
            long now = System.currentTimeMillis();
            if (deathTime == 0L) {
                deathTime = now;
            }

            if (now - deathTime >= delayMs.get()) {
                mc.player.respawn();
                mc.setScreen(null);
                deathTime = 0L;
            }
        } else {
            deathTime = 0L;
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Respawn-Verzoegerung (ms)##arsp", delayMs.getData(), 0, 1000);
        ImGui.spacing();
        ImGui.textDisabled("Spart Zeit und bringt dich ohne Klicken direkt zurueck ins Spiel.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("delayMs", delayMs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        delayMs.set(cfg.getSetting("delayMs", delayMs.get()));
    }
}
