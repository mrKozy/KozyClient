// ☭ Red Star Client — TrajectoriesModule
package com.redstarclient.module.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.module.Module;
import com.redstarclient.module.ModuleCategory;
import com.redstarclient.render.RenderUtils;
import imgui.ImGui;
import imgui.type.ImBoolean;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Predicts and renders 3D physical trajectory paths for arrows, pearls, potions, throwables, and wind charges.
 * Originates directly and accurately from the crosshair in 3D space.
 */
public class TrajectoriesModule extends Module {

    private final ImBoolean showHitbox = new ImBoolean(true);

    public TrajectoriesModule() {
        super("Trajectories", "Draws physical flight arcs and landing points starting from crosshair.", ModuleCategory.RENDER, -1);
    }

    @Override
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        ItemStack held = mc.player.getMainHandItem();
        if (held.isEmpty()) held = mc.player.getOffhandItem();
        if (held.isEmpty()) return;

        Item item = held.getItem();
        float velocity;
        float gravity;
        float drag = 0.99f;

        if (item instanceof BowItem) {
            int useDuration = mc.player.getTicksUsingItem();
            float bowPower = mc.player.isUsingItem() ? BowItem.getPowerForTime(useDuration) : 1.0f;
            velocity = bowPower * 3.0f;
            gravity = 0.05f;
        } else if (item instanceof CrossbowItem) {
            velocity = 3.15f;
            gravity = 0.05f;
        } else if (item instanceof EnderpearlItem || item instanceof SnowballItem || item instanceof EggItem) {
            velocity = 1.5f;
            gravity = 0.03f;
        } else if (item instanceof SplashPotionItem || item instanceof LingeringPotionItem) {
            velocity = 0.5f;
            gravity = 0.05f;
        } else if (item instanceof TridentItem) {
            velocity = 2.5f;
            gravity = 0.05f;
        } else if (item.getDescriptionId().contains("wind_charge")) {
            velocity = 1.5f;
            gravity = 0.02f;
        } else {
            return;
        }

        // Get dead-center crosshair ray from the active camera
        Vec3 look = mc.player.getViewVector(tickDelta);
        Vec3 start = camera.getPosition().add(look.scale(0.3));

        Vec3 motion = look.scale(velocity);

        Vec3 current = start;
        List<Vec3> points = new ArrayList<>();
        points.add(current);

        HitResult hit = null;

        for (int i = 0; i < 150; i++) {
            Vec3 next = current.add(motion);

            // Raycast block collision
            BlockHitResult blockHit = mc.level.clip(new ClipContext(
                    current, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player
            ));

            if (blockHit.getType() != HitResult.Type.MISS) {
                points.add(blockHit.getLocation());
                hit = blockHit;
                break;
            }

            points.add(next);
            current = next;

            // Apply gravity and drag
            motion = motion.scale(drag).subtract(0, gravity, 0);
        }

        // Draw 3D trajectory line segments
        int lineColor = RenderUtils.SOVIET_GOLD;
        for (int i = 0; i < points.size() - 1; i++) {
            RenderUtils.drawLine3D(poseStack, camera, points.get(i), points.get(i + 1), lineColor);
        }

        // Draw hit box at landing point
        if (hit != null && showHitbox.get()) {
            Vec3 hitPos = hit.getLocation();
            AABB hitBox = new AABB(
                    hitPos.x - 0.25, hitPos.y - 0.25, hitPos.z - 0.25,
                    hitPos.x + 0.25, hitPos.y + 0.25, hitPos.z + 0.25
            );
            RenderUtils.drawBox3D(poseStack, camera, hitBox, RenderUtils.SOVIET_RED);
        }
    }

    @Override
    public void renderImGuiConfig() {
        ImGui.checkbox("Aufschlagspunkt markieren##tr", showHitbox);
        ImGui.spacing();
        ImGui.textDisabled("Startet exakt mittig am Fadenkreuz.");
        ImGui.textDisabled("Unterstuetzt Boegen, Armbrueste, Perlen, Schneebaelle, Traenke & Wind Charges.");
    }
}
