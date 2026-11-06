// ☭ Red Star Client — RedStarMenuScreen
package com.redstarclient.gui;

import com.redstarclient.RedStarClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class RedStarMenuScreen extends Screen {

    public RedStarMenuScreen() {
        super(Component.literal("☭ Red Star Client"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Subtle dark tinted overlay behind ImGui
        graphics.fill(0, 0, width, height, 0x44000000);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || 
            keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || 
            keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL || 
            keyCode == GLFW.GLFW_KEY_U || 
            keyCode == GLFW.GLFW_KEY_INSERT) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (RedStarClient.GUI != null) {
            RedStarClient.GUI.setMenuOpen(false);
        }
        super.onClose();
    }
}
