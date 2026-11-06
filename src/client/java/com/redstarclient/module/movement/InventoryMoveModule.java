// ☭ Red Star Client — InventoryMoveModule
package com.redstarclient.module.movement;

import com.mojang.blaze3d.platform.InputConstants;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFW;

/**
 * InventoryMove (InvMove):
 * Allows walking, jumping, sprinting, sneaking, and camera rotation while any GUI
 * (Inventory, Chests, Anvils, Furnaces, ClickGUI) is open.
 */
public class InventoryMoveModule extends Module {

    private final ImBoolean allowJump   = new ImBoolean(true);
    private final ImBoolean allowSprint = new ImBoolean(true);
    private final ImBoolean allowSneak  = new ImBoolean(false);
    private final ImBoolean arrowRotate = new ImBoolean(true);
    private final ImFloat   rotateSpeed = new ImFloat(4.0f);

    public InventoryMoveModule() {
        super("InventoryMove", "Erlaubt Laufen, Springen und Umschauen bei geoeffnetem Inventar oder Kisten.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.getWindow() == null || !isEnabled()) return;

        Screen screen = mc.gui != null ? mc.gui.screen() : null;
        if (screen == null) return;

        // Never move if typing in chat or active text edit box
        if (screen instanceof ChatScreen) return;
        if (screen.getFocused() instanceof EditBox || screen.getFocused() instanceof MultiLineEditBox) return;

        updateKey(mc, mc.options.keyUp);
        updateKey(mc, mc.options.keyDown);
        updateKey(mc, mc.options.keyLeft);
        updateKey(mc, mc.options.keyRight);

        if (allowJump.get()) {
            updateKey(mc, mc.options.keyJump);
        }
        if (allowSprint.get()) {
            updateKey(mc, mc.options.keySprint);
        }
        if (allowSneak.get()) {
            updateKey(mc, mc.options.keyShift);
        }

        // Camera rotation via Arrow keys
        if (arrowRotate.get()) {
            float speed = rotateSpeed.get();
            if (isKeyDown(mc, GLFW.GLFW_KEY_LEFT)) {
                mc.player.setYRot(mc.player.getYRot() - speed);
            }
            if (isKeyDown(mc, GLFW.GLFW_KEY_RIGHT)) {
                mc.player.setYRot(mc.player.getYRot() + speed);
            }
            if (isKeyDown(mc, GLFW.GLFW_KEY_UP)) {
                mc.player.setXRot(Math.max(-90f, mc.player.getXRot() - speed * 0.75f));
            }
            if (isKeyDown(mc, GLFW.GLFW_KEY_DOWN)) {
                mc.player.setXRot(Math.min(90f, mc.player.getXRot() + speed * 0.75f));
            }
        }
    }

    private void updateKey(Minecraft mc, KeyMapping mapping) {
        if (mapping == null || mapping.key == null) return;
        int code = mapping.key.getValue();
        if (code <= 0) return;
        mapping.setDown(InputConstants.isKeyDown(mc.getWindow(), code));
    }

    private boolean isKeyDown(Minecraft mc, int code) {
        if (code <= 0) return false;
        return InputConstants.isKeyDown(mc.getWindow(), code);
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Springen erlauben (Leertaste)##im", allowJump);
        ImGui.checkbox("Sprinten erlauben##im", allowSprint);
        ImGui.checkbox("Sneaken erlauben##im", allowSneak);
        ImGui.checkbox("Kamera-Drehung mit Pfeiltasten##im", arrowRotate);
        if (arrowRotate.get()) {
            ImGui.sliderFloat("Drehgeschwindigkeit##im", rotateSpeed.getData(), 1.0f, 10.0f, "%.1f");
        }
        ImGui.spacing();
        ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "Aktiv in Inventar, Kisten, Amboss, Werkbank & ClickGUI.");
        ImGui.textDisabled("Deaktiviert sich automatisch bei Chateingaben und Textfeldern.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("allowJump", allowJump.get());
        cfg.putSetting("allowSprint", allowSprint.get());
        cfg.putSetting("allowSneak", allowSneak.get());
        cfg.putSetting("arrowRotate", arrowRotate.get());
        cfg.putSetting("rotateSpeed", rotateSpeed.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        allowJump.set(cfg.getSetting("allowJump", allowJump.get()));
        allowSprint.set(cfg.getSetting("allowSprint", allowSprint.get()));
        allowSneak.set(cfg.getSetting("allowSneak", allowSneak.get()));
        arrowRotate.set(cfg.getSetting("arrowRotate", arrowRotate.get()));
        rotateSpeed.set(cfg.getSetting("rotateSpeed", rotateSpeed.get()));
    }
}
