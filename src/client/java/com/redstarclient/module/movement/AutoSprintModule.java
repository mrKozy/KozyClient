// ☭ Red Star Client — AutoSprintModule
package com.redstarclient.module.movement;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;

/** Automatically maintains sprint status while walking forward. */
public class AutoSprintModule extends Module {

    private final ImBoolean omniSprint = new ImBoolean(false);

    public AutoSprintModule() {
        super("AutoSprint", "Automatically sprints whenever moving.", ModuleCategory.MOVEMENT, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        boolean moving = omniSprint.get() ?
                (mc.options.keyUp.isDown() || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown()) :
                mc.options.keyUp.isDown();

        if (moving && !mc.player.isShiftKeyDown() && mc.player.getFoodData().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Omni-Directional##as", omniSprint);
        ImGui.textDisabled("Automatically triggers sprint without double-tapping W.");
    }
}
