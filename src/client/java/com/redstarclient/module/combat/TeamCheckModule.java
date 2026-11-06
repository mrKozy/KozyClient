// ☭ Red Star Client — TeamCheckModule
package com.redstarclient.module.combat;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.scores.Team;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * TeamCheck: Prevents combat modules (KillAura, TriggerBot, AimAssist, AutoCrystal, etc.)
 * from targeting teammates in Bedwars, Skywars, and team minigames.
 * Supports Scoreboard Teams, DisplayName Colors, Leather Armor Color, and a Friends Whitelist.
 */
public class TeamCheckModule extends Module {

    private static TeamCheckModule instance;

    private final ImBoolean checkScoreboard = new ImBoolean(true);
    private final ImBoolean checkColorCode  = new ImBoolean(true);
    private final ImBoolean checkArmorColor = new ImBoolean(true);
    private final ImString  friendsList     = new ImString("", 512);

    private final Set<String> friendsSet = new HashSet<>();

    public TeamCheckModule() {
        super("TeamCheck", "Verhindert Angriffe auf Teammitglieder in Bedwars, Skywars und Team-Spielen.", ModuleCategory.COMBAT, -1);
        instance = this;
    }

    public static boolean isTeammate(Entity entity) {
        if (instance == null || !instance.isEnabled()) return false;
        if (!(entity instanceof Player otherPlayer)) return false;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (otherPlayer == mc.player) return true;

        String otherRawName = otherPlayer.getName().getString();

        // 1. Friends Whitelist
        if (instance.friendsSet.contains(otherRawName.toLowerCase())) {
            return true;
        }

        // 2. Scoreboard Team Check
        if (instance.checkScoreboard.get()) {
            Team myTeam = mc.player.getTeam();
            Team otherTeam = otherPlayer.getTeam();
            if (myTeam != null && otherTeam != null) {
                if (myTeam.isAlliedTo(otherTeam)) {
                    return true;
                }
                if (myTeam.getColor().isPresent() && myTeam.getColor().equals(otherTeam.getColor())) {
                    return true;
                }
            }
        }

        // 3. DisplayName TextColor Check (Bedwars & Skywars 1.21.4 Formatting)
        if (instance.checkColorCode.get()) {
            TextColor myColor = mc.player.getDisplayName().getStyle().getColor();
            TextColor otherColor = otherPlayer.getDisplayName().getStyle().getColor();

            if (myColor != null && otherColor != null && myColor.equals(otherColor)) {
                return true;
            }
        }

        // 4. Leather Armor Dye Color Check (Common in Bedwars teams)
        if (instance.checkArmorColor.get()) {
            ItemStack myChest = mc.player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack otherChest = otherPlayer.getItemBySlot(EquipmentSlot.CHEST);

            int myDye = DyedItemColor.getOrDefault(myChest, -1);
            int otherDye = DyedItemColor.getOrDefault(otherChest, -2);

            if (myDye != -1 && myDye == otherDye) {
                return true;
            }
        }

        return false;
    }

    private void updateFriendsSet() {
        friendsSet.clear();
        String raw = friendsList.get().trim();
        if (!raw.isEmpty()) {
            String[] parts = raw.split("[,; ]+");
            for (String p : parts) {
                if (!p.trim().isEmpty()) {
                    friendsSet.add(p.trim().toLowerCase());
                }
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Scoreboard-Teams & Farben pruefen##tc", checkScoreboard);
        ImGui.checkbox("Namens-Textfarbe pruefen (Bedwars/Skywars)##tc", checkColorCode);
        ImGui.checkbox("Lederruestungs-Farbe pruefen (Armor Team Check)##tc", checkArmorColor);
        ImGui.separator();
        ImGui.text("Freunde-Whitelist (Komma-getrennt):");
        if (ImGui.inputText("##friendsInput", friendsList)) {
            updateFriendsSet();
        }
        ImGui.spacing();
        ImGui.textDisabled("Schuetzt Teamkameraden und Freunde vor KillAura, Triggerbot, SilentAim & AutoCrystal.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("checkScoreboard", checkScoreboard.get());
        cfg.putSetting("checkColorCode", checkColorCode.get());
        cfg.putSetting("checkArmorColor", checkArmorColor.get());
        cfg.putSetting("friendsList", friendsList.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        checkScoreboard.set(cfg.getSetting("checkScoreboard", checkScoreboard.get()));
        checkColorCode.set(cfg.getSetting("checkColorCode", checkColorCode.get()));
        checkArmorColor.set(cfg.getSetting("checkArmorColor", checkArmorColor.get()));
        friendsList.set(cfg.getSetting("friendsList", friendsList.get()));
        updateFriendsSet();
    }
}
