// ☭ Red Star Client — AntiAFKModule
package com.redstarclient.module.player;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;

import java.util.Random;

/** Prevents AFK kicks by performing small random movements at intervals. */
public class AntiAFKModule extends Module {

    private final ImInt     intervalSeconds = new ImInt(60);
    private final ImBoolean randomize       = new ImBoolean(true);
    private final ImBoolean swing           = new ImBoolean(true);
    private final ImBoolean jump            = new ImBoolean(false);

    private long  lastAction = 0L;
    private final Random rng = new Random();

    public AntiAFKModule() {
        super("AntiAFK", "Prevents AFK kicks with random micro-movements.", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAction < (long) intervalSeconds.get() * 1000L) return;
        lastAction = now;

        // Small random yaw rotation
        float delta = randomize.get() ? (rng.nextFloat() * 10f - 5f) : 5f;
        mc.player.setYRot(mc.player.getYRot() + delta);

        if (swing.get()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        if (jump.get() && mc.player.onGround()) {
            mc.player.jumpFromGround();
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Interval (sec)##aafk", intervalSeconds.getData(), 10, 300);
        ImGui.checkbox("Randomize##aafk",  randomize);
        ImGui.checkbox("Swing Hand##aafk", swing);
        ImGui.checkbox("Jump##aafk",       jump);
    }
}
