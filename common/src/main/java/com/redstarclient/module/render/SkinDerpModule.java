// ☭ Red Star Client — SkinDerpModule (from Wurst 7)
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.Random;

/**
 * SkinDerp (from Wurst 7):
 * Rapidly toggles all skin layers (jacket, cape, sleeves, pants, hat)
 * to create a crazy flashing disco skin effect visible to all players.
 */
public class SkinDerpModule extends Module {

    private final ImInt speedTicks = new ImInt(2);
    private final Random random = new Random();
    private int tickCount = 0;

    public SkinDerpModule() {
        super("SkinDerp", "Schaltet alle Skin-Schichten (Jacke, Hut, Aermel, Hose) blitzschnell um (Wurst 7 Disco-Skin).", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onDisable() {
        if (mc != null && mc.options != null) {
            // Restore all skin layers to visible
            for (PlayerModelPart part : PlayerModelPart.values()) {
                mc.options.setModelPart(part, true);
            }
        }
        tickCount = 0;
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.options == null) return;

        tickCount++;
        if (tickCount >= Math.max(1, speedTicks.get())) {
            tickCount = 0;
            for (PlayerModelPart part : PlayerModelPart.values()) {
                mc.options.setModelPart(part, random.nextBoolean());
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Geschwindigkeit (Ticks Intervall)##skinderp", speedTicks.getData(), 1, 10);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Verrueckter Disco-Effekt fuer Jacke, Aermel & Hosen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("speedTicks", speedTicks.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        speedTicks.set(cfg.getSetting("speedTicks", speedTicks.get()));
    }
}
