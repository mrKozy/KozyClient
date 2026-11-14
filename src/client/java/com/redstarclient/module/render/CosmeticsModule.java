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

    // Character Skins / Models (Authentic Anubis Designs)
    public enum CharacterSkin {
        NONE("Normal Player"),
        TUNG_TUNG_SAHUR("Tung Tung Tung Sahur"),
        SAKURA("Sakura"),
        LUNA("Luna"),
        MIKA("Mika"),
        AOI("Aoi"),
        MIKU("Miku"),
        TRALALERO("Tralalero");

        public final String label;
        CharacterSkin(String label) { this.label = label; }
    }

    private final ImInt selectedSwordModel = new ImInt(1); // 1 = Butterfly by default
    private final ImInt selectedCharacter  = new ImInt(1); // 1 = Tung Tung Sahur by default
    private final ImInt inspectKey         = new ImInt(org.lwjgl.glfw.GLFW.GLFW_KEY_F); // Default F key for inspect
    private boolean listeningForInspectKey = false;
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

    public int getInspectKey() {
        return inspectKey.get();
    }

    public void setInspectKey(int key) {
        inspectKey.set(key);
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
        // Character skin rendering is handled directly by CharacterLayer on the player entity
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
        ImGui.text("Knife Inspect Animation:");
        String keyName = listeningForInspectKey ? "Taste druecken..." : com.redstarclient.util.KeyUtils.getKeyName(inspectKey.get());
        if (ImGui.button("Inspect Hotkey: [" + keyName + "]##btnInspectKey")) {
            listeningForInspectKey = true;
        }
        if (listeningForInspectKey) {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.getWindow() != null) {
                long handle = mc.getWindow().handle();
                for (int k = org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE; k <= org.lwjgl.glfw.GLFW.GLFW_KEY_LAST; k++) {
                    if (org.lwjgl.glfw.GLFW.glfwGetKey(handle, k) == org.lwjgl.glfw.GLFW.GLFW_PRESS) {
                        inspectKey.set(k);
                        listeningForInspectKey = false;
                        break;
                    }
                }
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
        cfg.putSetting("inspectKey", inspectKey.get());
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
        inspectKey.set(cfg.getSetting("inspectKey", inspectKey.get()));
        showThirdPerson.set(cfg.getSetting("showThirdPerson", showThirdPerson.get()));
        redAura.set(cfg.getSetting("redAura", redAura.get()));
        drumAnimation.set(cfg.getSetting("drumAnimation", drumAnimation.get()));
    }
}
