# KozyClient (Minecraft 1.21.4)

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.4-brightgreen.svg?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Modloader-Fabric%20%7C%20NeoForge-blue.svg?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat-square)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg?style=flat-square)](LICENSE)
[![Author](https://img.shields.io/badge/Author-mrKozy-black.svg?style=flat-square&logo=github)](https://github.com/mrKozy)

> Next-generation, multi-loader utility client for **Minecraft 1.21.4** powered by **Dear ImGui**, providing high-performance in-game overlays, responsive modular systems, and clean visuals.
>
> **Made by [mr.Kozy](https://github.com/mrKozy)**

---

## Highlights

- **MultiLoader Architecture**: Clean separation between `common` business logic and loader-specific mixins for both **Fabric** and **NeoForge**.
- **Dear ImGui Native GUI**: Hardware-accelerated, zero-lag ClickGUI with full mouse interaction, color pickers, sliders, and keybind listeners.
- **Over 40 Modular Features**: Comprehensive suite covering Combat, Movement, Render / Visuals, Player automation, and HUD diagnostics.
- **Draggable HUD Editor**: On-screen widget placement for Coordinates, FPS, Armor status, CPS tracker, Ping indicator, and Spotify HUD.

---

## Default Controls

| Key | Action |
|:---|:---|
| `Right Shift` | Toggle ImGui ClickGUI / Main Menu |
| `C` (Hold) | Smooth Optical Zoom |
| `R` | Toggle KillAura |
| `G` | Toggle Fly |
| `X` | Toggle X-Ray |
| `B` | Toggle ESP |
| `F` | Toggle FullBright |
| `H` | Toggle Coords & FPS Overlay |

*All keybinds, colors, and module parameters are fully customizable via the ClickGUI or in `config/redstarclient.json`.*

---

## Feature Directory

### Combat
- **KillAura**: Smart target acquisition with configurable range, angle limits, and humanized CPS throttling.
- **TriggerBot**: Automatically initiates attacks whenever a valid entity enters crosshair focus.
- **AutoTotem**: Real-time offhand replenishment upon Totem of Undying consumption.
- **BowAimbot**: Ballistic trajectory solver predicting gravity, entity velocity, and lead angles.
- **Velocity**: Reduces or cancels horizontal and vertical knockback impulses.

### Movement
- **Fly**: Smooth creative-style flight in survival mode with configurable propulsion speed.
- **Speed**: Horizontal velocity booster with ground and airborne acceleration profiles.
- **Jesus**: Walk seamlessly across water and lava surfaces.
- **Spider**: Climb sheer vertical obstacles upon wall contact.
- **SafeWalk**: Edge-detection preventing accidental falls off block borders.
- **NoFall**: Intercepts and alters fall packets to mitigate kinetic impact.
- **Step**: Smoothly steps up blocks (up to 2.5 blocks) without manual jumping.

### Render & Visuals
- **ESP**: 2D bounding boxes, 3D wireframes, and glowing player/mob silhouettes with customizable team colors.
- **Tracers**: Clean vector rays projected from screen center toward nearby entities.
- **X-Ray**: Clear block filter highlighting valuable ores (Diamond, Ancient Debris, Emerald, Gold, Iron) and containers.
- **FullBright**: Gamma override ensuring optimal visibility in dark caverns and during nighttime.
- **StorageESP**: Distinct highlighting for Chests, Ender Chests, Shulker Boxes, Furnaces, Hoppers, and Barrels.
- **Freecam**: Detached orbital spectator camera for reconnaissance while retaining your physical avatar.
- **NoHurtCam**: Disables disorienting camera shake upon taking damage.
- **MiniMap**: Live 2D overhead radar display showing player orientation, chunks, and entity positions.

### Player & Automation
- **FastPlace**: Removes the default delay between block placement actions.
- **Scaffold**: Automatic bridging directly beneath the player while moving across void or drop-offs.
- **AutoEat**: Monitors hunger and automatically consumes optimal inventory food items.
- **AntiAFK**: Micro-inputs and rotations preventing idle disconnection.
- **AutoReconnect**: Automatic reconnection loop targeting the last visited server upon network drops.

### HUD & Info
- **ArmorHUD**: Real-time visual display of equipped armor durability and held weapons.
- **CPS Counter**: Rolling-window clicks-per-second monitor for left and right mouse buttons.
- **Coords & FPS**: Compact diagnostic widget displaying exact coordinates, biome, facing direction, and frame rates.
- **Ping Tracker**: Color-coded network latency metric.
- **InventoryViewer**: Floating HUD displaying internal container / inventory layout.
- **SpotifyHUD**: Desktop media hook displaying currently playing artist and track.

---

## Project Structure

```
KozyClient (branch: 1.21.4)
├── common/                  # Shared mod logic (platform-agnostic)
│   ├── config/              # JSON module serialization & configuration
│   ├── event/               # Event bus & tick/packet/render event listeners
│   ├── gui/                 # ImGui ClickGUI, HUD editor & widget components
│   ├── module/              # Combat, movement, player, render & info modules
│   ├── render/              # ESP, tracers, line drawing & matrix utilities
│   ├── theme/               # Modern dark color palettes & styling
│   └── util/                # Block, math, player & inventory helpers
├── fabric/                  # Fabric Loader hooks & mixins (MC 1.21.4)
├── neoforge/                # NeoForge mod hooks & mixins (MC 1.21.4)
├── gradle/wrapper/          # Gradle 9 wrapper binaries & configuration
├── build.gradle             # Root multi-project build script
├── settings.gradle          # Module definitions
└── gradle.properties        # Version pins & dependencies
```

---

## Building from Source

### Prerequisites
- Java Development Kit (JDK) 21 or newer
- Git

### Build Instructions
```bash
# Clone the repository and switch to the 1.21.4 branch
git clone -b 1.21.4 https://github.com/mrKozy/KozyClient.git
cd KozyClient

# Build both Fabric and NeoForge artifacts
./gradlew build

# Or build specifically for Fabric:
./gradlew :fabric:build

# Or build specifically for NeoForge:
./gradlew :neoforge:build
```
Compiled artifacts will be located in `fabric/build/libs/` and `neoforge/build/libs/`.

---

## Author & Credits

- **Developer**: [mrKozy](https://github.com/mrKozy)
- **License**: Released under the [MIT License](LICENSE).
