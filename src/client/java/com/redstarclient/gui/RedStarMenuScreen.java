// ☭ Red Star Client — RedStarMenuScreen
package com.redstarclient.gui;

import com.redstarclient.RedStarClient;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        // Subtle dark tinted overlay behind ImGui
        graphics.fill(0, 0, width, height, 0x44000000);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || 
            keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT || 
            keyCode == GLFW.GLFW_KEY_RIGHT_CONTROL || 
            keyCode == GLFW.GLFW_KEY_U || 
            keyCode == GLFW.GLFW_KEY_INSERT ||
            keyCode == RedStarClient.menuKey) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        if (RedStarClient.GUI != null) {
            RedStarClient.GUI.setMenuOpen(false);
        }
        super.onClose();
    }
}
