// ☭ Red Star Client — GlideModule (from Wurst 7)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Glide (from Wurst 7):
 * Drastically reduces falling speed, allowing the player to glide slowly
 * through the air across large distances.
 */
public class GlideModule extends Module {

    private final ImFloat fallSpeed = new ImFloat(0.12f);
    private final ImFloat moveSpeed = new ImFloat(1.15f);

    public GlideModule() {
        super("Glide", "Laesst dich wie mit einem Gleitschirm langsam durch die Luft segeln (Wurst 7).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        if (!mc.player.onGround() && !mc.player.isInWater() && !mc.player.onClimbable()) {
            Vec3 vel = mc.player.getDeltaMovement();

            if (vel.y < 0) {
                double newY = -fallSpeed.get();
                double newX = vel.x * moveSpeed.get();
                double newZ = vel.z * moveSpeed.get();

                mc.player.setDeltaMovement(newX, newY, newZ);
                mc.player.fallDistance = 0f;
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Fall-Geschwindigkeit##gld", fallSpeed.getData(), 0.02f, 0.40f, "%.3f");
        ImGui.sliderFloat("Gleit-Weite (Horizontal)##gld", moveSpeed.getData(), 1.0f, 1.50f, "%.2fx");
        ImGui.spacing();
        ImGui.textDisabled("Verhindert Fallschaden und ermoeglicht weite Spruenge ueber Schluchten.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("fallSpeed", fallSpeed.get());
        cfg.putSetting("moveSpeed", moveSpeed.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        fallSpeed.set(cfg.getSetting("fallSpeed", fallSpeed.get()));
        moveSpeed.set(cfg.getSetting("moveSpeed", moveSpeed.get()));
    }
}
