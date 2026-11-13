// ☭ Red Star Client — ModuleManager
package com.redstarclient.module;

import com.redstarclient.module.combat.*;
import com.redstarclient.module.info.*;
import com.redstarclient.module.inventory.*;
import com.redstarclient.module.movement.*;
import com.redstarclient.module.player.*;
import com.redstarclient.module.render.*;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registry and dispatcher for all Red Star Client modules.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    // ── Registration ─────────────────────────────────────────────────────────

    public void register(Module m) {
        modules.add(m);
    }

    /** Registers all 40+ built-in modules. */
    public void registerDefaults() {
        // ⚔️ Combat / Sword PvP
        register(new AntiKickModule());
        register(new AimAssistModule());
        register(new KillAuraModule());
        register(new TriggerBotModule());
        register(new SilentAimModule());
        register(new AutoShieldBreakerModule());
        register(new ShieldDrainModule());
        register(new WTapModule());
        register(new HitboxesModule());
        register(new BackTrackModule());
        register(new BowAimbotModule());
        register(new VelocityModule());
        register(new AutoArmorModule());
        register(new CriticalsModule());
        register(new ReachModule());
        register(new ReachDisplayModule());
        register(new ComboCounterModule());
        register(new AutoGapModule());
        register(new AutoPearlModule());
        register(new TeamCheckModule());
        register(new AutoSwordModule());
        register(new AutoLeaveModule());

        // 💎 Crystal PvP
        register(new AutoCrystalModule());
        register(new AutoHitCrystalModule());
        register(new AutoTotemModule());
        register(new SurroundModule());
        register(new AutoCityModule());
        register(new AnchorMacroModule());
        register(new AutoElytraModule());
        register(new HoleESPModule());

        // 🔨 Mace PvP (1.21 & 26.3)
        register(new AutoMaceModule());
        register(new BreachSwapModule());
        register(new AutoWindChargeModule());
        register(new WindChargeAuraModule());
        register(new AutoPearlCatchModule());

        // 🏹 Cart & Spear PvP
        register(new AutoCartModule());
        register(new CartRefillModule());
        register(new XbowCartModule());
        register(new SpearModule());

        // 🧪 Pot & UHC PvP
        register(new AutoPotModule());
        register(new PotionRefillModule());

        // 🏃 Movement
        register(new FlyModule());
        register(new SpeedModule());
        register(new JesusModule());
        register(new SpiderModule());
        register(new SafeWalkModule());
        register(new FastBridgeModule());
        register(new NoFallModule());
        register(new StepModule());
        register(new AutoSprintModule());
        register(new ToggleSneakModule());
        register(new BunnyHopModule());
        register(new NoSlowModule());
        register(new ParkourModule());
        register(new FastLadderModule());
        register(new GlideModule());
        register(new AntiCobwebModule());
        register(new InventoryMoveModule());
        register(new BoatFlyModule());
        register(new JetpackModule());
        register(new DolphinModule());

        // 👁️ Render & Visuals
        register(new StreamProofModule());
        register(new AntiBlindModule());
        register(new CrosshairModule());
        register(new ESPModule());
        register(new TargetRendererModule());
        register(new BaseFinderModule());
        register(new AncientDebrisFinderModule());
        register(new MotionBlurModule());
        register(new TracerModule());
        register(new XRayModule());
        register(new FullBrightModule());
        register(new StorageESPModule());
        register(new FreecamModule());
        register(new NoHurtCamModule());
        register(new ZoomModule());
        register(new NameTagsModule());
        register(new MiniMapModule());
        register(new BreadcrumbsModule());
        register(new TrajectoriesModule());
        register(new BlockOutlineModule());
        register(new ParticleMultiplierModule());
        register(new TNTTimerModule());
        register(new TimeChangerModule());
        register(new WeatherChangerModule());
        register(new NoRenderModule());
        register(new SwordAnimationModule());
        register(new CameraClipModule());
        register(new TrueSightModule());
        register(new SkinDerpModule());
        register(new SoundESPModule());
        register(new StaffESPModule());
        register(new SusChunkESPModule());
        register(new AsianHatModule());
        register(new CosmeticsModule());
        register(new TunnelESPModule());

        // 🛠️ Player & World
        register(new FastPlaceModule());
        register(new ScaffoldModule());
        register(new AutoEatModule());
        register(new AntiAFKModule());
        register(new AutoReconnectModule());
        register(new ChatFilterModule());
        register(new AutoFishModule());
        register(new AutoGGModule());
        register(new AutoTunnelModule());
        register(new AutoRespawnModule());
        register(new FastBreakModule());
        register(new AutoFarmModule());
        register(new AutoSignModule());
        register(new NukerModule());
        register(new TwerkModule());
        register(new BlinkModule());

        // 📊 Info Overlays
        register(new VisualProfilerModule());
        register(new ActiveModulesHUDModule());
        register(new ArmorHUDModule());
        register(new ArmorNotifierModule());
        register(new PlayerRadarHUDModule());
        register(new CPSCounterModule());
        register(new CoordsFPSModule());
        register(new PingModule());
        register(new InventoryViewerModule());
        register(new KeystrokesHUDModule());
        register(new SpotifyHUDModule());
        register(new PotionStatusHUDModule());
        register(new DirectionHUDModule());
        register(new SpeedometerModule());
        register(new ItemCounterModule());

        // 🔧 Inventory
        register(new AutoToolModule());
        register(new ItemScrollerModule());
        register(new ChestStealerModule());
        register(new FastDropModule());
    }

    // ── Lookup ───────────────────────────────────────────────────────────────

    public Module get(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) return (T) m;
        }
        return null;
    }

    public <T extends Module> boolean isEnabled(Class<T> clazz) {
        T m = get(clazz);
        return m != null && m.isEnabled();
    }

    public List<Module> getAll() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(ModuleCategory category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    // ── Dispatch ─────────────────────────────────────────────────────────────

    public void onTick(Minecraft mc) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                long start = System.nanoTime();
                try {
                    m.onTick(mc);
                } catch (Exception e) {
                    System.err.println("[RSC] Tick error in " + m.getName() + ": " + e.getMessage());
                }
                VisualProfilerModule.recordExecution(m.getName(), System.nanoTime() - start);
            }
        }
    }

    public void onRenderWorld(GuiGraphicsExtractor graphics, float tickDelta) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onRenderWorld(graphics, tickDelta); }
                catch (Exception e) { System.err.println("[RSC] RenderWorld error in " + m.getName() + ": " + e.getMessage()); }
            }
        }
    }

    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onRenderWorld3D(poseStack, camera, tickDelta); }
                catch (Exception e) { System.err.println("[RSC] RenderWorld3D error in " + m.getName() + ": " + e.getMessage()); }
            }
        }
    }

    public void onRenderHud(GuiGraphicsExtractor graphics, float tickDelta) {
        for (Module m : modules) {
            if (m.isEnabled()) {
                try { m.onRenderHud(graphics, tickDelta); }
                catch (Exception e) { System.err.println("[RSC] RenderHud error in " + m.getName() + ": " + e.getMessage()); }
            }
        }
    }

    /** Toggles any module whose keybind matches the pressed key. */
    public void onKeyPress(int key) {
        for (Module m : modules) {
            if (m.getKeybind() == key) {
                m.toggle();
            }
        }
    }
}
