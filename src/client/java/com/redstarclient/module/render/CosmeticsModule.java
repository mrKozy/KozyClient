// KozyClient — CosmeticsModule
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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Cosmetics Module (Anubis & Kozy Edition):
 * - Sword Models: Butterfly Knife, Karambit, Katana, Red Dagger
 * - Character Skins: Tung Tung Sahur 3D Wooden Avatar, Pharaoh Headdress
 * - Head Wear: Asian Hat, Angel Halo, Horns
 */
public class CosmeticsModule extends Module {

    // Sword Model Cosmetics
    public enum SwordModel {
        DEFAULT("Vanilla / Red PvP"),
        BUTTERFLY("Butterfly Knife"),
        KARAMBIT("Karambit"),
        KATANA("Katana"),
        RED_DAGGER("Red Star Dagger");

        public final String label;
        SwordModel(String label) { this.label = label; }
    }

    // Character Skins / Models
    public enum CharacterSkin {
        NONE("Normal Player"),
        TUNG_TUNG_SAHUR("Tung Tung Tung Sahur (Wooden Bedug)"),
        PHARAOH("Pharaoh Headdress");

        public final String label;
        CharacterSkin(String label) { this.label = label; }
    }

    private final ImInt selectedSwordModel = new ImInt(1); // 1 = Butterfly by default
    private final ImInt selectedCharacter  = new ImInt(1); // 1 = Tung Tung Sahur by default
    private final ImBoolean showThirdPerson = new ImBoolean(true);
    private final ImBoolean redAura         = new ImBoolean(true);
    private final ImBoolean drumAnimation   = new ImBoolean(true); // Sahur drum animation

    public CosmeticsModule() {
        super("Cosmetics", "Ermoeglicht Anubis-Schwertmodelle (Butterfly, Karambit, Katana) und Charakter-Skins wie Tung Tung Sahur.", ModuleCategory.RENDER, -1);
        setEnabled(true);
    }

    public SwordModel getSwordModel() {
        int idx = selectedSwordModel.get();
        SwordModel[] vals = SwordModel.values();
        if (idx >= 0 && idx < vals.length) return vals[idx];
        return SwordModel.DEFAULT;
    }

    public CharacterSkin getCharacterSkin() {
        int idx = selectedCharacter.get();
        CharacterSkin[] vals = CharacterSkin.values();
        if (idx >= 0 && idx < vals.length) return vals[idx];
        return CharacterSkin.NONE;
    }

    public void setSwordModel(SwordModel model) {
        selectedSwordModel.set(model.ordinal());
    }

    public void setCharacterSkin(CharacterSkin skin) {
        selectedCharacter.set(skin.ordinal());
    }

    public boolean isShowThirdPerson() {
        return showThirdPerson.get();
    }

    public boolean isRedAura() {
        return redAura.get();
    }

    public boolean isDrumAnimation() {
        return drumAnimation.get();
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (!isEnabled()) return;

        // Render Tung Tung Sahur character rig when enabled
        if (getCharacterSkin() == CharacterSkin.TUNG_TUNG_SAHUR && showThirdPerson.get()) {
            com.redstarclient.cosmetics.AnubisCharacterRenderer.renderTung(mc.player, poseStack, camera, tickDelta);
        }
    }

    /**
     * Renders the authentic Tung Tung Tung Sahur wooden bedug/drum companion model
     * hovering next to / equipped on the player with animated drum-beating motion.
     */
    private void renderTungTungSahur(Player p, float tickDelta) {
        double px = Mth.lerp((double) tickDelta, p.xo, p.getX());
        double py = Mth.lerp((double) tickDelta, p.yo, p.getY());
        double pz = Mth.lerp((double) tickDelta, p.zo, p.getZ());

        float bodyYaw = Mth.lerp(tickDelta, p.yBodyRotO, p.yBodyRot);
        float rad = (float) Math.toRadians(bodyYaw);

        // Position: right shoulder floating companion (bedug drum)
        double ox = -Math.sin(rad) * 0.75;
        double oz = Math.cos(rad) * 0.75;
        double drumX = px + ox;
        double drumY = py + 0.95 + (Math.sin((System.currentTimeMillis() % 2000) / 2000.0 * Math.PI * 2) * 0.06);
        double drumZ = pz + oz;

        // 1. Bedug Log Barrel (Cylinder / Box approximation in dark wood & red bands)
        AABB logBox = new AABB(drumX - 0.28, drumY - 0.25, drumZ - 0.28, drumX + 0.28, drumY + 0.35, drumZ + 0.28);
        int woodColor = RenderUtils.toARGB(0.48f, 0.26f, 0.14f, 0.88f); // Rich warm wood
        int woodOutline = RenderUtils.toARGB(0.85f, 0.12f, 0.12f, 0.95f); // Crimson red rim band
        net.minecraft.gizmos.GizmoStyle logStyle = net.minecraft.gizmos.GizmoStyle.strokeAndFill(woodOutline, 1.8f, woodColor);

        try {
            net.minecraft.gizmos.Gizmos.cuboid(logBox, logStyle).setAlwaysOnTop();

            // 2. Drum Membrane & Knocker (Sahur stick)
            if (drumAnimation.get()) {
                double beatPhase = (System.currentTimeMillis() % 400) / 400.0;
                double stickOffset = Math.sin(beatPhase * Math.PI) * 0.15;
                AABB stickBox = new AABB(drumX - 0.05, drumY + 0.35 + stickOffset, drumZ - 0.05,
                                         drumX + 0.05, drumY + 0.55 + stickOffset, drumZ + 0.05);
                int stickColor = RenderUtils.toARGB(0.95f, 0.75f, 0.20f, 0.95f); // Gold striker
                net.minecraft.gizmos.GizmoStyle stickStyle = net.minecraft.gizmos.GizmoStyle.fill(stickColor);
                net.minecraft.gizmos.Gizmos.cuboid(stickBox, stickStyle).setAlwaysOnTop();
            }

            // 3. Red Aura Glow
            if (redAura.get()) {
                AABB glowBox = logBox.inflate(0.06);
                int auraColor = RenderUtils.toARGB(0.90f, 0.10f, 0.10f, 0.18f);
                net.minecraft.gizmos.GizmoStyle auraStyle = net.minecraft.gizmos.GizmoStyle.stroke(auraColor, 1.2f);
                net.minecraft.gizmos.Gizmos.cuboid(glowBox, auraStyle).setAlwaysOnTop();
            }

            // 4. Billboard Tag: "TUNG TUNG SAHUR"
            BlockPos tagPos = BlockPos.containing(drumX, drumY + 0.65, drumZ);
            int tagTextColor = RenderUtils.toARGB(1.0f, 0.2f, 0.2f, 1.0f);
            int tagBgColor = RenderUtils.toARGB(0.0f, 0.0f, 0.0f, 0.75f);
            net.minecraft.gizmos.Gizmos.billboardTextOverBlock("Tung Tung Tung Sahur", tagPos, tagTextColor, tagBgColor, 0.85f)
                    .setAlwaysOnTop();

        } catch (Throwable ignored) {}
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.textColored(1.0f, 0.2f, 0.2f, 1.0f, "★ KozyClient Cosmetics");
        ImGui.separator();

        ImGui.text("Custom Sword Model:");
        SwordModel[] swords = SwordModel.values();
        for (SwordModel s : swords) {
            boolean isCur = (selectedSwordModel.get() == s.ordinal());
            if (ImGui.radioButton(s.label + "##sw", isCur)) {
                selectedSwordModel.set(s.ordinal());
            }
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.text("Character / Companion Skin:");
        CharacterSkin[] chars = CharacterSkin.values();
        for (CharacterSkin c : chars) {
            boolean isCur = (selectedCharacter.get() == c.ordinal());
            if (ImGui.radioButton(c.label + "##ch", isCur)) {
                selectedCharacter.set(c.ordinal());
            }
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.checkbox("In 3rd Person anzeigen", showThirdPerson);
        ImGui.checkbox("Rote Aura-Effekte aktivieren", redAura);
        ImGui.checkbox("Sahur Drum-Animation", drumAnimation);
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("selectedSwordModel", selectedSwordModel.get());
        cfg.putSetting("selectedCharacter", selectedCharacter.get());
        cfg.putSetting("showThirdPerson", showThirdPerson.get());
        cfg.putSetting("redAura", redAura.get());
        cfg.putSetting("drumAnimation", drumAnimation.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        selectedSwordModel.set(cfg.getSetting("selectedSwordModel", selectedSwordModel.get()));
        selectedCharacter.set(cfg.getSetting("selectedCharacter", selectedCharacter.get()));
        showThirdPerson.set(cfg.getSetting("showThirdPerson", showThirdPerson.get()));
        redAura.set(cfg.getSetting("redAura", redAura.get()));
        drumAnimation.set(cfg.getSetting("drumAnimation", drumAnimation.get()));
    }
}
