# PacketSpy - Advanced Minecraft Packet Inspection Tool

## Overview
PacketSpy is a powerful, client-side packet sniffer designed for modern Minecraft versions (1.21.x). It provides a real-time "hacker-style" web interface to inspect, filter, and analyze network traffic between the client and server. Unlike traditional loggers, PacketSpy uses a robust reflection-based approach to read packet data, making it resilient to obfuscation changes across minor game updates.

## Key Features

### 📡 Real-Time Web Interface
- **Dark Mode Terminal UI:** Monitors network traffic in a sleek, hacker-themed dashboard accessible via browser (`http://localhost:8887`).
- **Live Filtering:** Filter packets by direction (RX/TX), category (Player, GUI, Entity, etc.), or regex search.
- **Packet Aggregation:** Automatically groups repeating packets to prevent log spam.
- **Detailed Inspection:** Expand any packet to see its internal fields and values.

### 🛠️ Advanced Tools
- **Movement Toggle:** Easily toggle player movement/rotation packets on/off to reduce noise.
- **Silencing System:** "Silence" specific packets directly from the UI to stop them from appearing in the log.
- **Export Functionality:** Export currently visible (filtered) packets to a text file for analysis.

### 🔧 Technical Robustness
- **Reflection-Based Decoding:** automatically reads packet fields by type (e.g., `int: 5`, `String: "hello"`), bypassing obfuscated field names (`field_1234`) that break other mods.
- **Cross-Version Compatibility:** Built to work across the 1.21.x family with minimal updates.
- **WebSocket Architecture:** Uses a dedicated WebSocket server for high-performance data streaming to the browser.

