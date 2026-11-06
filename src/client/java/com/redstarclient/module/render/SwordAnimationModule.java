// ☭ Red Star Client — SwordAnimationModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;

/**
 * Custom 1.7.10 / 1.8 PvP sword animation module.
 * Restores classic right-click sword blocking, block-hit animation,
 * removes the 1.9+ attack re-equip dip, and provides hand position customization.
 */
public class SwordAnimationModule extends Module {

    private final ImBoolean blockHit       = new ImBoolean(true);
    private final ImBoolean oldSwing       = new ImBoolean(true);
    private final ImBoolean preventReEquip = new ImBoolean(true);
    private float scale                    = 1.0f;
    private float posX                     = 0.0f;
    private float posY                     = 0.0f;
    private float posZ                     = 0.0f;

    public SwordAnimationModule() {
        super("SwordAnimation", "1.7 / 1.8 PvP sword blocking, block-hit animation & custom hand positions.", ModuleCategory.RENDER, -1);
        setEnabled(true);
    }

    public boolean isBlockHit() {
        return isEnabled() && blockHit.get();
    }

    public boolean isOldSwing() {
        return isEnabled() && oldSwing.get();
    }

    public boolean isPreventReEquip() {
        return isEnabled() && preventReEquip.get();
    }

    public float getScale() {
        return scale;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getPosZ() {
        return posZ;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "PvP Sword Animations (1.7 / 1.8 Style)");
        ImGui.separator();

        ImGui.checkbox("1.7 Sword Block & Block-Hit##sa", blockHit);
        ImGui.checkbox("1.7 Smooth Swing Motion##sa", oldSwing);
        ImGui.checkbox("Prevent Re-Equip Dip on Attack##sa", preventReEquip);

        ImGui.separator();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "Sword Transform & Offsets");

        float[] sc = { scale };
        if (ImGui.sliderFloat("Item Scale##sa", sc, 0.5f, 1.5f)) {
            scale = sc[0];
        }

        float[] px = { posX };
        if (ImGui.sliderFloat("Offset X##sa", px, -0.5f, 0.5f)) {
            posX = px[0];
        }

        float[] py = { posY };
        if (ImGui.sliderFloat("Offset Y##sa", py, -0.5f, 0.5f)) {
            posY = py[0];
        }

        float[] pz = { posZ };
        if (ImGui.sliderFloat("Offset Z##sa", pz, -0.5f, 0.5f)) {
            posZ = pz[0];
        }

        if (ImGui.button("Reset Offsets##sa")) {
            scale = 1.0f;
            posX = 0.0f;
            posY = 0.0f;
            posZ = 0.0f;
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("blockHit", blockHit.get());
        cfg.putSetting("oldSwing", oldSwing.get());
        cfg.putSetting("preventReEquip", preventReEquip.get());
        cfg.putSetting("scale", (double) scale);
        cfg.putSetting("posX", (double) posX);
        cfg.putSetting("posY", (double) posY);
        cfg.putSetting("posZ", (double) posZ);
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        blockHit.set(cfg.getSetting("blockHit", blockHit.get()));
        oldSwing.set(cfg.getSetting("oldSwing", oldSwing.get()));
        preventReEquip.set(cfg.getSetting("preventReEquip", preventReEquip.get()));
        scale = cfg.getSetting("scale", (double) scale).floatValue();
        posX = cfg.getSetting("posX", (double) posX).floatValue();
        posY = cfg.getSetting("posY", (double) posY).floatValue();
        posZ = cfg.getSetting("posZ", (double) posZ).floatValue();
    }
}
