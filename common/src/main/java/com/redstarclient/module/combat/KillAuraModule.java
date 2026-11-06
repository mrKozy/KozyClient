// ☭ Red Star Client — KillAuraModule
package com.redstarclient.module.combat;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.PlayerUtils;
import com.redstarclient.util.TimerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.lwjgl.glfw.GLFW;

/** Automatically attacks entities within configurable range at a set CPS. */
public class KillAuraModule extends Module {

    private final ImFloat range          = new ImFloat(3.0f);
    private final ImInt   maxCPS         = new ImInt(12);
    private final ImBoolean playersOnly  = new ImBoolean(false);
    private final ImBoolean throughWalls = new ImBoolean(false);

    private final TimerUtils cpsTimer = new TimerUtils();

    public KillAuraModule() {
        super("KillAura", "Automatically attacks nearby entities.", ModuleCategory.COMBAT, GLFW.GLFW_KEY_R);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;

        AntiKickModule antiKick = RedStarClient.MODULES.get(AntiKickModule.class);
        if (antiKick != null && antiKick.isSafetyPaused()) return;

        float effectiveRange = range.get();
        if (antiKick != null && antiKick.isClampReach()) {
            effectiveRange = Math.min(effectiveRange, antiKick.getSafeMaxReach());
        }

        boolean canAttackThroughWalls = throughWalls.get();
        if (antiKick != null && antiKick.isGrimMode()) {
            canAttackThroughWalls = false; // Never attack through walls on Grim
        }

        Entity target = PlayerUtils.getClosestEntity(mc.player, effectiveRange, playersOnly.get());
        if (target == null) return;
        if (!(target instanceof LivingEntity le) || le.isDeadOrDying()) return;
        if (!canAttackThroughWalls && !PlayerUtils.canSeeEntity(mc.player, target)) return;

        int effectiveCPS = maxCPS.get();
        if (antiKick != null && antiKick.isGrimMode()) {
            effectiveCPS = Math.min(effectiveCPS, 14); // Avoid timer flags
        }

        if (cpsTimer.canAct(effectiveCPS)) {
            mc.gameMode.attack(mc.player, target);
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Range##ka",   range.getData(),   1.0f, 6.0f, "%.2f");
        ImGui.sliderInt  ("Max CPS##ka", maxCPS.getData(),  1,    20);
        ImGui.checkbox("Players Only##ka",   playersOnly);
        ImGui.checkbox("Through Walls##ka",  throughWalls);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("maxCPS", maxCPS.get());
        cfg.putSetting("playersOnly", playersOnly.get());
        cfg.putSetting("throughWalls", throughWalls.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        maxCPS.set(cfg.getSetting("maxCPS", maxCPS.get()));
        playersOnly.set(cfg.getSetting("playersOnly", playersOnly.get()));
        throughWalls.set(cfg.getSetting("throughWalls", throughWalls.get()));
    }
}
