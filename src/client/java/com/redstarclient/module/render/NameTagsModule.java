// ☭ Red Star Client — NameTagsModule
package com.redstarclient.module.render;

import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/** Renders custom name tags with health bars above entity heads. */
public class NameTagsModule extends Module {

    private final ImBoolean showHealth   = new ImBoolean(true);
    private final ImBoolean showDistance = new ImBoolean(true);
    private final ImBoolean throughWalls = new ImBoolean(true);
    private final ImFloat   scale        = new ImFloat(1.5f);

    public NameTagsModule() {
        super("NameTags", "Renders custom name tags with health above entities.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        for (LivingEntity le : mc.level.getEntitiesOfClass(LivingEntity.class,
                mc.player.getBoundingBox().inflate(64))) {
            if (le == mc.player) continue;
            if (!le.isAlive()) continue;

            String name = le.getDisplayName().getString();
            float  dist = (float) mc.player.distanceTo(le);
            float  hp   = le.getHealth();
            float  maxHp = le.getMaxHealth();

            // Build tag text
            StringBuilder sb = new StringBuilder(name);
            if (showHealth.get())   sb.append(" ").append(String.format("%.1f❤", hp));
            if (showDistance.get()) sb.append(" [").append((int) dist).append("m]");

            // Draw at fixed screen position (simplified — proper world-to-screen in production)
            int color = le instanceof Player ? RenderUtils.PLAYER_GOLD : RenderUtils.HOSTILE_RED;
            // Render in top-right area as a list (simplified HUD fallback)
            int idx = mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(64)).indexOf(le);
            RenderUtils.drawTextWithBackground(graphics, sb.toString(), 5, 120 + idx * 20, color, RenderUtils.BG_DARK_RED);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Show Health##nt",   showHealth);
        ImGui.checkbox("Show Distance##nt", showDistance);
        ImGui.checkbox("Through Walls##nt", throughWalls);
        ImGui.sliderFloat("Scale##nt",      scale.getData(), 0.5f, 3.0f);
    }
}
