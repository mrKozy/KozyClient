// ☭ Red Star Client — ParticleMultiplierModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;

/**
 * Multiplies hit and critical particles when attacking enemies for satisfying combat feedback.
 */
public class ParticleMultiplierModule extends Module {

    private final ImInt multiplier = new ImInt(3);

    public ParticleMultiplierModule() {
        super("ParticleMultiplier", "Multiplies hit and critical particles for satisfying PvP feedback.", ModuleCategory.RENDER, -1);
    }

    public void onAttackEntity(Entity target) {
        if (!isEnabled()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || target == null) return;

        int count = multiplier.get();
        for (int i = 0; i < count; i++) {
            double ox = (Math.random() - 0.5) * target.getBbWidth();
            double oy = Math.random() * target.getBbHeight();
            double oz = (Math.random() - 0.5) * target.getBbWidth();
            mc.level.addParticle(ParticleTypes.CRIT,
                    target.getX() + ox, target.getY() + oy, target.getZ() + oz,
                    (Math.random() - 0.5) * 0.4, Math.random() * 0.3, (Math.random() - 0.5) * 0.4);
            mc.level.addParticle(ParticleTypes.ENCHANTED_HIT,
                    target.getX() + ox, target.getY() + oy, target.getZ() + oz,
                    (Math.random() - 0.5) * 0.3, Math.random() * 0.2, (Math.random() - 0.5) * 0.3);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Particle Multiplier##pm", multiplier.getData(), 1, 10);
        ImGui.textDisabled("Spawns extra crit and sharpness particles upon striking opponents.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("multiplier", multiplier.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        multiplier.set(cfg.getSetting("multiplier", multiplier.get()));
    }
}
