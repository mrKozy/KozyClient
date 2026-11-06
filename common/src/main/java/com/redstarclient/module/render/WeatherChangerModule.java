// ☭ Red Star Client — WeatherChangerModule
package com.redstarclient.module.render;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;

/**
 * Client-side weather override: forces clear sun and disables rain particles/thunder sounds.
 */
public class WeatherChangerModule extends Module {

    private final ImBoolean clearWeather = new ImBoolean(true);

    public WeatherChangerModule() {
        super("WeatherChanger", "Forces clear sunny weather client-side, removing rain lag.", ModuleCategory.RENDER, -1);
    }

    public boolean isClearWeather() {
        return isEnabled() && clearWeather.get();
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.level != null && isEnabled()) {
            mc.level.setRainLevel(0.0f);
            mc.level.setThunderLevel(0.0f);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Force Clear Sunny Weather##wc", clearWeather);
        ImGui.textDisabled("Removes rain particles and loud storm noise.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("clearWeather", clearWeather.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        clearWeather.set(cfg.getSetting("clearWeather", clearWeather.get()));
    }
}
