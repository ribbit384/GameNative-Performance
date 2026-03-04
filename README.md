# GameNative-Performance (GNP)

[![Join Discord](https://img.shields.io/badge/Join%20Discord-5865F2?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/KWc5h7GZTK)

**GameNative-Performance** is a high-performance, quality-of-life focused fork of [GameNative Official](https://github.com/utkarshdalal/GameNative). While we build upon the incredible foundation laid by the GameNative team, GNP strives to provide a more refined, efficient, and performance-oriented experience for Adreno-powered devices.

---

## 🌟 Key Features & Quality of Life

### 📦 Efficiency & Storage
- **Master Containers:** Drastically reduce the application's storage footprint. Instead of creating a unique container for every game, "Master Containers" allow multiple games to share a single, optimized container environment.
- **Unified Download Manager:** A native, robust download system supporting **Custom Download Paths** for all integrated stores: **Steam, Epic, GOG, and Amazon**.

### 🎮 Controller-First Experience
- **New Controller-Friendly UI & Menus:** A complete overhaul of the interface designed specifically for handhelds and gamepads. Navigate your entire library and settings without ever touching the screen.
- **Enhanced Multi-Controller Support:** Stable local multiplayer for up to **4 Players** with improved mapping and 'coffincolors' controller fixes.
- **Stretch To Fullscreen:** Native support for various aspect ratios, ensuring your games fill the screen on modern mobile devices.
- **No Login Requirement:** Mandatory accounts have been removed. Use the full power of the app instantly; login is now optional and found in settings.

### 🚀 Performance & Backend (The Powerhouse)
- **80% Ludashi 2.9 Backend:** Integrated core logic and stability improvements from the Ludashi 2.9 project.
- **StevenMXZ Integrations:**
    - **Native Rendering:** Leveraging advanced rendering techniques for improved visual fidelity and lower overhead.
    - **Screen Effects:** High-quality post-processing and visual filters directly from Ludashi 2.9.
    - **File Redirect Hooks:** Specialized Bionic/Glibc hooks for seamless path remapping and performance.
- **Alsa-Reflector Integration:** Powered by **CoffinColors**, providing low-latency, high-fidelity audio reflection for a superior sound experience.
- **Adreno-Specific Tuning:** Optimized for Snapdragon 8 Gen 1/2/3 and 8 Elite with smart power management and specialized Turnip driver variants.

---

## 🤝 Credits & Acknowledgments

GNP is a community-driven effort that stands on the shoulders of giants. We give full credit to the original creators and the developers whose work we've integrated:

- **GameNative Official:** Our foundational base. We are a fork that strives to provide more QoL fixes and performance while respecting and building upon their incredible dedication.
- **StevenMXZ (Ludashi 2.9):** For the Native Rendering, Screen Effects, and the robust backend logic that powers our core.
- **CoffinColors:** For the Alsa-Reflector integration and essential controller stability fixes.
- **Winlator Teams:** Credit to all contributors, from the original **Bruno** builds to the various community-led forks that have pushed the scene forward.
- **Core Components:** **Box64**, **Fex-Emu**, **DXVK (Gplasync)**, **VKD3D-Proton**, and the **Mesa/Turnip** contributors.

---

## 🚀 Getting Started

1. **Download:** Grab the latest debug APK from the Actions or Releases tab.
2. **Install:** Sideload onto your Adreno-powered Android device.
3. **Configure:** Head to **Settings** to enable Master Containers and set your custom download paths.
4. **Play:** Experience your PC library with maximum performance and a modern UI.

### 🏗️ Building from Source
```bash
./gradlew assembleDebug
```
*Requires Android SDK 35 and OpenJDK 17.*

---

**Disclaimer:** *GameNative-Performance is intended for playing legally owned software. We do not condone or support piracy. Use responsibly.*
