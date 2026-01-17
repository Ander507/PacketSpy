# Supported Minecraft Versions

PacketSpy is designed to work across the entire **Minecraft 1.21.x** family.

## Fully Supported Versions
The following versions are fully compatible with automatic packet analysis and deobfuscation:

*   **1.21** (The Tricky Trials Update)
*   **1.21.1**
*   **1.21.2** (Bundles of Bravery)
*   **1.21.3**
*   **1.21.4** (The Winter Drop)
*   **1.21.5**

## Version Specifics

### 1.21.5
*   **Packet Structure**: Uses Java Records for most C2S packets (e.g., `ClickSlotC2SPacket` uses `comp_` fields).
*   **Status**: Fully mapped.

### 1.21.4
*   **Packet Structure**: Mixed usage of Records and standard Classes.
*   **Status**: Fully mapped.

### 1.21 - 1.21.3
*   **Packet Structure**: Standard Fabric Intermediary mappings.
*   **Status**: Stable.

## Compatibility Note
PacketSpy uses a heuristic mapping system (`MappingHelper`) that dynamically resolves field names at runtime. This allows a single Jar file to work across multiple minor versions without recompilation, provided Fabric Loader can load it.

