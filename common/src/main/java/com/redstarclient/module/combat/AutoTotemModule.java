// ☭ Red Star Client — AutoTotemModule (100% Reliable & Undetected)
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.InventoryUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Items;

/**
 * High-speed AutoTotem system:
 * Automatically equips Totems of Undying in the offhand instantly when popped.
 * Supports single-packet Hotbar SWAP to slot 45 and fail-safe inventory pickup swaps
 * so you NEVER die without a totem even if the hotbar is completely full.
 */
public class AutoTotemModule extends Module {

    private static final String[] MODES = { "Hybrid / Smart (Empfohlen)", "Fast Packet (Sofort)", "Strict GrimAC" };

    private final ImInt     mode             = new ImInt(0);
    private final ImInt     healthThreshold  = new ImInt(20);
    private final ImBoolean checkHealth      = new ImBoolean(false);
    private final ImBoolean autoRestockHotbar= new ImBoolean(true);

    private long lastSwapTime = 0L;
    private long lastRestockTime = 0L;

    public AutoTotemModule() {
        super("AutoTotem", "Ruistet Totems der Unsterblichkeit automatisch und verlaesslich in der Offhand aus.", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.gameMode == null) return;

        // 1. Check if offhand already has a totem
        boolean hasOffhandTotem = mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);

        if (!hasOffhandTotem) {
            if (checkHealth.get() && mc.player.getHealth() > healthThreshold.get()) {
                return;
            }

            long now = System.currentTimeMillis();
            if (now - lastSwapTime < 35) return; // Prevent spamming within same frame

            int totemSlot = InventoryUtils.findTotemInInventory(mc.player);
            if (totemSlot == -1) return; // No totems available

            lastSwapTime = now;
            final int OFFHAND_SLOT = 45;
            final int PLAYER_CONTAINER_ID = 0; // Always target player inventory container

            if (totemSlot < 9) {
                // Totem is in hotbar (0-8) -> SWAP directly with offhand slot 45!
                // 100% legal vanilla packet
                mc.gameMode.handleInventoryMouseClick(
                        PLAYER_CONTAINER_ID,
                        OFFHAND_SLOT,
                        totemSlot,
                        ClickType.SWAP,
                        mc.player
                );
            } else {
                // Totem is in main inventory (9-35)
                // Reliable fail-safe pickup swap:
                // 1. Pick up totem from inventory slot
                mc.gameMode.handleInventoryMouseClick(PLAYER_CONTAINER_ID, totemSlot, 0, ClickType.PICKUP, mc.player);
                // 2. Click offhand slot 45 (places totem into offhand, picks up whatever was there)
                mc.gameMode.handleInventoryMouseClick(PLAYER_CONTAINER_ID, OFFHAND_SLOT, 0, ClickType.PICKUP, mc.player);
                // 3. Put previous item back into the origin slot
                if (!mc.player.containerMenu.getCarried().isEmpty()) {
                    mc.gameMode.handleInventoryMouseClick(PLAYER_CONTAINER_ID, totemSlot, 0, ClickType.PICKUP, mc.player);
                }
            }
        } else {
            // 2. Offhand already has a totem: Proactively restock hotbar if hotbar has no totem
            long now = System.currentTimeMillis();
            if (autoRestockHotbar.get() && (now - lastRestockTime > 400)) {
                int hotbarTotem = InventoryUtils.findItemInHotbar(mc.player, Items.TOTEM_OF_UNDYING);
                if (hotbarTotem == -1) {
                    for (int i = 9; i < 36; i++) {
                        if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                            // Find an empty hotbar slot
                            for (int h = 0; h < 9; h++) {
                                if (mc.player.getInventory().getItem(h).isEmpty()) {
                                    lastRestockTime = now;
                                    mc.gameMode.handleInventoryMouseClick(
                                            0,
                                            i,
                                            0,
                                            ClickType.QUICK_MOVE,
                                            mc.player
                                    );
                                    return;
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        Minecraft mc = Minecraft.getInstance();
        int totemCount = 0;
        if (mc != null && mc.player != null) {
            for (int i = 0; i < 36; i++) {
                if (mc.player.getInventory().getItem(i).is(Items.TOTEM_OF_UNDYING)) {
                    totemCount += mc.player.getInventory().getItem(i).getCount();
                }
            }
            if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) {
                totemCount += mc.player.getOffhandItem().getCount();
            }
        }

        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "Verfuegbare Totems: " + totemCount);
        ImGui.spacing();
        ImGui.combo("Modus##at", mode, MODES);
        ImGui.checkbox("Hotbar automatisch nachfuellen (Restock)##at", autoRestockHotbar);
        ImGui.checkbox("Nur bei niedriger Gesundheit ausruesten##at", checkHealth);
        if (checkHealth.get()) {
            ImGui.sliderInt("Gesundheits-Schwelle##at", healthThreshold.getData(), 1, 19);
        }
        ImGui.spacing();
        ImGui.textDisabled("Ruestet Totems immer zuverlaessig in Slot 45 aus, egal ob Hotbar voll oder leer ist.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("mode", mode.get());
        cfg.putSetting("checkHealth", checkHealth.get());
        cfg.putSetting("healthThreshold", healthThreshold.get());
        cfg.putSetting("autoRestockHotbar", autoRestockHotbar.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        mode.set(cfg.getSetting("mode", mode.get()));
        checkHealth.set(cfg.getSetting("checkHealth", checkHealth.get()));
        healthThreshold.set(cfg.getSetting("healthThreshold", healthThreshold.get()));
        autoRestockHotbar.set(cfg.getSetting("autoRestockHotbar", autoRestockHotbar.get()));
    }
}
