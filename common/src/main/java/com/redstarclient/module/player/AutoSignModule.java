// ☭ Red Star Client — AutoSignModule (from Wurst 7)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.entity.SignBlockEntity;

import java.lang.reflect.Field;

/**
 * AutoSign (from Wurst 7):
 * Automatically fills in 4 predefined lines on newly placed signs instantly.
 */
public class AutoSignModule extends Module {

    private final ImString line1 = new ImString("☭ RED STAR", 32);
    private final ImString line2 = new ImString("★ CLIENT ★", 32);
    private final ImString line3 = new ImString("Proletarier aller", 32);
    private final ImString line4 = new ImString("Laender, vereinigt euch!", 32);

    public AutoSignModule() {
        super("AutoSign", "Schreibt automatisch 4 Textzeilen auf frisch platzierte Schilder (Wurst 7).", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.getConnection() == null) return;

        if (mc.screen instanceof AbstractSignEditScreen signScreen) {
            SignBlockEntity sign = findSignEntity(signScreen);
            if (sign != null) {
                mc.getConnection().send(new ServerboundSignUpdatePacket(
                        sign.getBlockPos(),
                        sign.isFacingFrontText(mc.player),
                        line1.get(),
                        line2.get(),
                        line3.get(),
                        line4.get()
                ));
                mc.setScreen(null);
            }
        }
    }

    private SignBlockEntity findSignEntity(AbstractSignEditScreen screen) {
        try {
            Class<?> clazz = screen.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field f : clazz.getDeclaredFields()) {
                    if (SignBlockEntity.class.isAssignableFrom(f.getType())) {
                        f.setAccessible(true);
                        return (SignBlockEntity) f.get(screen);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.inputText("Zeile 1##asg1", line1);
        ImGui.inputText("Zeile 2##asg2", line2);
        ImGui.inputText("Zeile 3##asg3", line3);
        ImGui.inputText("Zeile 4##asg4", line4);
        ImGui.spacing();
        ImGui.textDisabled("Fuellt Schilder beim Aufstellen automatisch in 0 Millisekunden aus.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("line1", line1.get());
        cfg.putSetting("line2", line2.get());
        cfg.putSetting("line3", line3.get());
        cfg.putSetting("line4", line4.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        line1.set(cfg.getSetting("line1", line1.get()));
        line2.set(cfg.getSetting("line2", line2.get()));
        line3.set(cfg.getSetting("line3", line3.get()));
        line4.set(cfg.getSetting("line4", line4.get()));
    }
}
