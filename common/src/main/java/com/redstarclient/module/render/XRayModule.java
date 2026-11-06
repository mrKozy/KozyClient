// ☭ Red Star Client — XRayModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import com.redstarclient.util.BlockUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Makes non-whitelisted blocks invisible to reveal resources,
 * AND renders 3D glowing outline boxes directly around valuable ores and chests.
 */
public class XRayModule extends Module {

    private final Set<Block> visibleBlocks = new HashSet<>(BlockUtils.getDefaultXRayBlocks());

    // Per-ore toggles for the config panel
    private final ImBoolean showDiamond   = new ImBoolean(true);
    private final ImBoolean showIron      = new ImBoolean(true);
    private final ImBoolean showGold      = new ImBoolean(true);
    private final ImBoolean showCopper    = new ImBoolean(true);
    private final ImBoolean showCoal      = new ImBoolean(true);
    private final ImBoolean showLapis     = new ImBoolean(true);
    private final ImBoolean showRedstone  = new ImBoolean(true);
    private final ImBoolean showEmerald   = new ImBoolean(true);
    private final ImBoolean showDebris    = new ImBoolean(true);
    private final ImBoolean showChests    = new ImBoolean(true);
    private final ImBoolean showSpawners  = new ImBoolean(true);
    private final ImBoolean drawBoxes     = new ImBoolean(true);
    private final ImInt     scanRadius    = new ImInt(20);

    // Cached ore list for 3D box rendering
    private static class OreBox {
        final BlockPos pos;
        final int color;
        OreBox(BlockPos pos, int color) { this.pos = pos; this.color = color; }
    }
    private final List<OreBox> cachedOreBoxes = new ArrayList<>();
    private int tickCounter = 0;

    public XRayModule() {
        super("X-Ray", "Reveals ores through terrain with 3D highlighted boxes and block culling.", ModuleCategory.RENDER, GLFW.GLFW_KEY_X);
    }

    @Override
    public void onEnable() {
        rebuildChunks();
        scanOres();
    }

    @Override
    public void onDisable() {
        rebuildChunks();
        synchronized (cachedOreBoxes) {
            cachedOreBoxes.clear();
        }
    }

    @Override
    public void onTick(Minecraft mc) {
        if (!isEnabled()) return;
        tickCounter++;
        // Refresh scanned ore blocks twice a second (every 10 ticks)
        if (tickCounter % 10 == 0) {
            scanOres();
        }
    }

    private void scanOres() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || !drawBoxes.get()) return;

        BlockPos playerPos = mc.player.blockPosition();
        int r = Math.min(Math.max(scanRadius.get(), 8), 32);

        List<OreBox> found = new ArrayList<>();
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();

        int px = playerPos.getX();
        int py = playerPos.getY();
        int pz = playerPos.getZ();

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                int y = py + dy;
                if (y < mc.level.getMinY() || y > mc.level.getMaxY()) continue;
                for (int dz = -r; dz <= r; dz++) {
                    mpos.set(px + dx, y, pz + dz);
                    BlockState state = mc.level.getBlockState(mpos);
                    Block b = state.getBlock();
                    if (state.isAir()) continue;

                    int color = getOreColor(b);
                    if (color != 0) {
                        found.add(new OreBox(mpos.immutable(), color));
                    }
                }
            }
        }

        synchronized (cachedOreBoxes) {
            cachedOreBoxes.clear();
            cachedOreBoxes.addAll(found);
        }
    }

    private int getOreColor(Block b) {
        if (showDiamond.get() && (b == Blocks.DIAMOND_ORE || b == Blocks.DEEPSLATE_DIAMOND_ORE))
            return RenderUtils.toARGB(0.0f, 0.95f, 1.0f, 1.0f); // Bright Cyan
        if (showEmerald.get() && (b == Blocks.EMERALD_ORE || b == Blocks.DEEPSLATE_EMERALD_ORE))
            return RenderUtils.toARGB(0.0f, 1.0f, 0.35f, 1.0f); // Bright Emerald Green
        if (showGold.get() && (b == Blocks.GOLD_ORE || b == Blocks.DEEPSLATE_GOLD_ORE || b == Blocks.NETHER_GOLD_ORE))
            return RenderUtils.SOVIET_GOLD; // Soviet Gold
        if (showIron.get() && (b == Blocks.IRON_ORE || b == Blocks.DEEPSLATE_IRON_ORE))
            return RenderUtils.toARGB(0.9f, 0.75f, 0.65f, 1.0f); // Iron
        if (showCopper.get() && (b == Blocks.COPPER_ORE || b == Blocks.DEEPSLATE_COPPER_ORE))
            return RenderUtils.toARGB(0.95f, 0.45f, 0.25f, 1.0f); // Copper
        if (showRedstone.get() && (b == Blocks.REDSTONE_ORE || b == Blocks.DEEPSLATE_REDSTONE_ORE))
            return RenderUtils.toARGB(1.0f, 0.15f, 0.15f, 1.0f); // Red
        if (showLapis.get() && (b == Blocks.LAPIS_ORE || b == Blocks.DEEPSLATE_LAPIS_ORE))
            return RenderUtils.toARGB(0.15f, 0.35f, 1.0f, 1.0f); // Lapis Blue
        if (showDebris.get() && (b == Blocks.ANCIENT_DEBRIS))
            return RenderUtils.toARGB(0.70f, 0.40f, 0.25f, 1.0f); // Netherite Brown
        if (showCoal.get() && (b == Blocks.COAL_ORE || b == Blocks.DEEPSLATE_COAL_ORE))
            return RenderUtils.toARGB(0.40f, 0.40f, 0.40f, 1.0f); // Coal Charcoal
        if (showChests.get() && (b == Blocks.CHEST || b == Blocks.TRAPPED_CHEST))
            return RenderUtils.toARGB(1.0f, 0.85f, 0.1f, 1.0f);
        if (showSpawners.get() && (b == Blocks.SPAWNER))
            return RenderUtils.toARGB(0.65f, 0.20f, 0.95f, 1.0f); // Spawner Violet
        return 0;
    }

    /** Called from 3D world render pass — renders glowing 3D wireframe boxes on all ores. */
    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        if (!isEnabled() || !drawBoxes.get()) return;

        List<OreBox> boxes;
        synchronized (cachedOreBoxes) {
            if (cachedOreBoxes.isEmpty()) return;
            boxes = new ArrayList<>(cachedOreBoxes);
        }

        for (OreBox box : boxes) {
            BlockPos p = box.pos;
            AABB aabb = new AABB(p.getX(), p.getY(), p.getZ(),
                                 p.getX() + 1.0, p.getY() + 1.0, p.getZ() + 1.0);
            RenderUtils.drawBox3D(poseStack, camera, aabb, box.color);
        }
    }

    /** Called by the BlockRenderManagerMixin to decide if a block should render. */
    public boolean isBlockVisible(Block block) {
        return visibleBlocks.contains(block);
    }

    private void rebuildChunks() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.levelRenderer != null) mc.levelRenderer.allChanged();
    }

    private void rebuildVisibleSet() {
        visibleBlocks.clear();
        if (showDiamond.get())  { visibleBlocks.add(Blocks.DIAMOND_ORE);    visibleBlocks.add(Blocks.DEEPSLATE_DIAMOND_ORE); }
        if (showIron.get())     { visibleBlocks.add(Blocks.IRON_ORE);       visibleBlocks.add(Blocks.DEEPSLATE_IRON_ORE); }
        if (showGold.get())     { visibleBlocks.add(Blocks.GOLD_ORE);       visibleBlocks.add(Blocks.DEEPSLATE_GOLD_ORE); }
        if (showCopper.get())   { visibleBlocks.add(Blocks.COPPER_ORE);     visibleBlocks.add(Blocks.DEEPSLATE_COPPER_ORE); }
        if (showCoal.get())     { visibleBlocks.add(Blocks.COAL_ORE);       visibleBlocks.add(Blocks.DEEPSLATE_COAL_ORE); }
        if (showLapis.get())    { visibleBlocks.add(Blocks.LAPIS_ORE);      visibleBlocks.add(Blocks.DEEPSLATE_LAPIS_ORE); }
        if (showRedstone.get()) { visibleBlocks.add(Blocks.REDSTONE_ORE);   visibleBlocks.add(Blocks.DEEPSLATE_REDSTONE_ORE); }
        if (showEmerald.get())  { visibleBlocks.add(Blocks.EMERALD_ORE);    visibleBlocks.add(Blocks.DEEPSLATE_EMERALD_ORE); }
        if (showDebris.get())   { visibleBlocks.add(Blocks.ANCIENT_DEBRIS); visibleBlocks.add(Blocks.NETHER_GOLD_ORE); }
        if (showChests.get())   { visibleBlocks.add(Blocks.CHEST);          visibleBlocks.add(Blocks.TRAPPED_CHEST); }
        if (showSpawners.get()) { visibleBlocks.add(Blocks.SPAWNER); }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Draw Glowing 3D Boxes##xr", drawBoxes);
        ImGui.sliderInt("Box Scan Radius##xr", scanRadius.getData(), 8, 32);

        ImGui.separator();
        ImGui.text("Resource Whitelist:");

        boolean changed = false;
        changed |= ImGui.checkbox("Diamond##xr",   showDiamond);
        ImGui.sameLine(); changed |= ImGui.checkbox("Iron##xr",     showIron);
        changed |= ImGui.checkbox("Gold##xr",      showGold);
        ImGui.sameLine(); changed |= ImGui.checkbox("Copper##xr",   showCopper);
        changed |= ImGui.checkbox("Coal##xr",      showCoal);
        ImGui.sameLine(); changed |= ImGui.checkbox("Lapis##xr",    showLapis);
        changed |= ImGui.checkbox("Redstone##xr",  showRedstone);
        ImGui.sameLine(); changed |= ImGui.checkbox("Emerald##xr",  showEmerald);
        changed |= ImGui.checkbox("Anc. Debris##xr", showDebris);
        ImGui.sameLine(); changed |= ImGui.checkbox("Chests##xr",   showChests);
        changed |= ImGui.checkbox("Spawners##xr",  showSpawners);

        if (changed) {
            rebuildVisibleSet();
            rebuildChunks();
            scanOres();
        }
    }

    @Override
    public ModuleConfig toConfig() {
        ModuleConfig cfg = super.toConfig();
        cfg.putSetting("showDiamond", showDiamond.get());
        cfg.putSetting("showIron", showIron.get());
        cfg.putSetting("showGold", showGold.get());
        cfg.putSetting("showCopper", showCopper.get());
        cfg.putSetting("showCoal", showCoal.get());
        cfg.putSetting("showLapis", showLapis.get());
        cfg.putSetting("showRedstone", showRedstone.get());
        cfg.putSetting("showEmerald", showEmerald.get());
        cfg.putSetting("showDebris", showDebris.get());
        cfg.putSetting("showChests", showChests.get());
        cfg.putSetting("showSpawners", showSpawners.get());
        cfg.putSetting("drawBoxes", drawBoxes.get());
        cfg.putSetting("scanRadius", scanRadius.get());
        return cfg;
    }

    @Override
    public void fromConfig(ModuleConfig cfg) {
        super.fromConfig(cfg);
        showDiamond.set(cfg.getSetting("showDiamond", showDiamond.get()));
        showIron.set(cfg.getSetting("showIron", showIron.get()));
        showGold.set(cfg.getSetting("showGold", showGold.get()));
        showCopper.set(cfg.getSetting("showCopper", showCopper.get()));
        showCoal.set(cfg.getSetting("showCoal", showCoal.get()));
        showLapis.set(cfg.getSetting("showLapis", showLapis.get()));
        showRedstone.set(cfg.getSetting("showRedstone", showRedstone.get()));
        showEmerald.set(cfg.getSetting("showEmerald", showEmerald.get()));
        showDebris.set(cfg.getSetting("showDebris", showDebris.get()));
        showChests.set(cfg.getSetting("showChests", showChests.get()));
        showSpawners.set(cfg.getSetting("showSpawners", showSpawners.get()));
        drawBoxes.set(cfg.getSetting("drawBoxes", drawBoxes.get()));
        scanRadius.set(cfg.getSetting("scanRadius", scanRadius.get()));
        rebuildVisibleSet();
    }
}
