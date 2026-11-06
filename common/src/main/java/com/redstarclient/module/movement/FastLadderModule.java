// ☭ Red Star Client — FastLadderModule (from Wurst 7)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * FastLadder (from Wurst 7):
 * Accelerates ladder and vine climbing speed.
 */
public class FastLadderModule extends Module {

    private final ImFloat climbSpeed = new ImFloat(0.288f);

    public FastLadderModule() {
        super("FastLadder", "Ermoeglicht blitzschnelles Erklimmen von Leitern und Ranken (Wurst 7).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        if (mc.player.onClimbable()) {
            Vec3 motion = mc.player.getDeltaMovement();
            if (mc.options.keyUp.isDown() || mc.player.horizontalCollision) {
                mc.player.setDeltaMovement(motion.x, climbSpeed.get(), motion.z);
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Kletter-Geschwindigkeit##flad", climbSpeed.getData(), 0.15f, 0.60f, "%.3f");
        ImGui.spacing();
        ImGui.textDisabled("Standard-Geschwindigkeit ist ~0.15. 0.288 ist verlaesslich und schnell.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("climbSpeed", climbSpeed.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        climbSpeed.set(cfg.getSetting("climbSpeed", climbSpeed.get()));
    }
}
