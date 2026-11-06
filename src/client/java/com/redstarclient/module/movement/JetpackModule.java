// ☭ Red Star Client — JetpackModule (from Wurst 7)
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

/**
 * Jetpack (from Wurst 7):
 * Holding the jump key (Space) gives constant vertical thrust like wearing a jetpack.
 */
public class JetpackModule extends Module {

    private final ImFloat   thrust   = new ImFloat(0.42f);
    private final ImBoolean particles = new ImBoolean(true);

    public JetpackModule() {
        super("Jetpack", "Halte die Leertaste gedrueckt, um wie mit einem Duesenrucksack aufzusteigen (Wurst 7).", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        if (mc.options.keyJump.isDown()) {
            Vec3 vel = mc.player.getDeltaMovement();
            mc.player.setDeltaMovement(new Vec3(vel.x, thrust.get(), vel.z));

            if (particles.get() && mc.level.getRandom().nextBoolean()) {
                mc.level.addParticle(
                        ParticleTypes.FLAME,
                        mc.player.getX() + (mc.level.getRandom().nextDouble() - 0.5) * 0.2,
                        mc.player.getY(),
                        mc.player.getZ() + (mc.level.getRandom().nextDouble() - 0.5) * 0.2,
                        0.0, -0.2, 0.0
                );
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Schubstaerke (Thrust)##jetpack", thrust.getData(), 0.1f, 1.5f, "%.2f");
        ImGui.checkbox("Flammen-Partikel anzeigen##jetpack", particles);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Halte Leertaste gedrueckt, um senkrecht in den Himmel zu duesen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("thrust", thrust.get());
        cfg.putSetting("particles", particles.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        thrust.set(cfg.getSetting("thrust", thrust.get()));
        particles.set(cfg.getSetting("particles", particles.get()));
    }
}
