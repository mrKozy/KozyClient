// ☭ Red Star Client — TriggerBotModule
package com.redstarclient.module.combat;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.TimerUtils;
import imgui.ImGui;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/** Attacks automatically when the crosshair hovers over a valid target. */
public class TriggerBotModule extends Module {

    private final ImInt   maxCPS = new ImInt(14);
    private final ImFloat range  = new ImFloat(3.0f);

    private final TimerUtils cpsTimer = new TimerUtils();

    public TriggerBotModule() {
        super("TriggerBot", "Attacks when crosshair is on a target.", ModuleCategory.COMBAT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.hitResult == null) return;
        if (mc.hitResult.getType() != HitResult.Type.ENTITY) return;

        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafetyPaused()) return;

        float effectiveRange = range.get();
        if (antiKick != null && antiKick.isClampReach()) {
            effectiveRange = Math.min(effectiveRange, antiKick.getSafeMaxReach());
        }

        Entity target = ((EntityHitResult) mc.hitResult).getEntity();
        if (!(target instanceof LivingEntity le) || le.isDeadOrDying()) return;
        if (mc.player.distanceTo(target) > effectiveRange) return;

        int effectiveCPS = maxCPS.get();
        if (antiKick != null && antiKick.isGrimMode()) {
            effectiveCPS = Math.min(effectiveCPS, 14);
        }

        if (cpsTimer.canAct(effectiveCPS)) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt  ("Max CPS##tb", maxCPS.getData(), 1, 20);
        ImGui.sliderFloat("Range##tb",   range.getData(),  1f, 6f, "%.2f");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("maxCPS", maxCPS.get());
        cfg.putSetting("range", range.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        maxCPS.set(cfg.getSetting("maxCPS", maxCPS.get()));
        range.set(cfg.getSetting("range", range.get()));
    }
}
