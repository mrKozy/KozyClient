// ☭ Red Star Client — AutoGGModule
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;

/**
 * Automatically sends "gg" (good game) to chat when a minigame match finishes.
 */
public class AutoGGModule extends Module {

    private final ImString customMessage = new ImString("gg", 32);
    private final ImInt    delayMs       = new ImInt(1000);

    private long lastSentTime = 0L;

    public AutoGGModule() {
        super("AutoGG", "Automatically says 'gg' in chat when a match concludes.", ModuleCategory.PLAYER, -1);
    }

    public void onChatMessage(String raw) {
        if (!isEnabled()) return;
        if (System.currentTimeMillis() - lastSentTime < 8000L) return; // Prevent spam

        String lower = raw.toLowerCase();
        if (lower.contains("winner") || lower.contains("victory") || lower.contains("game over") ||
            lower.contains("1st killer") || lower.contains("gewonnen") || lower.contains("sieger")) {

            lastSentTime = System.currentTimeMillis();
            new Thread(() -> {
                try {
                    Thread.sleep(delayMs.get());
                    Minecraft mc = Minecraft.getInstance();
                    if (mc.getConnection() != null && mc.player != null) {
                        mc.getConnection().sendChat(customMessage.get());
                    }
                } catch (InterruptedException ignored) {}
            }, "RedStar-AutoGG").start();
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.inputText("GG Message##agg", customMessage);
        ImGui.sliderInt("Delay (ms)##agg", delayMs.getData(), 200, 3000);
        ImGui.textDisabled("Triggers automatically on BedWars/SkyWars game over screens.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("message", customMessage.get());
        cfg.putSetting("delayMs", delayMs.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        customMessage.set(cfg.getSetting("message", customMessage.get()));
        delayMs.set(cfg.getSetting("delayMs", delayMs.get()));
    }
}
