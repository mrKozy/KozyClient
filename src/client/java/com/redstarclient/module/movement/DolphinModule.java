// ☭ Red Star Client — DolphinModule (from Wurst 7)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Dolphin (from Wurst 7):
 * Automatically propels the player upwards when swimming in water,
 * hopping along the surface like a dolphin for fast water travel.
 */
public class DolphinModule extends Module {

    private final ImFloat bouncePower = new ImFloat(0.04f);

    public DolphinModule() {
        super("Dolphin", "Laesst dich wie ein Delphin durchs Wasser huepfen fuer extremes Reisetempo (Wurst 7).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        if (mc.player.isInWater()) {
            Vec3 vel = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(vel.x, vel.y + bouncePower.get(), vel.z);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Auftriebskraft##dolphin", bouncePower.getData(), 0.01f, 0.20f, "%.3f");
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Springe blitzschnell ueber Ozeane und Fluesse.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("bouncePower", bouncePower.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        bouncePower.set(cfg.getSetting("bouncePower", bouncePower.get()));
    }
}
