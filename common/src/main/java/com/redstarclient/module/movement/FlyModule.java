// ☭ Red Star Client — FlyModule
package com.redstarclient.module.movement;

import com.redstarclient.RedStarClient;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.module.combat.AntiKickModule;
import imgui.ImGui;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

/** Creative-style flight in survival mode. Respects AntiKick setback protection. */
public class FlyModule extends Module {

    private final ImFloat flySpeed = new ImFloat(0.05f);

    public FlyModule() {
        super("Fly", "Allows creative-style flying in survival mode.", ModuleCategory.MOVEMENT, GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        AntiKickModule ak = RedStarClient.MODULES.get(AntiKickModule.class);
        if (ak != null && ak.isSafetyPaused()) {
            // Temporarily disable flight state during server setback recovery
            mc.player.getAbilities().flying = false;
            return;
        }

        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlyingSpeed(flySpeed.get());
    }

    @Override
    public void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().flying = false;
        }
        mc.player.getAbilities().setFlyingSpeed(0.05f);
        mc.player.onUpdateAbilities();
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Fly Speed##fly", flySpeed.getData(), 0.01f, 0.20f);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("flySpeed", flySpeed.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        flySpeed.set(cfg.getSetting("flySpeed", flySpeed.get()));
    }
}
