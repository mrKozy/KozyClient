// ☭ Red Star Client — AntiBlindModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.ArrayList;
import java.util.List;

/**
 * AntiBlind: Suppresses Darkness, Blindness, and Nausea screen effects for 100% clear sight.
 */
public class AntiBlindModule extends Module {

    private final ImBoolean removeDarkness  = new ImBoolean(true);
    private final ImBoolean removeBlindness = new ImBoolean(true);
    private final ImBoolean removeNausea    = new ImBoolean(true);

    public AntiBlindModule() {
        super("AntiBlind", "Entfernt Dunkelheit (Warden), Blindheit und Uebelkeit fuer dauerhaft klare Sicht.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        List<Holder<MobEffect>> toRemove = new ArrayList<>();
        for (MobEffectInstance inst : mc.player.getActiveEffects()) {
            Holder<MobEffect> effectHolder = inst.getEffect();
            String name = effectHolder.getRegisteredName().toLowerCase();

            if (removeDarkness.get() && name.contains("darkness")) {
                toRemove.add(effectHolder);
            } else if (removeBlindness.get() && name.contains("blindness")) {
                toRemove.add(effectHolder);
            } else if (removeNausea.get() && (name.contains("nausea") || name.contains("confusion"))) {
                toRemove.add(effectHolder);
            }
        }

        for (Holder<MobEffect> eff : toRemove) {
            mc.player.removeEffect(eff);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Dunkelheit entfernen (Darkness)##ab", removeDarkness);
        ImGui.checkbox("Blindheit entfernen (Blindness)##ab", removeBlindness);
        ImGui.checkbox("Uebelkeit / Bildverzerrung entfernen (Nausea)##ab", removeNausea);
        ImGui.spacing();
        ImGui.textDisabled("Gewaehrleistet uneingeschraenkte Sicht auch bei Warden und Traenken.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("removeDarkness", removeDarkness.get());
        cfg.putSetting("removeBlindness", removeBlindness.get());
        cfg.putSetting("removeNausea", removeNausea.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        removeDarkness.set(cfg.getSetting("removeDarkness", removeDarkness.get()));
        removeBlindness.set(cfg.getSetting("removeBlindness", removeBlindness.get()));
        removeNausea.set(cfg.getSetting("removeNausea", removeNausea.get()));
    }
}
