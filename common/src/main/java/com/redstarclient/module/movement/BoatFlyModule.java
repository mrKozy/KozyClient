// ☭ Red Star Client — BoatFlyModule (from Wurst 7)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * BoatFly (from Wurst 7):
 * Allows flying smoothly in boats, minecarts, and other rideable vehicles.
 * Control vertical height with Space (up) and Shift (down).
 */
public class BoatFlyModule extends Module {

    private final ImFloat   speed     = new ImFloat(1.5f);
    private final ImFloat   upSpeed   = new ImFloat(0.6f);
    private final ImBoolean hover     = new ImBoolean(true);

    public BoatFlyModule() {
        super("BoatFly", "Ermoeglicht freies Fliegen mit Booten und Reittieren (Wurst 7 Klassiker).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        double forward = 0;
        double strafe = 0;

        if (mc.options.keyUp.isDown()) forward += 1.0;
        if (mc.options.keyDown.isDown()) forward -= 1.0;
        if (mc.options.keyLeft.isDown()) strafe += 1.0;
        if (mc.options.keyRight.isDown()) strafe -= 1.0;

        float yaw = mc.player.getYRot();
        double rad = Math.toRadians(yaw);

        double motionX = 0;
        double motionZ = 0;

        if (forward != 0 || strafe != 0) {
            double s = speed.get();
            double sin = Math.sin(rad);
            double cos = Math.cos(rad);

            motionX = (forward * -sin + strafe * cos) * s;
            motionZ = (forward * cos + strafe * sin) * s;
        }

        double motionY = 0;
        if (mc.options.keyJump.isDown()) {
            motionY = upSpeed.get();
        } else if (mc.options.keyShift.isDown()) {
            motionY = -upSpeed.get();
        } else if (hover.get()) {
            motionY = 0.0;
        } else {
            motionY = vehicle.getDeltaMovement().y;
        }

        vehicle.setDeltaMovement(new Vec3(motionX, motionY, motionZ));
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Fluggeschwindigkeit##boatfly", speed.getData(), 0.2f, 5.0f, "%.2f");
        ImGui.sliderFloat("Steiggeschwindigkeit##boatfly", upSpeed.getData(), 0.1f, 2.0f, "%.2f");
        ImGui.checkbox("In der Luft schweben (Hover)##boatfly", hover);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Fliege mit Booten ueber die Karte. Steuerung: Leertaste = Hoch, Shift = Runter.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("speed", speed.get());
        cfg.putSetting("upSpeed", upSpeed.get());
        cfg.putSetting("hover", hover.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        speed.set(cfg.getSetting("speed", speed.get()));
        upSpeed.set(cfg.getSetting("upSpeed", upSpeed.get()));
        hover.set(cfg.getSetting("hover", hover.get()));
    }
}
