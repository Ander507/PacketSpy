# Changelog

## Version 2.0 - The "Terminal" Update

This is the first major release of the revamped PacketSpy, designed for Minecraft 1.21.x.

### New Features
*   **Web Interface V2.0**: Completely redesigned "Dark Terminal" UI for a better hacking feel.
*   **Real-Time Monitoring**: Watch packets fly by in real-time on `http://localhost:8887`.
*   **Silence Packets**: Added a `[X]` button next to packets to instantly silence them. Manage silenced packets via the "SILENCED" menu.
*   **Smart Filtering**:
    *   Toggle "Movement" packets on/off to reduce spam.
    *   Filter by category: Player, Interface, Entities, World, Misc.
    *   Regex search support.
*   **Export**: Easily export currently visible packets to a text file for analysis.
*   **Multi-Version Support**: Built to work across the entire 1.21.x family (1.21.0 to 1.21.11).

### Technical
*   Injects into `ClientConnection` for robust packet interception.
*   Uses `Java-WebSocket` for the backend server.
*   Configuration saved to `config/packetspy.json`.

