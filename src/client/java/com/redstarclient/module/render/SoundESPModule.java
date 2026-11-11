// Red Star Client — SoundESPModule
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Sound ESP:
 * Visualizes in-game sounds (player footsteps, hurt sounds, explosions, chests opening,
 * block break/place, mob sounds) through walls as expanding 3D rings and billboard markers.
 */
public class SoundESPModule extends Module {

    public static class SoundPing {
        public final Vec3 pos;
        public final String name;
        public final SoundSource source;
        public final long timestamp;
        public final int color;

        public SoundPing(Vec3 pos, String name, SoundSource source, long timestamp, int color) {
            this.pos = pos;
            this.name = name;
            this.source = source;
            this.timestamp = timestamp;
            this.color = color;
        }
    }

    private final ConcurrentLinkedDeque<SoundPing> pings = new ConcurrentLinkedDeque<>();

    private final ImInt     durationMs  = new ImInt(2500);
    private final ImBoolean showPlayers = new ImBoolean(true);
    private final ImBoolean showHostile = new ImBoolean(true);
    private final ImBoolean showBlocks  = new ImBoolean(true);
    private final ImBoolean showOther   = new ImBoolean(false);
    private final ImBoolean drawRings   = new ImBoolean(true);
    private final ImBoolean drawTracers = new ImBoolean(false);
    private final ImBoolean drawText    = new ImBoolean(true);

    public SoundESPModule() {
        super("SoundESP", "Visualisiert Schritte, Kisten, Kämpfe und Monster-Sounds als 3D-Ringe durch Wände.", ModuleCategory.RENDER, -1);
    }

    public void onSoundPacket(ClientboundSoundPacket packet) {
        if (!isEnabled()) return;
        SoundSource src = packet.getSource();
        if (!shouldCapture(src)) return;

        Vec3 pos = new Vec3(packet.getX(), packet.getY(), packet.getZ());
        String soundPath = packet.getSound().value().location().getPath();
        registerPing(pos, soundPath, src);
    }

    public void onSoundEntityPacket(ClientboundSoundEntityPacket packet) {
        if (!isEnabled()) return;
        SoundSource src = packet.getSource();
        if (!shouldCapture(src)) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Entity e = mc.level.getEntity(packet.getId());
        if (e == null) return;

        Vec3 pos = e.position();
        String soundPath = packet.getSound().value().location().getPath();
        registerPing(pos, soundPath, src);
    }

    private boolean shouldCapture(SoundSource src) {
        if (src == SoundSource.PLAYERS) return showPlayers.get();
        if (src == SoundSource.HOSTILE) return showHostile.get();
        if (src == SoundSource.BLOCKS) return showBlocks.get();
        return showOther.get();
    }

    private void registerPing(Vec3 pos, String rawName, SoundSource src) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.player.position().distanceToSqr(pos) < 1.0) {
            // Ignore own self sounds at exact player feet
            return;
        }

        String label = simplifySoundName(rawName);
        int color = getSoundColor(src, rawName);
        pings.add(new SoundPing(pos, label, src, System.currentTimeMillis(), color));

        // Limit maximum queued pings
        while (pings.size() > 64) {
            pings.pollFirst();
        }
    }

    private String simplifySoundName(String raw) {
        if (raw.contains("minecart") || raw.contains("riding")) return "Minecart (Farm/Rail)";
        if (raw.contains("piston")) return "Piston (Auto-Farm)";
        if (raw.contains("dispenser") || raw.contains("dropper")) return "Dispenser / Dropper";
        if (raw.contains("step")) return "Footstep";
        if (raw.contains("hurt")) return "Damage / Hurt";
        if (raw.contains("attack")) return "Attack Swing";
        if (raw.contains("explode")) return "Explosion";
        if (raw.contains("chest.open")) return "Chest Open";
        if (raw.contains("chest.close")) return "Chest Close";
        if (raw.contains("bow")) return "Bow Shoot";
        if (raw.contains("eat") || raw.contains("drink")) return "Eating / Drinking";
        if (raw.contains("totem")) return "Totem Pop!";
        if (raw.contains("splash") || raw.contains("swim")) return "Water Splash";
        if (raw.contains("creeper")) return "Creeper Hiss!";
        if (raw.contains("break")) return "Block Break";
        if (raw.contains("place")) return "Block Place";
        if (raw.contains("portal")) return "Portal";
        if (raw.contains("wind_charge")) return "Wind Charge";
        if (raw.contains("beacon")) return "Beacon Sound";

        // Fallback: extract last component
        int lastSlash = raw.lastIndexOf('/');
        int lastDot = raw.lastIndexOf('.');
        int start = Math.max(lastSlash, lastDot);
        return start >= 0 && start + 1 < raw.length() ? raw.substring(start + 1) : raw;
    }

    private int getSoundColor(SoundSource src, String raw) {
        if (raw.contains("minecart") || raw.contains("piston") || raw.contains("dispenser")) {
            return RenderUtils.toARGB(0.2f, 1.0f, 0.4f, 1.0f); // Bright Lime for farm/minecart sounds
        }
        if (raw.contains("totem") || raw.contains("creeper") || raw.contains("explode")) {
            return RenderUtils.toARGB(1.0f, 0.1f, 0.1f, 1.0f); // Bright red alert
        }
        if (src == SoundSource.PLAYERS || raw.contains("step") || raw.contains("attack")) {
            return RenderUtils.toARGB(1.0f, 0.85f, 0.1f, 1.0f); // Gold / Player
        }
        if (src == SoundSource.HOSTILE) {
            return RenderUtils.toARGB(1.0f, 0.35f, 0.1f, 1.0f); // Hostile orange
        }
        if (src == SoundSource.BLOCKS) {
            return RenderUtils.toARGB(0.2f, 0.85f, 1.0f, 1.0f); // Cyan / Block
        }
        return RenderUtils.toARGB(0.8f, 0.8f, 0.8f, 1.0f);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        long maxAge = Math.max(500, durationMs.get());

        Iterator<SoundPing> it = pings.iterator();
        while (it.hasNext()) {
            SoundPing ping = it.next();
            long elapsed = now - ping.timestamp;
            if (elapsed > maxAge) {
                it.remove();
                continue;
            }

            float ageFactor = elapsed / (float) maxAge; // 0.0 -> 1.0
            float radius = 0.25f + (1.25f * ageFactor);
            float alpha = Math.max(0.05f, 1.0f - ageFactor);

            int alphaInt = (int) (alpha * 255.0f) & 0xFF;
            int ringColor = (alphaInt << 24) | (ping.color & 0x00FFFFFF);

            try {
                if (drawRings.get()) {
                    net.minecraft.gizmos.GizmoStyle ringStyle = net.minecraft.gizmos.GizmoStyle.stroke(ringColor, 2.5f);
                    net.minecraft.gizmos.Gizmos.circle(ping.pos, radius, ringStyle).setAlwaysOnTop();
                }

                if (drawTracers.get()) {
                    Vec3 eye = mc.player.getEyePosition(tickDelta);
                    net.minecraft.gizmos.Gizmos.line(eye, ping.pos, ringColor, 1.5f).setAlwaysOnTop();
                }

                if (drawText.get()) {
                    int dist = (int) mc.player.position().distanceTo(ping.pos);
                    String display = ping.name + " (" + dist + "m)";
                    BlockPos bpos = BlockPos.containing(ping.pos.x, ping.pos.y + 0.35, ping.pos.z);
                    net.minecraft.gizmos.Gizmos.billboardTextOverBlock(display, bpos, ringColor, RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.7f), 1.0f)
                            .setAlwaysOnTop();
                }
            } catch (Throwable ignored) {}
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Anzeige-Dauer (ms)##snd", durationMs.getData(), 500, 5000);
        ImGui.separator();
        ImGui.checkbox("Spieler-Sounds (Schritte, Hits)##snd", showPlayers);
        ImGui.checkbox("Monster-Sounds (Creeper, Mobs)##snd", showHostile);
        ImGui.checkbox("Block-Sounds (Kisten, Abbauen)##snd", showBlocks);
        ImGui.checkbox("Sonstige Sounds##snd", showOther);
        ImGui.separator();
        ImGui.checkbox("3D-Expansions-Ringe zeichnen##snd", drawRings);
        ImGui.checkbox("Tracer-Linien zum Sound##snd", drawTracers);
        ImGui.checkbox("Sound-Name & Distanz-Text##snd", drawText);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("durationMs", durationMs.get());
        cfg.putSetting("showPlayers", showPlayers.get());
        cfg.putSetting("showHostile", showHostile.get());
        cfg.putSetting("showBlocks", showBlocks.get());
        cfg.putSetting("showOther", showOther.get());
        cfg.putSetting("drawRings", drawRings.get());
        cfg.putSetting("drawTracers", drawTracers.get());
        cfg.putSetting("drawText", drawText.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        durationMs.set(cfg.getSetting("durationMs", durationMs.get()));
        showPlayers.set(cfg.getSetting("showPlayers", showPlayers.get()));
        showHostile.set(cfg.getSetting("showHostile", showHostile.get()));
        showBlocks.set(cfg.getSetting("showBlocks", showBlocks.get()));
        showOther.set(cfg.getSetting("showOther", showOther.get()));
        drawRings.set(cfg.getSetting("drawRings", drawRings.get()));
        drawTracers.set(cfg.getSetting("drawTracers", drawTracers.get()));
        drawText.set(cfg.getSetting("drawText", drawText.get()));
    }
}
