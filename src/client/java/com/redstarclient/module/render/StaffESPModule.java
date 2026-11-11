// Red Star Client — StaffESPModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

import java.util.*;

/**
 * Staff & Invisibility ESP:
 * Highlights invisible players, spectators, vanished staff, and server administrators
 * through walls with high-priority warnings, alerts and distinct ESP boxes.
 */
public class StaffESPModule extends Module {

    private final ImBoolean detectInvisible = new ImBoolean(true);
    private final ImBoolean detectSpectator = new ImBoolean(true);
    private final ImBoolean detectStaff     = new ImBoolean(true);
    private final ImBoolean detectVanish    = new ImBoolean(true);
    private final ImBoolean chatAlert       = new ImBoolean(true);
    private final ImBoolean soundAlert      = new ImBoolean(true);
    private final ImInt     alertDistance   = new ImInt(64);

    private final Set<UUID> alertedPlayers = new HashSet<>();
    private long lastAlertClearTime = 0;

    private static final String[] STAFF_KEYWORDS = {
        "admin", "staff", "mod", "moderator", "owner", "dev", "developer",
        "support", "helper", "operator", "op", "leitung", "inhaber", "guard",
        "builder", "manager", "lead", "team", "gm", "gamemaster"
    };

    public StaffESPModule() {
        super("StaffESP", "Erkennt unsichtbare Spieler, Spectator, Vanish & Server-Staff (Admins/Mods).", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAlertClearTime > 10000) {
            alertedPlayers.clear();
            lastAlertClearTime = now;
        }

        Collection<PlayerInfo> tabList = (mc.getConnection() != null)
                ? mc.getConnection().getOnlinePlayers()
                : Collections.emptyList();

        Set<UUID> tabUUIDs = new HashSet<>();
        for (PlayerInfo info : tabList) {
            tabUUIDs.add(info.getProfile().id());
        }

        for (Player p : mc.level.players()) {
            if (p == mc.player) continue;

            boolean isStaff = false;
            String staffRole = "";
            boolean isVanish = false;
            boolean isInvis = false;
            boolean isSpec = false;

            // 1. Invisibility Check
            if (detectInvisible.get()) {
                if (p.isInvisible() || p.hasEffect(MobEffects.INVISIBILITY)) {
                    isInvis = true;
                }
            }

            // 2. Spectator Check
            if (detectSpectator.get() && p.isSpectator()) {
                isSpec = true;
            }

            // 3. Vanish Check (Player entity in world, but missing from TabList)
            if (detectVanish.get() && !tabUUIDs.isEmpty()) {
                if (!tabUUIDs.contains(p.getUUID())) {
                    isVanish = true;
                }
            }

            // 4. Staff / Admin Prefix Check
            if (detectStaff.get()) {
                String fullInfo = (p.getName().getString() + " " +
                        p.getDisplayName().getString() + " " +
                        p.getScoreboardName()).toLowerCase(Locale.ROOT);

                if (p.getTeam() != null) {
                    fullInfo += " " + p.getTeam().getFormattedDisplayName().getString().toLowerCase(Locale.ROOT);
                }

                for (String kw : STAFF_KEYWORDS) {
                    if (fullInfo.contains(kw)) {
                        isStaff = true;
                        staffRole = kw.toUpperCase(Locale.ROOT);
                        break;
                    }
                }
            }

            // Check if flagged
            if (!isInvis && !isSpec && !isVanish && !isStaff) continue;

            // Determine tag and color
            String tag;
            int boxColor;

            if (isVanish) {
                tag = "[VANISHED STAFF] " + p.getName().getString();
                boxColor = RenderUtils.toARGB(1.0f, 0.05f, 0.05f, 1.0f); // Bright red
            } else if (isStaff) {
                tag = "[STAFF: " + staffRole + "] " + p.getName().getString();
                boxColor = RenderUtils.toARGB(1.0f, 0.2f, 0.0f, 1.0f); // Bright red-orange
            } else if (isInvis) {
                tag = "[INVISIBLE] " + p.getName().getString();
                boxColor = RenderUtils.toARGB(0.85f, 0.1f, 1.0f, 1.0f); // Neon purple
            } else {
                tag = "[SPECTATOR] " + p.getName().getString();
                boxColor = RenderUtils.toARGB(0.1f, 0.85f, 1.0f, 1.0f); // Cyan
            }

            // Draw 3D Box around player through walls
            RenderUtils.drawBox3D(poseStack, camera, p.getBoundingBox(), boxColor, 2.5f, 0.25f);

            // Draw Billboard overhead tag
            int dist = (int) mc.player.distanceTo(p);
            String label = tag + " (" + dist + "m)";
            try {
                net.minecraft.gizmos.Gizmos.billboardTextOverMob(p, 0, label, boxColor, 1.1f).setAlwaysOnTop();
            } catch (Throwable ignored) {}

            // Trigger alert if in range
            if (dist <= alertDistance.get() && alertedPlayers.add(p.getUUID())) {
                triggerAlert(p, tag);
            }
        }
    }

    private void triggerAlert(Player p, String tag) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (soundAlert.get() && mc.level != null) {
            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                    SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, 1.8f, false);
        }

        if (chatAlert.get()) {
            String msg = "§c§l[WARNUNG] §e" + tag + " §7in Reichweite entdeckt (§f" + (int) mc.player.distanceTo(p) + "m§7)!";
            mc.player.sendSystemMessage(Component.literal(msg));
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Unsichtbare Spieler erkennen (Invisibility Potion)##stf", detectInvisible);
        ImGui.checkbox("Spectator-Spieler erkennen##stf", detectSpectator);
        ImGui.checkbox("Staff / Admins über Team & Ränge erkennen##stf", detectStaff);
        ImGui.checkbox("Vanish-Detector (Spieler nicht in Tabliste)##stf", detectVanish);
        ImGui.separator();
        ImGui.sliderInt("Warn-Reichweite (Blöcke)##stf", alertDistance.getData(), 16, 128);
        ImGui.checkbox("Chat-Warnmeldung ausgeben##stf", chatAlert);
        ImGui.checkbox("Warn-Ton abspielen##stf", soundAlert);
        ImGui.spacing();
        ImGui.textDisabled("Schützt vor Admins, die sich unsichtbar oder im Spectator heranteleportieren.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("detectInvisible", detectInvisible.get());
        cfg.putSetting("detectSpectator", detectSpectator.get());
        cfg.putSetting("detectStaff", detectStaff.get());
        cfg.putSetting("detectVanish", detectVanish.get());
        cfg.putSetting("chatAlert", chatAlert.get());
        cfg.putSetting("soundAlert", soundAlert.get());
        cfg.putSetting("alertDistance", alertDistance.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        detectInvisible.set(cfg.getSetting("detectInvisible", detectInvisible.get()));
        detectSpectator.set(cfg.getSetting("detectSpectator", detectSpectator.get()));
        detectStaff.set(cfg.getSetting("detectStaff", detectStaff.get()));
        detectVanish.set(cfg.getSetting("detectVanish", detectVanish.get()));
        chatAlert.set(cfg.getSetting("chatAlert", chatAlert.get()));
        soundAlert.set(cfg.getSetting("soundAlert", soundAlert.get()));
        alertDistance.set(cfg.getSetting("alertDistance", alertDistance.get()));
    }
}
