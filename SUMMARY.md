# PacketSpy - Advanced Minecraft Packet Sniffer

**PacketSpy** is the ultimate client-side network analysis tool for Minecraft 1.21.x, designed for developers, protocol researchers, and technical players who demand detailed insight into server communication.

Unlike basic loggers, PacketSpy injects a high-performance probe directly into the Netty pipeline, capturing every packet sent and received with zero latency. It powers a local **Real-Time Web Interface** (`http://localhost:8887`), presenting traffic in a sleek, hacker-style dark mode dashboard that keeps your in-game chat clean.

### 🚀 Key Features
*   **Deep Reflection Analysis**: PacketSpy now uses advanced reflection to deconstruct packets, revealing internal field values (even in obfuscated environments) and newer Java Records effectively.
*   **Smart Web Dashboard**: Filter, sort, and search packets instantly. Toggle "Player Movement" to hide spammy position updates, or use **Silence Mode** to permanently ignore specific packet types.
*   **In-Game Integration**: Press **F12** to instantly open the dashboard in your default browser.
*   **Robust Compatibility**: Engineered to handle the entire 1.21.x family (1.21.0 - 1.21.11), dynamically adapting to protocol changes.

Whether you are debugging a mod, analyzing server plugins, or just curious about the protocol, PacketSpy transforms raw network data into actionable intelligence. Export your logs, inspect NBT data, and visualize the invisible flow of Minecraft.

