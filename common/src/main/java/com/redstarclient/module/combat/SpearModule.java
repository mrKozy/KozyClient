// ☭ Red Star Client — SpearModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

/**
 * Spear & Piercing PvP:
 * - Instant Lunge: Boosts piercing thrust momentum
 * - Extended Reach: Maximizes hit reach for polearms and tridents
 * - Spear D-Tap: Rapid lateral double-tap dash around enemy shields
 */
public class SpearModule extends Module {

    private final ImFloat   lungeMultiplier = new ImFloat(1.35f);
    private final ImFloat   spearReach      = new ImFloat(4.5f);
    private final ImBoolean instantLunge    = new ImBoolean(true);
    private final ImBoolean spearDTap       = new ImBoolean(true);

    public SpearModule() {
        super("SpearPvP", "Verstaerkt Speer- & Dreizack-Angriffe mit Instant Lunge und erweitertem Reach.", ModuleCategory.CART, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        // Instant lunge boost when thrusting forward
        if (instantLunge.get() && mc.player.isUsingItem()) {
            if (mc.player.getMainHandItem().getItem().getDescriptionId().contains("spear") ||
                mc.player.getMainHandItem().getItem().getDescriptionId().contains("trident")) {
                Vec3 look = mc.player.getLookAngle();
                mc.player.setDeltaMovement(
                        look.x * 0.25 * lungeMultiplier.get(),
                        mc.player.getDeltaMovement().y,
                        look.z * 0.25 * lungeMultiplier.get()
                );
            }
        }
    }

    public float getSpearReach() {
        return isEnabled() ? spearReach.get() : 3.0f;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Lunge Multiplikator##sp", lungeMultiplier.getData(), 1.0f, 2.5f, "%.2fx");
        ImGui.sliderFloat("Speer Reichweite##sp", spearReach.getData(), 3.0f, 6.0f, "%.1fm");
        ImGui.checkbox("Instant Lunge Ausstoesse##sp", instantLunge);
        ImGui.checkbox("Spear D-Tap (Schildumgehung)##sp", spearDTap);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("lungeMultiplier", lungeMultiplier.get());
        cfg.putSetting("spearReach", spearReach.get());
        cfg.putSetting("instantLunge", instantLunge.get());
        cfg.putSetting("spearDTap", spearDTap.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        lungeMultiplier.set(cfg.getSetting("lungeMultiplier", lungeMultiplier.get()));
        spearReach.set(cfg.getSetting("spearReach", spearReach.get()));
        instantLunge.set(cfg.getSetting("instantLunge", instantLunge.get()));
        spearDTap.set(cfg.getSetting("spearDTap", spearDTap.get()));
    }
}
