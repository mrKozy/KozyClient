// ☭ Red Star Client — ESPModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.ESPRenderer;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Draws colored ESP boxes / glow outlines around entities through walls.
 * Includes precise filters and full color customization.
 */
public class ESPModule extends Module {

    private static final String[] MODE_NAMES = { "2D Box", "3D Box", "Glow", "Box + Glow" };

    private final ImInt modeIndex = new ImInt(1);  // default 3D Box
    private final ImBoolean players = new ImBoolean(true);
    private final ImBoolean hostileMobs = new ImBoolean(true);
    private final ImBoolean passiveMobs = new ImBoolean(false);
    private final ImBoolean items = new ImBoolean(false);

    // Advanced visual customizers
    private final ImBoolean rainbowMode = new ImBoolean(false);
    private final ImBoolean healthColorMode = new ImBoolean(false);
    private final float[] fillOpacity = { 0.20f };
    private final float[] outlineThickness = { 2.0f };

    // Configurable colors (RGBA)
    private final float[] playerColor  = { 0.92f, 0.76f, 0.13f, 1.0f }; // Soviet Gold
    private final float[] hostileColor = { 0.95f, 0.15f, 0.15f, 1.0f }; // Soviet Red
    private final float[] passiveColor = { 0.20f, 0.95f, 0.30f, 1.0f }; // Green
    private final float[] itemColor    = { 0.30f, 0.85f, 1.0f,  1.0f }; // Cyan

    public ESPModule() {
        super("ESP", "Renders entity outlines visible through walls with custom filters and colors.", ModuleCategory.RENDER, GLFW.GLFW_KEY_B);
    }

    /** Called from the world render pass — 3D boxes and glow. */
    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        ESPRenderer.ESPMode mode = ESPRenderer.ESPMode.values()[modeIndex.get()];

        for (Entity e : mc.level.entitiesForRendering()) {
            if (e == mc.player) continue;
            if (!e.isAlive() && !(e instanceof ItemEntity)) continue;

            int color = 0;
            boolean shouldRender = false;

            if (e instanceof Player) {
                if (players.get()) {
                    shouldRender = true;
                    color = RenderUtils.toARGB(playerColor[0], playerColor[1], playerColor[2], playerColor[3]);
                }
            } else if (e instanceof Monster || e instanceof Enemy) {
                if (hostileMobs.get()) {
                    shouldRender = true;
                    color = RenderUtils.toARGB(hostileColor[0], hostileColor[1], hostileColor[2], hostileColor[3]);
                }
            } else if (e instanceof Animal || e instanceof WaterAnimal || e instanceof Villager) {
                if (passiveMobs.get()) {
                    shouldRender = true;
                    color = RenderUtils.toARGB(passiveColor[0], passiveColor[1], passiveColor[2], passiveColor[3]);
                }
            } else if (e instanceof ItemEntity) {
                if (items.get()) {
                    shouldRender = true;
                    color = RenderUtils.toARGB(itemColor[0], itemColor[1], itemColor[2], itemColor[3]);
                }
            }

            if (shouldRender) {
                if (rainbowMode.get()) {
                    float hue = (float) ((System.currentTimeMillis() + (e.getId() * 140L)) % 4000L) / 4000.0f;
                    int rgb = java.awt.Color.HSBtoRGB(hue, 0.85f, 1.0f);
                    color = (0xFF << 24) | (rgb & 0x00FFFFFF);
                } else if (healthColorMode.get() && e instanceof net.minecraft.world.entity.LivingEntity living) {
                    float ratio = Math.max(0.0f, Math.min(1.0f, living.getHealth() / Math.max(1.0f, living.getMaxHealth())));
                    float r = 1.0f - ratio;
                    float g = ratio;
                    color = RenderUtils.toARGB(r, g, 0.1f, 1.0f);
                }

                ESPRenderer.renderEntity3D(poseStack, camera, e, mode, color, outlineThickness[0], fillOpacity[0], tickDelta);
            }
        }
    }

    /** Public getter so EntityGlowMixin can query the GLOW mode state. */
    public boolean isGlowEnabled() {
        return isEnabled() && isGlowMode();
    }

    public boolean isGlowMode() {
        int idx = modeIndex.get();
        return idx == 2 || idx == 3;
    }

    public int getColorRGBForEntity(Entity e) {
        if (e instanceof Player) {
            return toRGB(playerColor);
        } else if (e instanceof Monster || e instanceof Enemy) {
            return toRGB(hostileColor);
        } else if (e instanceof Animal || e instanceof WaterAnimal || e instanceof Villager) {
            return toRGB(passiveColor);
        } else if (e instanceof ItemEntity) {
            return toRGB(itemColor);
        }
        return toRGB(passiveColor);
    }

    private int toRGB(float[] c) {
        int r = Math.min(255, Math.max(0, (int)(c[0] * 255f)));
        int g = Math.min(255, Math.max(0, (int)(c[1] * 255f)));
        int b = Math.min(255, Math.max(0, (int)(c[2] * 255f)));
        return (r << 16) | (g << 8) | b;
    }

    public boolean includesPlayers()     { return players.get(); }
    public boolean includesHostileMobs() { return hostileMobs.get(); }
    public boolean includesPassiveMobs() { return passiveMobs.get(); }
    public boolean includesItems()       { return items.get(); }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Mode##esp", modeIndex, MODE_NAMES);

        ImGui.separator();
        ImGui.text("Visual Style & Effects:");
        ImGui.checkbox("Rainbow Mode##esp", rainbowMode);
        ImGui.checkbox("Health-based Color##esp", healthColorMode);
        ImGui.sliderFloat("Box Fill Opacity##esp", fillOpacity, 0.0f, 0.6f);
        ImGui.sliderFloat("Outline Width##esp", outlineThickness, 1.0f, 5.0f);

        ImGui.separator();
        ImGui.text("Filters:");
        ImGui.checkbox("Players##esp",      players);
        ImGui.checkbox("Hostile Mobs##esp", hostileMobs);
        ImGui.checkbox("Passive Mobs##esp", passiveMobs);
        ImGui.checkbox("Items##esp",        items);

        ImGui.separator();
        ImGui.text("Color Customization:");
        ImGui.colorEdit4("Player Color##esp",  playerColor);
        ImGui.colorEdit4("Hostile Color##esp", hostileColor);
        ImGui.colorEdit4("Passive Color##esp", passiveColor);
        ImGui.colorEdit4("Item Color##esp",    itemColor);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("modeIndex", modeIndex.get());
        cfg.putSetting("players", players.get());
        cfg.putSetting("hostileMobs", hostileMobs.get());
        cfg.putSetting("passiveMobs", passiveMobs.get());
        cfg.putSetting("items", items.get());
        cfg.putSetting("rainbowMode", rainbowMode.get());
        cfg.putSetting("healthColorMode", healthColorMode.get());
        cfg.putSetting("fillOpacity", fillOpacity[0]);
        cfg.putSetting("outlineThickness", outlineThickness[0]);
        cfg.putSetting("playerColor", List.of(playerColor[0], playerColor[1], playerColor[2], playerColor[3]));
        cfg.putSetting("hostileColor", List.of(hostileColor[0], hostileColor[1], hostileColor[2], hostileColor[3]));
        cfg.putSetting("passiveColor", List.of(passiveColor[0], passiveColor[1], passiveColor[2], passiveColor[3]));
        cfg.putSetting("itemColor", List.of(itemColor[0], itemColor[1], itemColor[2], itemColor[3]));
        return cfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        modeIndex.set(cfg.getSetting("modeIndex", modeIndex.get()));
        players.set(cfg.getSetting("players", players.get()));
        hostileMobs.set(cfg.getSetting("hostileMobs", hostileMobs.get()));
        passiveMobs.set(cfg.getSetting("passiveMobs", passiveMobs.get()));
        items.set(cfg.getSetting("items", items.get()));
        rainbowMode.set(cfg.getSetting("rainbowMode", rainbowMode.get()));
        healthColorMode.set(cfg.getSetting("healthColorMode", healthColorMode.get()));
        fillOpacity[0] = cfg.getSetting("fillOpacity", fillOpacity[0]);
        outlineThickness[0] = cfg.getSetting("outlineThickness", outlineThickness[0]);

        loadColor(cfg.getSetting("playerColor", null), playerColor);
        loadColor(cfg.getSetting("hostileColor", null), hostileColor);
        loadColor(cfg.getSetting("passiveColor", null), passiveColor);
        loadColor(cfg.getSetting("itemColor", null), itemColor);
    }

    private void loadColor(Object obj, float[] target) {
        if (obj instanceof List<?> list && list.size() >= 4) {
            for (int i = 0; i < 4; i++) {
                Object val = list.get(i);
                if (val instanceof Number num) {
                    target[i] = num.floatValue();
                }
            }
        }
    }
}
