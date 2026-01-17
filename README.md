# PacketSpy

**PacketSpy** is a lightweight network debugging utility designed for Minecraft Fabric (version 1.21.1). It serves as a real-time packet inspector, allowing developers and power users to monitor the network traffic between the client and the server.

The core functionality revolves around intercepting both transmitted (TX) and received (RX) packets. Instead of relying solely on the in-game console, PacketSpy hosts a local web server using `Java-WebSocket`. This provides a clean, external web interface accessible via a browser (defaulting to `http://localhost:8887`), where packet data is streamed live.

Key features include:
*   **Live Traffic Monitoring:** View packet names and timestamps as they occur.
*   **Web Interface:** A dedicated dashboard to watch traffic without cluttering the game chat or logs.
*   **Smart Filtering:** To prevent log spam, the mod includes toggleable filters. Specifically, it can ignore high-frequency "noise" packets such as `move_player_pos` and `move_player_rot`, ensuring that critical network events aren't buried under movement data.
*   **Silence Packets:** Easily silence specific packets directly from the web interface to focus on what matters.
*   **Easy Export:** Export captured packets to a text file for analysis.

## Building
To build the mod for Minecraft 1.21.x (compatible with 1.21.0 - 1.21.5+):
1. Run `build_release.bat` (Windows).
2. Check the `release` folder for the JAR file.

Currently at version **2.3**, the project is built using Gradle and integrates seamlessly with the Fabric Loader ecosystem.

## Supported Versions
*   **1.21**
*   **1.21.1**
*   **1.21.2**
*   **1.21.3**
*   **1.21.4**
*   **1.21.5**

See `SUPPORTED_VERSIONS.md` for more details.
