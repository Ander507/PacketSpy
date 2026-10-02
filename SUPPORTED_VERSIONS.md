# Supported Minecraft Versions

PacketSpy is built as one jar per range of Minecraft versions. Pick the jar whose name covers your version:

| Jar | Minecraft versions | Java |
|-----|--------------------|------|
| `PacketSpy-2.4+mc1.21-1.21.8.jar` | 1.21, 1.21.1, 1.21.2, 1.21.3, 1.21.4, 1.21.5, 1.21.6, 1.21.7, 1.21.8 | 21+ |
| `PacketSpy-2.4+mc1.21.9-1.21.10.jar` | 1.21.9, 1.21.10 | 21+ |
| `PacketSpy-2.4+mc1.21.11.jar` | 1.21.11 | 21+ |
| `PacketSpy-2.4+mc26.1-26.2.jar` | 26.1, 26.1.1, 26.1.2, 26.2 | 25+ |
| `PacketSpy-2.4+mc26.3.jar` | 26.3 | 25+ |

All jars need Fabric Loader and Fabric API. Each jar refuses to load on a version it was not built for, so the game tells you if you picked the wrong one.

## Why several jars?

A new jar starts wherever the code had to change:

* **1.21.9** – key bindings got registered categories instead of plain text categories.
* **1.21.11** – Mojang renamed `ResourceLocation` to `Identifier`.
* **26.1** – Minecraft is no longer obfuscated and needs Java 25; Fabric API renamed its key binding helper.
* **26.3** – Minecraft switched from GLFW to SDL for input, so key codes (F12) changed.

## Readable names on 1.21.x

Minecraft 1.21.x is obfuscated (`class_2828`, `field_12345`, `comp_1563`). Each 1.21.x jar contains a small table built from the Yarn mappings for every version it covers, so packets show up with readable class and field names, offline. From 26.1 on, Minecraft ships with real names.

## Adding a new Minecraft version

1. Add the version (and its Fabric API version) to `versions.json`, under `minecraft` and in the newest target's `versions` list.
2. Run `check_versions.bat`. If it compiles, run `build_release.bat`.
3. If it doesn't compile, make a new target for it with its own folder in `src/compat/`.
