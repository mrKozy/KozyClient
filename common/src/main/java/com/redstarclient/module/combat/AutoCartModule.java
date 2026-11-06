// ☭ Red Star Client — AutoCartModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import com.redstarclient.util.PlayerUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImFloat;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartTNT;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Cart PvP Engine:
 * - Auto Cart Bow / Xbow Cart: Automatically shoots TNT Minecarts near enemies for devastating instant explosions
 * - Cart Refill: Keeps rails and TNT minecarts restocked in your hotbar
 */
public class AutoCartModule extends Module {

    private final ImFloat   range           = new ImFloat(12.0f);
    private final ImBoolean autoShootCart   = new ImBoolean(true);
    private final ImBoolean cartRefill      = new ImBoolean(true);

    private long lastShotTime = 0L;

    public AutoCartModule() {
        super("AutoCart", "Detoniert TNT-Minecarts automatisch mit Bogen oder Armbrust (Cart PvP).", ModuleCategory.CART, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        long now = System.currentTimeMillis();

        // 1. Auto Cart Bow & Crossbow
        if (autoShootCart.get() && now - lastShotTime >= 150) {
            MinecartTNT nearestCart = null;
            double nearestDist = range.get();

            for (Entity e : mc.level.entitiesForRendering()) {
                if (e instanceof MinecartTNT cart && !cart.isRemoved()) {
                    double dist = mc.player.distanceTo(cart);
                    if (dist <= nearestDist) {
                        nearestDist = dist;
                        nearestCart = cart;
                    }
                }
            }

            if (nearestCart != null) {
                // If holding charged crossbow or ready bow
                ItemStack held = mc.player.getMainHandItem();
                boolean isBow = held.is(Items.BOW) || held.is(Items.CROSSBOW);
                int bowSlot = -1;
                if (!isBow) {
                    bowSlot = InventoryUtils.findItemInHotbar(mc.player, Items.CROSSBOW);
                    if (bowSlot == -1) bowSlot = InventoryUtils.findItemInHotbar(mc.player, Items.BOW);
                }

                if (isBow || bowSlot != -1) {
                    lastShotTime = now;
                    int prev = mc.player.getInventory().selected;
                    if (bowSlot != -1) mc.player.getInventory().selected = bowSlot;

                    float[] rots = PlayerUtils.getRotationsToEntity(mc.player, nearestCart);
                    float prevYaw = mc.player.getYRot();
                    float prevPitch = mc.player.getXRot();

                    mc.player.setYRot(rots[0]);
                    mc.player.setXRot(rots[1]);

                    mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                    mc.player.swing(InteractionHand.MAIN_HAND);

                    mc.player.setYRot(prevYaw);
                    mc.player.setXRot(prevPitch);

                    if (bowSlot != -1) mc.player.getInventory().selected = prev;
                }
            }
        }

        // 2. Cart Refill
        if (cartRefill.get()) {
            if (InventoryUtils.findItemInHotbar(mc.player, Items.TNT_MINECART) == -1) {
                int invCart = InventoryUtils.findItemInInventory(mc.player, Items.TNT_MINECART);
                if (invCart != -1) {
                    mc.gameMode.handleInventoryMouseClick(mc.player.containerMenu.containerId, invCart, 0, net.minecraft.world.inventory.ClickType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderFloat("Reichweite##cart", range.getData(), 4.0f, 20.0f, "%.1fm");
        ImGui.checkbox("Auto-Shoot Minecarts (Bogen/Armbrust)##cart", autoShootCart);
        ImGui.checkbox("Cart Refill (Hotbar auffuellen)##cart", cartRefill);
        ImGui.spacing();
        ImGui.textDisabled("Fokussiert TNT-Loren fuer instant Detonationen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("range", range.get());
        cfg.putSetting("autoShootCart", autoShootCart.get());
        cfg.putSetting("cartRefill", cartRefill.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        range.set(cfg.getSetting("range", range.get()));
        autoShootCart.set(cfg.getSetting("autoShootCart", autoShootCart.get()));
        cartRefill.set(cfg.getSetting("cartRefill", cartRefill.get()));
    }
}
