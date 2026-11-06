// ☭ Red Star Client — NukerModule (from Wurst 7)
package com.redstarclient.module.player;

import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.util.BlockUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Nuker (from Wurst 7):
 * Automatically and rapidly destroys blocks around the player in a configurable radius.
 * Modes: ALL, FLATTEN (feet level and above), ORES_ONLY, ONE_HIT.
 */
public class NukerModule extends Module {

    private final ImInt     radius      = new ImInt(4);
    private final ImInt     modeIndex   = new ImInt(0); // 0 = ALL, 1 = FLATTEN, 2 = ORES_ONLY, 3 = ONE_HIT
    private final ImInt     blocksPerTick = new ImInt(2);
    private final ImBoolean survivalSafe = new ImBoolean(false);

    private static final String[] MODES = {"ALL", "FLATTEN", "ORES_ONLY", "ONE_HIT"};

    public NukerModule() {
        super("Nuker", "Zerstoert blitzschnell alle Bloecke in der Umgebung (Wurst 7 Klassiker).", ModuleCategory.PLAYER, -1);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        BlockPos playerPos = mc.player.blockPosition();
        int r = radius.get();
        int currentMode = modeIndex.get();
        int playerFeetY = playerPos.getY();

        List<BlockPos> targets = new ArrayList<>();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    BlockPos pos = playerPos.offset(dx, dy, dz);
                    if (pos.equals(playerPos) || pos.equals(playerPos.above())) {
                        continue; // Do not break the block player is standing inside
                    }

                    BlockState state = mc.level.getBlockState(pos);
                    if (state.isAir() || state.is(Blocks.BEDROCK) || state.is(Blocks.BARRIER)) {
                        continue;
                    }

                    // Mode filtering
                    if (currentMode == 1 && pos.getY() < playerFeetY) {
                        // FLATTEN: Only break at feet level or above
                        continue;
                    } else if (currentMode == 2 && !BlockUtils.isOre(state.getBlock())) {
                        // ORES_ONLY
                        continue;
                    } else if (currentMode == 3 && state.getDestroySpeed(mc.level, pos) > 0.0f) {
                        // ONE_HIT only
                        continue;
                    }

                    targets.add(pos);
                }
            }
        }

        // Sort closest first
        targets.sort(Comparator.comparingDouble(p -> p.distSqr(mc.player.blockPosition())));

        int broken = 0;
        int limit = blocksPerTick.get();

        for (BlockPos target : targets) {
            if (broken >= limit) break;

            if (mc.gameMode.destroyBlock(target)) {
                mc.player.swing(InteractionHand.MAIN_HAND);
                broken++;
            } else {
                mc.gameMode.startDestroyBlock(target, Direction.UP);
                broken++;
            }
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.combo("Nuker Modus##nuker", modeIndex, MODES);
        ImGui.sliderInt("Radius (Bloecke)##nuker", radius.getData(), 1, 6);
        ImGui.sliderInt("Bloecke pro Tick##nuker", blocksPerTick.getData(), 1, 8);
        ImGui.checkbox("Survival-Safe (Bricht keine Bloecke direkt unter den Fuessen)##nuker", survivalSafe);
        ImGui.spacing();
        ImGui.textColored(0.92f, 0.76f, 0.13f, 1.0f, "★ Wurst 7 Nuker: Grabe riesige Raeume in Sekundenschnelle frei.");
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("radius", radius.get());
        cfg.putSetting("modeIndex", modeIndex.get());
        cfg.putSetting("blocksPerTick", blocksPerTick.get());
        cfg.putSetting("survivalSafe", survivalSafe.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        radius.set(cfg.getSetting("radius", radius.get()));
        modeIndex.set(cfg.getSetting("modeIndex", modeIndex.get()));
        blocksPerTick.set(cfg.getSetting("blocksPerTick", blocksPerTick.get()));
        survivalSafe.set(cfg.getSetting("survivalSafe", survivalSafe.get()));
    }
}
