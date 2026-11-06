// ☭ Red Star Client — HoleESPModule (from Wurst 7 / Crystal PvP)
package com.redstarclient.module.combat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.module.render.StreamProofModule;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

/**
 * HoleESP (from Wurst 7 / Crystal PvP):
 * Highlights safe 1x1 Bedrock and Obsidian blast-proof holes in 3D
 * for survival during Crystal PvP.
 */
public class HoleESPModule extends Module {

    private final ImInt     radius      = new ImInt(8);
    private final ImBoolean showBedrock = new ImBoolean(true);
    private final ImBoolean showObsidian= new ImBoolean(true);

    public HoleESPModule() {
        super("HoleESP", "Hebt sichere 1x1 Loecher (Bedrock & Obsidian) fuer Crystal PvP in 3D hervor (Wurst 7).", ModuleCategory.CRYSTAL, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (StreamProofModule.shouldHideVisuals()) return;

        BlockPos playerPos = mc.player.blockPosition();
        int r = radius.get();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -3; dy <= 2; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);

                    // Hole must have air at feet and head
                    if (!mc.level.getBlockState(pos).isAir() || !mc.level.getBlockState(pos.above()).isAir()) {
                        continue;
                    }

                    // Check floor and 4 horizontal walls
                    BlockState downState = mc.level.getBlockState(pos.below());
                    BlockState nState = mc.level.getBlockState(pos.north());
                    BlockState sState = mc.level.getBlockState(pos.south());
                    BlockState eState = mc.level.getBlockState(pos.east());
                    BlockState wState = mc.level.getBlockState(pos.west());

                    boolean isBedrockHole = downState.is(Blocks.BEDROCK) &&
                                            nState.is(Blocks.BEDROCK) &&
                                            sState.is(Blocks.BEDROCK) &&
                                            eState.is(Blocks.BEDROCK) &&
                                            wState.is(Blocks.BEDROCK);

                    if (isBedrockHole && showBedrock.get()) {
                        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                                            pos.getX() + 1.0, pos.getY() + 0.2, pos.getZ() + 1.0);
                        RenderUtils.drawBox3D(poseStack, camera, box, RenderUtils.PASSIVE_GREEN);
                        continue;
                    }

                    boolean isObsidianHole = isBlastResistant(downState) &&
                                             isBlastResistant(nState) &&
                                             isBlastResistant(sState) &&
                                             isBlastResistant(eState) &&
                                             isBlastResistant(wState);

                    if (isObsidianHole && showObsidian.get()) {
                        AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                                            pos.getX() + 1.0, pos.getY() + 0.2, pos.getZ() + 1.0);
                        RenderUtils.drawBox3D(poseStack, camera, box, RenderUtils.SOVIET_GOLD);
                    }
                }
            }
        }
    }

    private boolean isBlastResistant(BlockState state) {
        return state.is(Blocks.BEDROCK) ||
               state.is(Blocks.OBSIDIAN) ||
               state.is(Blocks.CRYING_OBSIDIAN) ||
               state.is(Blocks.ENDER_CHEST) ||
               state.is(Blocks.RESPAWN_ANCHOR) ||
               state.is(Blocks.NETHERITE_BLOCK);
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.sliderInt("Such-Radius##hesp", radius.getData(), 3, 16);
        ImGui.checkbox("Bedrock-Loecher anzeigen (Gruen - Unzerstoerbar)##hesp", showBedrock);
        ImGui.checkbox("Obsidian-Loecher anzeigen (Gold - Sicher)##hesp", showObsidian);
        ImGui.spacing();
        ImGui.textDisabled("Unverzichtbar fuer Crystal PvP: zeigt sichere Bunker-Positionen.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("radius", radius.get());
        cfg.putSetting("showBedrock", showBedrock.get());
        cfg.putSetting("showObsidian", showObsidian.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        radius.set(cfg.getSetting("radius", radius.get()));
        showBedrock.set(cfg.getSetting("showBedrock", showBedrock.get()));
        showObsidian.set(cfg.getSetting("showObsidian", showObsidian.get()));
    }
}
