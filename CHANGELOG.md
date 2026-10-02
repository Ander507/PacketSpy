# Changelog

## Version 2.4 - Multi-Version Update

### New
*   **Minecraft 1.21 to 26.3**: one jar per version range, see `SUPPORTED_VERSIONS.md`.
*   **Redesigned web UI**: live traffic waveform, details panel with highlighted JSON and Copy JSON, settings panel, works offline (no web fonts) and on phones.
*   **Sorting**: newest at bottom or top, or grouped by packet type (A–Z or most common first) with totals; click a type to see its packets.
*   **Low memory**: only the rows on screen exist in the page (about 230 elements instead of 40,000 at 5,000 packets).
*   **Readable names in the real game**: packets show as `move_player_pos` with fields like `x`, `y`, `onGround` instead of `class_2828` / `comp_1563`. Works offline, no StackDeobf needed.
*   Web UI language entries for the F12 key binding.
*   `allowLanAccess` option in `config/packetspy.json` for viewing on a phone.

### Fixed
*   Packet names were obfuscated (`class_2743`) outside the dev environment, which made multiplayer look broken.
*   The movement toggle did nothing in the real game.
*   Singleplayer logged every packet twice with swapped directions.
*   The mod claimed to support 1.21.9+ but crashed there (key binding changes).
*   Web UI: server-controlled text (chat, signs, item names) could run as HTML/JS.
*   Any website, or anyone on your network, could connect to the WebSocket and read your packets.
*   A broken `packetspy.json` crashed the game on start.
*   Thread-safety issue with the silenced packet list.
*   "STACK: OFF" merged packets with different data.
*   The web UI ignored the configured WebSocket port.
*   Busy servers could lag the game: packets are now serialized off the network thread, sent in batches, and skipped entirely while no browser is open.
*   The servers are shut down when the game closes; the license file is now included in the jar.

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

