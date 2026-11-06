// ☭ Red Star Client — NoSlowModule
package com.redstarclient.module.movement;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Eliminates movement slowdown while eating, charging bows, using shields,
 * walking through cobwebs, or stepping on soul sand / honey blocks.
 */
public class NoSlowModule extends Module {

    private final ImBoolean items     = new ImBoolean(true);
    private final ImBoolean grimSafe  = new ImBoolean(true);
    private final ImBoolean webs      = new ImBoolean(true);
    private final ImBoolean soulSand  = new ImBoolean(true);
    private final ImBoolean honey     = new ImBoolean(true);
    private final ImBoolean sneak     = new ImBoolean(false);
    private final ImFloat speedFactor = new ImFloat(1.0f);

    public NoSlowModule() {
        super("NoSlow", "Prevents movement slowdown while eating, using items or walking on slow blocks.", ModuleCategory.MOVEMENT, -1);
    }

    public boolean isItemsEnabled() {
        return isEnabled() && items.get();
    }

    public boolean isWebsEnabled() {
        return isEnabled() && webs.get();
    }

    public boolean isGrimSafe() {
        return grimSafe.get();
    }

    public boolean isSneakEnabled() {
        return isEnabled() && sneak.get();
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || !isEnabled()) return;

        // 1. Cobwebs & Slow Blocks
        if (webs.get() || soulSand.get() || honey.get()) {
            var blockState = mc.level != null ? mc.level.getBlockState(mc.player.blockPosition()) : null;
            var belowState = mc.level != null ? mc.level.getBlockState(mc.player.blockPosition().below()) : null;

            boolean inWeb = blockState != null && blockState.is(Blocks.COBWEB);
            boolean onSoul = belowState != null && (belowState.is(Blocks.SOUL_SAND) || belowState.is(Blocks.SOUL_SOIL));
            boolean onHoney = belowState != null && belowState.is(Blocks.HONEY_BLOCK);

            if ((inWeb && webs.get()) || (onSoul && soulSand.get()) || (onHoney && honey.get())) {
                Vec3 v = mc.player.getDeltaMovement();
                if (mc.options.keyUp.isDown() || mc.options.keyDown.isDown() ||
                    mc.options.keyLeft.isDown() || mc.options.keyRight.isDown()) {
                    double mult = inWeb ? 2.5 : 1.4;
                    mc.player.setDeltaMovement(v.x * mult, v.y, v.z * mult);
                }
            }
        }

        // 2. Sneak Speed
        if (sneak.get() && mc.player.isCrouching()) {
            Vec3 v = mc.player.getDeltaMovement();
            if (mc.options.keyUp.isDown() || mc.options.keyDown.isDown() ||
                mc.options.keyLeft.isDown() || mc.options.keyRight.isDown()) {
                mc.player.setDeltaMovement(v.x * 1.5, v.y, v.z * 1.5);
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Items (Food, Bow, Shield)##ns", items);
        ImGui.checkbox("GrimAC Safe Mode##ns", grimSafe);
        if (grimSafe.get()) {
            ImGui.textColored(0.35f, 0.90f, 0.45f, 1.0f, "GrimAC Bypass: Applies 0.88x speed leniency to bypass simulation.");
        }
        ImGui.checkbox("Cobwebs##ns", webs);
        ImGui.checkbox("Soul Sand##ns", soulSand);
        ImGui.checkbox("Honey Blocks##ns", honey);
        ImGui.checkbox("Sneak Full Speed##ns", sneak);
        ImGui.sliderFloat("Speed Boost##ns", speedFactor.getData(), 0.5f, 2.0f, "%.1fx");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("items", items.get());
        cfg.putSetting("grimSafe", grimSafe.get());
        cfg.putSetting("webs", webs.get());
        cfg.putSetting("soulSand", soulSand.get());
        cfg.putSetting("honey", honey.get());
        cfg.putSetting("sneak", sneak.get());
        cfg.putSetting("speedFactor", speedFactor.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        items.set(cfg.getSetting("items", items.get()));
        grimSafe.set(cfg.getSetting("grimSafe", grimSafe.get()));
        webs.set(cfg.getSetting("webs", webs.get()));
        soulSand.set(cfg.getSetting("soulSand", soulSand.get()));
        honey.set(cfg.getSetting("honey", honey.get()));
        sneak.set(cfg.getSetting("sneak", sneak.get()));
        speedFactor.set(cfg.getSetting("speedFactor", speedFactor.get()));
    }
}
