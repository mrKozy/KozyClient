// ☭ Red Star Client — TwerkModule (from Wurst 7 MileyCyrus)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;

/**
 * Twerk / MileyCyrus (from Wurst 7):
 * Rapidly crouches and stands up automatically to twerk / spam crouch.
 */
public class TwerkModule extends Module {

    private final ImInt     speedTicks   = new ImInt(2);
    private final ImBoolean onlyOnGround = new ImBoolean(true);

    private int tickCounter = 0;
    private boolean sneaking = false;

    public TwerkModule() {
        super("Twerk", "Duckt sich automatisch in schneller Frequenz (MileyCyrus / Twerk aus Wurst 7).", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.options != null) {
            mc.options.keyShift.setDown(false);
        }
        sneaking = false;
        tickCounter = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.options == null) return;

        if (onlyOnGround.get() && !mc.player.onGround()) {
            if (sneaking) {
                mc.options.keyShift.setDown(false);
                sneaking = false;
            }
            return;
        }

        tickCounter++;
        if (tickCounter >= Math.max(1, speedTicks.get())) {
            tickCounter = 0;
            sneaking = !sneaking;
            mc.options.keyShift.setDown(sneaking);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Geschwindigkeit (Ticks Intervall)##twerk", speedTicks.getData(), 1, 10);
        ImGui.checkbox("Nur auf dem Boden twerken##twerk", onlyOnGround);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Schnelles Ducken fuer Feiern, Spass und Begruessungen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("speedTicks", speedTicks.get());
        cfg.putSetting("onlyOnGround", onlyOnGround.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        speedTicks.set(cfg.getSetting("speedTicks", speedTicks.get()));
        onlyOnGround.set(cfg.getSetting("onlyOnGround", onlyOnGround.get()));
    }
}
