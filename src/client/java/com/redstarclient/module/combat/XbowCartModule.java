// ☭ Red Star Client — XbowCartModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

/**
 * XbowCart: Cart PvP automated trigger helper.
 * Quickly fires loaded crossbow at TNT minecarts when aligned for instant detonation.
 */
public class XbowCartModule extends Module {

    private final ImBoolean autoRelease = new ImBoolean(true);
    private long lastShot = 0L;

    public XbowCartModule() {
        super("XbowCart", "Cart PvP: Zuendet TNT-Minecarts blitzschnell mit vorgeladener Armbrust fuer Instant-Explosionen.", ModuleCategory.CART, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (!held.isEmpty() && held.getItem() instanceof CrossbowItem) {
            if (CrossbowItem.isCharged(held) && autoRelease.get()) {
                long now = System.currentTimeMillis();
                if (now - lastShot > 250) {
                    lastShot = now;
                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Automatisch geladene Armbrust sofort abfeuern##xc", autoRelease);
        ImGui.spacing();
        ImGui.textDisabled("Zuendet platzierte TNT-Loren in Millisekunden fuer toedliche Burst-Schaden.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("autoRelease", autoRelease.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        autoRelease.set(cfg.getSetting("autoRelease", autoRelease.get()));
    }
}
