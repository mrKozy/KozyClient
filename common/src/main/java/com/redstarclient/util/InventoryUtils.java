// ☭ Red Star Client — InventoryUtils
package com.redstarclient.util;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Inventory scanning and slot manipulation helpers.
 */
public final class InventoryUtils {

    private InventoryUtils() {}

    /** Finds the first occurrence of {@code item} in hotbar slots 0–8. Returns -1 if absent. */
    public static int findItemInHotbar(LocalPlayer player, Item item) {
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }

    /** Finds the first occurrence of {@code item} across the full inventory (slots 0–35). Returns -1 if absent. */
    public static int findItemInInventory(LocalPlayer player, Item item) {
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i).is(item)) return i;
        }
        return -1;
    }

    /** Returns the hotbar slot with the highest mining speed for the given block state. */
    public static int findBestToolForBlock(LocalPlayer player, BlockState state) {
        int    bestSlot  = player.getInventory().selected;
        float  bestSpeed = 1.0f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot  = i;
            }
        }
        return bestSlot;
    }

    /** Returns the hotbar slot index of the first food item found, or -1. */
    public static int findFoodInHotbar(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem().components().has(net.minecraft.core.component.DataComponents.FOOD)) return i;
        }
        return -1;
    }

    /** Returns the inventory slot index of the first food item found, or -1. */
    public static int findFoodInInventory(LocalPlayer player) {
        for (int i = 0; i < 36; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem().components().has(net.minecraft.core.component.DataComponents.FOOD)) return i;
        }
        return -1;
    }

    /** True if the offhand is holding a Totem of Undying. */
    public static boolean hasTotemInOffhand(LocalPlayer player) {
        return player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
    }

    /** Returns the inventory slot index of a Totem of Undying, or -1. */
    public static int findTotemInInventory(LocalPlayer player) {
        return findItemInInventory(player, Items.TOTEM_OF_UNDYING);
    }

    /** Returns the hotbar slot of the first placeable block, or -1. */
    public static int findPlaceableBlockSlot(LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.getItem() instanceof net.minecraft.world.item.BlockItem) {
                return i;
            }
        }
        return -1;
    }

    /** Counts total placeable-block items across the hotbar. */
    public static int countBlocksInHotbar(LocalPlayer player) {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && s.getItem() instanceof net.minecraft.world.item.BlockItem) {
                count += s.getCount();
            }
        }
        return count;
    }
}
