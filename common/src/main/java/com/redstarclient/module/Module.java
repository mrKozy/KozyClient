// ☭ Red Star Client — Module
package com.redstarclient.module;

import com.mojang.blaze3d.vertex.PoseStack;
import com.redstarclient.config.ModuleConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

/**
 * Abstract base class for all Red Star Client modules.
 * Subclasses must implement {@link #renderImGuiConfig()} to draw their
 * Dear ImGui settings panel.
 */
public abstract class Module {

    protected final String name;
    protected final String description;
    protected final ModuleCategory category;

    protected boolean enabled  = false;
    protected int     keybind;

    /** Convenience reference — set lazily on first use. */
    protected Minecraft mc;

    protected Module(String name, String description, ModuleCategory category, int defaultKeybind) {
        this.name        = name;
        this.description = description;
        this.category    = category;
        this.keybind     = defaultKeybind;
    }

    // ── Lifecycle ────────────────────────────────────────────────────────────

    /** Called when the module is enabled. */
    public void onEnable()  {}

    /** Called when the module is disabled. */
    public void onDisable() {}

    /** Called every game tick while the module is enabled. */
    public void onTick(Minecraft mc) {}

    /** Called during the 3D world render pass while the module is enabled. */
    public void onRenderWorld(GuiGraphics graphics, float tickDelta) {}

    /**
     * Called during the world render pass with proper 3D context.
     * Use this for 3D world-space ESP, tracer lines, etc.
     */
    public void onRenderWorld3D(PoseStack poseStack, Camera camera, float tickDelta) {}

    /** Called during the 2D HUD render pass while the module is enabled. */
    public void onRenderHud(GuiGraphics graphics, float tickDelta) {}

    /** Draw this module's ImGui configuration widgets (called from the GUI). */
    public abstract void renderImGuiConfig();

    // ── Toggle ───────────────────────────────────────────────────────────────

    public void toggle() {
        setEnabled(!enabled);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        try {
            if (enabled) {
                this.mc = Minecraft.getInstance();
                onEnable();
            } else {
                onDisable();
            }
        } catch (Throwable t) {
            System.err.println("[Red Star Client] Error changing state for " + name + ": " + t.getMessage());
        }
    }

    // ── Config persistence ───────────────────────────────────────────────────

    public ModuleConfig toConfig() {
        ModuleConfig cfg = new ModuleConfig(enabled, keybind);
        return cfg;
    }

    public void fromConfig(ModuleConfig cfg) {
        this.keybind = cfg.keybind;
        setEnabled(cfg.enabled);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String          getName()        { return name; }
    public String          getDescription() { return description; }
    public ModuleCategory  getCategory()    { return category; }
    public boolean         isEnabled()      { return enabled; }
    public int             getKeybind()     { return keybind; }
    public void            setKeybind(int k){ this.keybind = k; }
}
