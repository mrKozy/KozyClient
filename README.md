# KozyClient (Minecraft 26.2)

[![Minecraft](https://img.shields.io/badge/Minecraft-26.2-brightgreen.svg?style=flat-square)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Modloader-Fabric-blue.svg?style=flat-square)](https://fabricmc.net/)
[![Java](https://img.shields.io/badge/Java-25%2B-orange.svg?style=flat-square)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-purple.svg?style=flat-square)](LICENSE)
[![Author](https://img.shields.io/badge/Author-mrKozy-black.svg?style=flat-square&logo=github)](https://github.com/mrKozy)

> Next-generation, high-performance utility client engineered for **Minecraft 26.2 (Fabric)** with native **Dear ImGui** acceleration, modular architecture, and modern visual design.
>
> **Made by [mr.Kozy](https://github.com/mrKozy)**

---

## Versions & Branches

| Minecraft Version | Branch | Loader | Status |
|:---|:---|:---|:---|
| **26.2** | [`main`](https://github.com/mrKozy/KozyClient/tree/main) | Fabric Loader (0.19.3+) | **Current / Active** |
| **1.21.4** | [`1.21.4`](https://github.com/mrKozy/KozyClient/tree/1.21.4) | Fabric & NeoForge (MultiLoader) | Supported |

---

## Features

- **Dear ImGui Native HUD & GUI**: Smooth 60+ FPS overlay rendered natively via Dear ImGui LWJGL3 bindings with zero game thread stutter.
- **Draggable HUD Layout Editor**: Customize and position on-screen HUD widgets including Coordinates/FPS, Armor HUD, CPS tracker, Ping indicator, and Spotify HUD.
- **Custom Modules**: Extensible module system categorized into Combat, Movement, Render / Visuals, Player Automation, and Diagnostics.
- **Embedded PvP Enhancements**: Integrated low-fire, clear visual assets, and customized particle styling.

---

## Controls & Keybinds

| Key | Function |
|:---|:---|
| `Right Shift` | Toggle ImGui ClickGUI / Main Menu |
| `C` (Hold) | Smooth Optical Zoom |
| `R` | Toggle KillAura |
| `G` | Toggle Fly |
| `X` | Toggle X-Ray |
| `B` | Toggle ESP |
| `F` | Toggle FullBright |
| `H` | Toggle Coords & FPS Overlay |

*All keybinds, visual colors, and module parameters are live-configurable in-game via the ClickGUI.*

---

## Module Directory

### Combat
- **KillAura**: Autonomous entity targeting with configurable reach, field-of-view limits, and humanized CPS timing.
- **TriggerBot**: Instant attack execution whenever a hostile player or mob crosses your target cursor.
- **AutoTotem**: Instant detection and offhand replenishment when a Totem of Undying triggers.
- **BowAimbot**: Predicts arrow trajectory, target movement, and elevation drop for long-range accuracy.
- **Velocity**: Reduces or cancels horizontal and vertical knockback impulses.

### Movement
- **Fly**: Survival flight simulation with customizable acceleration and glide parameters.
- **Speed**: Horizontal velocity multiplier for ground and airborne maneuvering.
- **Jesus**: Solid liquid-surface traversal over water and lava lakes.
- **Spider**: Vertical wall-climbing impulse upon surface contact.
- **SafeWalk**: Edge-detection lock preventing falls off block perimeters.
- **NoFall**: Fall packet modification to negate fall impact damage.
- **Step**: Elevated block step-up (up to 2.5 blocks) without manual jumping.

### Render & Visuals
- **ESP**: 2D bounding boxes, 3D wireframe boxes, and highlighted silhouettes with customizable team coloring.
- **Tracers**: Clean vector guidance lines drawn from crosshair toward entities in range.
- **X-Ray**: Selective ore rendering for Ancient Debris, Diamonds, Gold, Iron, Emerald, and container blocks.
- **FullBright**: Permanent gamma override for crystal-clear night and underground visibility.
- **StorageESP**: Color-coded container outlines (Chests, Shulker Boxes, Ender Chests, Barrels, Hoppers).
- **Freecam**: Detachable camera spectator mode for base scouting while your player body remains safe.
- **NoHurtCam**: Disables camera tilt and red damage flash upon taking hits.
- **MiniMap**: Live 2D overhead radar tracking surface terrain, cardinal rotation, and nearby players.

### Player & Automation
- **FastPlace**: Removes click delays for rapid building and block placement.
- **Scaffold**: Automatic bridging directly beneath the player while navigating over air.
- **AutoEat**: Automatically consumes inventory food when hunger drops below a set threshold.
- **AntiAFK**: Micro-movement simulator preventing idle kicks on multiplayer servers.
- **AutoReconnect**: Automatic retry handler that reconnects upon connection drops.

### HUD Overlays
- **ArmorHUD**: Real-time display of equipped armor piece durability and active weapon stats.
- **CPS Counter**: Rolling click-per-second tracker for left and right mouse buttons.
- **Coords & FPS**: Minimalist status bar displaying XYZ coordinates, facing direction, biome, and frame rate.
- **Ping Tracker**: Live latency readout to the connected server.
- **InventoryViewer**: Visual HUD displaying main inventory items without opening the screen.
- **SpotifyHUD**: Desktop media integration displaying currently playing artist and song title.

---

## Project Structure

```
KozyClient (branch: main)
├── src/
│   ├── client/java/com/redstarclient/
│   │   ├── config/          # JSON configuration persistence & serialization
│   │   ├── event/           # High-speed EventBus & game lifecycle events
│   │   ├── fabric/          # Fabric ClientModInitializer & mixins
│   │   ├── gui/             # Dear ImGui ClickGUI, HUD editor & widgets
│   │   ├── module/          # Combat, Movement, Render, Player & Info modules
│   │   ├── render/          # Matrix math, ESP renderer & render utils
│   │   ├── theme/           # UI styling & color palettes
│   │   └── util/            # Player, inventory, block & timer helpers
│   └── main/resources/      # Mod metadata, mixins, icons & assets
├── gradle/wrapper/          # Gradle wrapper binaries & configuration
├── build.gradle             # Fabric Loom 1.17 build script
├── settings.gradle          # Gradle project configuration
└── gradle.properties        # Minecraft 26.2 & Fabric dependency versions
```

---

## Building from Source

### Prerequisites
- Java Development Kit (JDK) 25 or higher
- Git

### Build Instructions
```bash
# Clone the repository
git clone https://github.com/mrKozy/KozyClient.git
cd KozyClient

# Build the Fabric mod JAR
./gradlew build
```
The compiled mod JAR will be located in `build/libs/`.

---

## Downloads

Precompiled `.jar` files for both Minecraft **26.2** and **1.21.4** are available on the [Releases](https://github.com/mrKozy/KozyClient/releases) page.

---

## Author & License

- **Author**: [mrKozy](https://github.com/mrKozy)
- **License**: [MIT License](LICENSE)
