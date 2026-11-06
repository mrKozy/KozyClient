// ☭ Red Star Client — TimeChangerModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;

/**
 * Locks the client-side game time to Day, Sunset, Night, or a custom time.
 */
public class TimeChangerModule extends Module {

    private static final String[] PRESETS = { "Day (1000)", "Noon (6000)", "Sunset (12000)", "Night (18000)", "Custom" };
    private final ImInt preset     = new ImInt(0);
    private final ImInt customTime = new ImInt(1000);

    public TimeChangerModule() {
        super("TimeChanger", "Locks client-side world time to Day, Sunset, or Night.", ModuleCategory.RENDER, -1);
    }

    public long getOverrideTime() {
        return switch (preset.get()) {
            case 0 -> 1000L;
            case 1 -> 6000L;
            case 2 -> 12000L;
            case 3 -> 18000L;
            default -> (long) customTime.get();
        };
    }

    @Override
    public void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            mc.level.setTimeFromServer(getOverrideTime());
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        // Handled smoothly via DimensionTypeMixin and ClientPlayNetworkHandlerMixin
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Preset Time##tc", preset, PRESETS);
        if (preset.get() == 4) {
            ImGui.sliderInt("Custom Ticks##tc", customTime.getData(), 0, 24000);
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("preset", preset.get());
        cfg.putSetting("customTime", customTime.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        preset.set(cfg.getSetting("preset", preset.get()));
        customTime.set(cfg.getSetting("customTime", customTime.get()));
    }
}
