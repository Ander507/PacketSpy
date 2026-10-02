# PacketSpy

**PacketSpy** is a lightweight network debugging utility designed for Minecraft Fabric (1.21 to 26.3). It serves as a real-time packet inspector, allowing developers and power users to monitor the network traffic between the client and the server, in singleplayer and on multiplayer servers.

The core functionality revolves around intercepting both transmitted (TX) and received (RX) packets. Instead of relying solely on the in-game console, PacketSpy hosts a local web server using `Java-WebSocket`. This provides a clean, external web interface accessible via a browser (press **F12** in game, or open `http://127.0.0.1:8888`), where packet data is streamed live.

By default the web UI only accepts connections from your own PC. To watch from a phone on the same network, set `"allowLanAccess": true` in `config/packetspy.json`.

Key features include:
*   **Live Traffic Monitoring:** View packet names and timestamps as they occur.
*   **Web Interface:** A dedicated dashboard to watch traffic without cluttering the game chat or logs.
*   **Smart Filtering:** To prevent log spam, the mod includes toggleable filters. Specifically, it can ignore high-frequency "noise" packets such as `move_player_pos` and `move_player_rot`, ensuring that critical network events aren't buried under movement data.
*   **Silence Packets:** Easily silence specific packets directly from the web interface to focus on what matters.
*   **Easy Export:** Export captured packets to a text file for analysis.

## Building
Building needs JDK 25 (it also builds the Java 21 jars for 1.21.x).
1. Run `build_release.bat` (Windows). It builds one jar per version range listed in `versions.json`.
2. Check the `release` folder for the JAR files.

To build a single jar: `gradlew releaseJar -Ptarget=1.21.11` (target names are in `versions.json`).
`check_versions.bat` compiles every jar against every Minecraft version it claims to support.

Currently at version **2.4**, the project is built using Gradle and integrates seamlessly with the Fabric Loader ecosystem.

## Supported Versions
*   **1.21 – 1.21.11**
*   **26.1 – 26.3**

See `SUPPORTED_VERSIONS.md` for which jar to use.
