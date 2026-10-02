# PacketSpy

PacketSpy is a **client-side Fabric mod** for Minecraft that shows every network packet the game sends and receives, live, in a web page. It works in singleplayer and on multiplayer servers, for **Minecraft 1.21 – 1.21.11 and 26.1 – 26.3**.

In game, press **F12** (or open `http://127.0.0.1:8888`) to open the web UI. There you can search, filter by direction and category, sort by time or packet type, open a packet to see all its fields as JSON, mute noisy packets and export what you see.

This README is the main reference for the project, written so that a person or an AI assistant can understand and change the code without reading all of it first. `SUMMARY.md` and `mod_summary.md` are older store-page texts and are out of date.

---

## Quick facts

| | |
|---|---|
| Mod id | `packetspy` (client only, `"environment": "client"`) |
| Version | `2.4` (`mod_version` in `gradle.properties`) |
| Minecraft | 1.21 – 1.21.11 and 26.1 – 26.3, one jar per version range |
| Loader / API | Fabric Loader + Fabric API |
| Java | 21 for the 1.21.x jars, 25 for the 26.x jars |
| Build | Gradle 9.7.1 wrapper, Fabric Loom 1.18.2, **must run on JDK 25** |
| Bundled library | Java-WebSocket 1.6.0 (jar-in-jar) |
| Ports | HTTP (web page) `8888`, WebSocket (packet stream) `8887` |
| Config | `config/packetspy.json` |
| Key binding | F12, `key.packetspy.open_web_ui` |
| Main package | `org.ander507.packetspy` |

---

## How it works

```
Minecraft (Netty pipeline of the client's Connection)
  │  ConnectionMixin adds PacketSpyHandler right before Minecraft's "packet_handler",
  │  only on the client's own (CLIENTBOUND) connection
  ▼
PacketSpyHandler.channelRead / write            ← network thread, must stay cheap and never throw
  │  PacketspyClient.capture(direction, packet)
  │    - returns at once if no browser is connected
  │    - drops ServerboundMovePlayerPacket if "Player movement" is off
  ▼
PacketSpyServer queue (bounded, 10,000; overflow is counted and reported as "dropped")
  ▼
"PacketSpy Sender" thread                        ← all expensive work happens here
  │  format(): packet id, readable class name (NameTable), mute check,
  │            PacketSerializer → JSON of every field
  │  sends up to 500 packets as one JSON array, then sleeps 25 ms
  ▼
WebSocket :8887  ──►  web/index.html in the browser (served by PacketSpyHttpServer on :8888)
```

Settings changed in the browser (mute, movement) go back over the same WebSocket and are saved to `config/packetspy.json`.

---

## Source map

```
build.gradle            Multi-version build (see "Building"), name table generation, verifyJar, prod test tasks
versions.json           Every supported Minecraft version + which jar ("target") covers it
gradle.properties       Loom/loader versions, mod_version, default target for the IDE
build_release.bat       Builds every target into release/
check_versions.bat      Builds every target, then compiles + binary-checks it against each of its versions

src/main/resources/fabric.mod.json            Mod metadata; ${...} placeholders filled per target by build.gradle
src/client/resources/packetspy.client.mixins.json
src/client/resources/assets/packetspy/lang/en_us.json   Key binding names
src/client/resources/web/index.html            The whole web UI (HTML + CSS + JS, no dependencies)

src/client/java/org/ander507/packetspy/
  mixin/client/ConnectionMixin.java    Injects into Connection.channelActive, adds PacketSpyHandler
  client/PacketspyClient.java          Entry point: loads config, starts both servers, F12 key, capture() filter
  client/PacketSpyHandler.java         Netty ChannelDuplexHandler; hands packets to capture(), never throws
  client/PacketSpyServer.java          WebSocket server: queue, sender thread, JSON formatting, origin check, settings messages
  client/PacketSpyHttpServer.java      Serves /web/* from the jar; fills the WebSocket port into index.html
  client/PacketSerializer.java         Reflection: any object → JSON (fields, records, lists, maps), with size limits
  client/NameTable.java                Turns obfuscated names (class_1234, field_5678, comp_9012) into readable ones
  client/PacketSpyConfig.java          Reads/writes config/packetspy.json

src/compat/<name>/java/org/ander507/packetspy/client/compat/Compat.java
  One small class per API generation, chosen by the target in versions.json:
  mc1.21    1.21 – 1.21.8    KeyMapping with a String category, KeyBindingHelper, displayClientMessage
  mc1.21.9  1.21.9 – 1.21.10 KeyMapping.Category registered with ResourceLocation
  mc1.21.11 1.21.11          Same, but ResourceLocation is now called Identifier
  mc26      26.x             Fabric's KeyMappingHelper, KeyMapping(name, key, category), sendOverlayMessage
```

---

## Code conventions that matter

* **All Java code uses Mojang's official names** (`Connection`, `Minecraft`, `KeyMapping`, `ServerboundMovePlayerPacket`), for every target. 1.21.x targets are compiled with `loom.officialMojangMappings()`; 26.x is unobfuscated and uses these names natively. Do **not** use Yarn names (`ClientConnection`, `MinecraftClient`, `KeyBinding`).
* Code that differs between Minecraft versions goes in `Compat.java` (one copy per `src/compat/*` folder, all with the same method signatures). Shared code calls `Compat.registerKey(...)` and `Compat.showOverlayMessage(...)`.
* `PacketSpyHandler` runs on Netty's network thread. Anything that throws there disconnects the player, and anything slow lags the game. Keep it to a quick check plus an enqueue.
* Packets are turned into JSON generically by reflection (`PacketSerializer`), never with per-packet code, so Mojang changing a packet can't break the mod.
* In the web UI, packet data is only ever put on the page with `textContent` / text nodes, never `innerHTML`. Servers control chat, sign and item text.
* `.bat` files must keep Windows (CRLF) line endings.

---

## WebSocket protocol (port 8887)

Only browsers whose `Origin` is this page (`http://127.0.0.1:8888`, `http://localhost:8888`, or a LAN IP of this PC when `allowLanAccess` is on) may connect. Clients without an `Origin` header (scripts) are allowed.

### Mod → browser

On connect, a single config object:
```json
{ "type": "config", "logPlayerMovementPackets": true, "silencedPackets": ["keep_alive"] }
```

Packets arrive in batches (a JSON array):
```jsonc
[
  {
    "type": "packet",
    "direction": "OUT",                        // "IN" = from the server, "OUT" = sent by the client
    "name": "move_player_pos",                 // packet id without "minecraft:", same on every version
    "class": "net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket$PositionAndOnGround",
    "time": 1790959605131,                     // ms since epoch, when the mod captured it
    "data": { "x": -3.5, "y": -60.0, "z": -1.5, "onGround": false }
  },
  { "type": "dropped", "count": 42 }           // packets skipped because the queue was full
]
```
`class` uses Yarn-style names on 1.21.x (from the name table) and Mojang names on 26.x (`net.minecraft.network.protocol.game.ServerboundMovePlayerPacket$Pos`).

`data` limits (in `PacketSerializer`): nesting depth 3, 32 items per list/map, strings cut at 2,000 characters, 2,000 values per packet. `byte[]` shows as `"byte[1234]"`, lambdas as `"<lambda>"`, and the int `-999` as `"-999 (Outside Inventory)"`.

### Browser → mod
```json
{ "action": "toggleMovement", "value": false }
{ "action": "silencePacket", "packetName": "keep_alive" }
{ "action": "unsilencePacket", "packetName": "keep_alive" }
```
After a change the mod saves the config and broadcasts a new `config` message to every open page.

A muted name matches the packet id, the readable class name (full or simple), or the raw class name.

---

## Config: `config/packetspy.json`

| Key | Default | Meaning |
|---|---|---|
| `silencedPackets` | `[]` | Muted packet names |
| `logPlayerMovementPackets` | `true` | Capture `ServerboundMovePlayerPacket` |
| `httpServerPort` | `8888` | Port of the web page |
| `webSocketPort` | `8887` | Port of the packet stream (filled into the page automatically) |
| `allowLanAccess` | `false` | `false`: only this PC can connect. `true`: listen on all interfaces so a phone on the same network can open the page |

A broken file is left untouched and defaults are used for that session; the game does not crash.

---

## Building

Requirements: **JDK 25** as the JDK that runs Gradle (it also compiles the Java 21 jars). Everything else is downloaded by the wrapper.

```bat
build_release.bat                                rem all jars → release\
check_versions.bat                               rem build all, then verify every supported version
gradlew releaseJar -Ptarget=1.21.11              rem one jar → release\
gradlew compileClientJava -Ptarget=26.1-26.2 -Pmc=26.2   rem compile a target against another version in its range
gradlew verifyJar -Ptarget=26.1-26.2 -Pmc=26.2   rem binary check of release\<jar> against that version
```

### `versions.json`

```jsonc
{
  "minecraft": {
    "1.21.4": { "fabric_api": "0.119.4+1.21.4", "yarn": "1.21.4+build.8" },
    "26.2":   { "fabric_api": "0.161.0+26.2" }
  },
  "targets": [
    {
      "name": "1.21-1.21.8",        // jar name: PacketSpy-<mod_version>+mc<name>.jar
      "obfuscated": true,           // true: fabric-loom-remap + Mojang mappings; false: fabric-loom (26.x)
      "java": 21,
      "loader": ">=0.15.11",        // fabricloader dependency written into fabric.mod.json
      "compat": ["mc1.21"],         // folders under src/compat added to the sources
      "versions": ["1.21", "...", "1.21.8"]   // first = version it is compiled against; range goes into fabric.mod.json
    }
  ]
}
```

`-Ptarget` picks the target (default: `target=` in `gradle.properties`, which is also what the IDE loads). `-Pmc` swaps the Minecraft version used for compiling and testing, but only to one listed in that target.

### Current targets

| Jar | Minecraft | Why a new jar starts here |
|---|---|---|
| `PacketSpy-2.4+mc1.21-1.21.8.jar` | 1.21 – 1.21.8 | |
| `PacketSpy-2.4+mc1.21.9-1.21.10.jar` | 1.21.9 – 1.21.10 | Key bindings use registered `KeyMapping.Category` objects |
| `PacketSpy-2.4+mc1.21.11.jar` | 1.21.11 | Mojang renamed `ResourceLocation` to `Identifier` |
| `PacketSpy-2.4+mc26.1-26.2.jar` | 26.1 – 26.2 | No more obfuscation, Java 25, Fabric API renamed to Mojang names (`KeyMappingHelper`) |
| `PacketSpy-2.4+mc26.3.jar` | 26.3 | GLFW replaced by SDL: key codes changed (`InputConstants.KEY_F12` is 301 before, 69 in 26.3) |

### Readable names on 1.21.x (name table)

1.21.x jars run against obfuscated ("intermediary") names, so a packet would show as `class_2828` with fields like `comp_1563`. The `generateNameTable` task downloads the Yarn mappings (CC0) for **every version in the target**, keeps only classes (`c`), fields (`f`) and record components (`r`), and writes `packetspy/names.tsv.gz` into the jar (about 400 KB). `NameTable` loads it on first use and rewrites names in class names, field names and `toString()` output. 26.x jars don't need it and don't include it.

### Why `verifyJar` exists

"It compiles against version X" does not prove the jar built against the target's *first* version runs on X: intermediary names can change while the Mojang name stays the same. Example: `Packet.type()` is `method_55846` up to 1.21.3 and `method_65080` from 1.21.4. (`PacketSpyServer` now finds that method by its return type for this reason.) `verifyJar` reads the release jar with ASM and checks that every Minecraft class, method, field and mixin target it uses exists in the version given by `-Pmc`. `check_versions.bat` runs it for every version of every target.

Compile-time constants are a second trap. They are copied into the jar at build time, so a value that changes between versions (like `KEY_F12` in 26.3) needs its own target even though everything compiles and verifies.

### Adding a new Minecraft version

1. Add it under `minecraft` in `versions.json` with its Fabric API version (and Yarn build if it's obfuscated). Latest versions: <https://fabricmc.net/develop>, <https://meta.fabricmc.net/v2/versions/game>.
2. Add it to the newest target's `versions` list and run `check_versions.bat`.
3. If it fails, create a new target for it. If an API changed, copy the nearest `src/compat/*` folder, fix `Compat.java` there, and point the new target's `compat` at it.
4. Run `build_release.bat` and test in game (below).

---

## Testing

There are no unit tests. Testing means compile and binary checks (`check_versions.bat`) plus running the real game.

* **Dev environment:** `gradlew runClient` (Minecraft with readable Mojang names, so the name table isn't used).
* **Real game (production names):** Loom's production run tasks, in two terminals:
  ```bat
  gradlew prodServer -Ptarget=1.21-1.21.8 -Pmc=1.21.8
  gradlew prodClient -Ptarget=1.21-1.21.8 -Pmc=1.21.8 -Pjoin=localhost:25565
  ```
  The server runs in `run/prod-server-<mc>/`. Set `online-mode=false` and `white-list=false` in its `server.properties` (26.3 servers turn the whitelist on by default) and `eula=true` in `eula.txt`. `-PmodJar=path\to\jar` runs another PacketSpy jar instead of the one just built, for example an older release.
* Then open `http://127.0.0.1:8888`, or connect a script to `ws://127.0.0.1:8887` and count the packets.

Already verified: multiplayer capture on 1.21.3, 1.21.8, 1.21.10, 1.21.11 and 26.2, with readable names and foreign origins rejected. `verifyJar` passes for all 17 versions.

---

## Web UI (`src/client/resources/web/index.html`)

One self-contained file: no frameworks, no web fonts, no network requests besides the WebSocket. `PacketSpyHttpServer` replaces `__PACKETSPY_WS_PORT__` with the configured port when serving it.

* **Virtual list.** Only the rows on screen exist in the page (about 230 elements total at the 5,000-packet cap, instead of one element tree per packet). Rows are reused while scrolling; clicks are handled once on the list (`#rows`).
* **State** lives in `state`: `packets` (newest last, capped at `MAX_PACKETS = 5000`), `view` (packets passing filters), `sorted`/`groups` (type views), `filter`, `selected`, `follow` (stick to newest).
* **Sorting** (`#sort`):
  * `bottom` / `top`: by time, live.
  * `name` / `common`: one row per packet type (direction + name) with totals; click a type to list its packets. Rebuilt at most once a second, and not while the mouse is over the list, so rows don't move under the pointer.
* **Group repeats.** An identical packet (same direction, name and data) right after the previous one increases its `×N` counter instead of adding a row.
* **Categories** come from regex rules on the packet id (`CATEGORY_RULES`, first match wins): Player, Entities, World, Inventory, Chat, Connection, Other.
* **Waveform.** 120 buckets of 500 ms on a canvas: incoming drawn up, outgoing drawn down, square-root scale. It keeps showing traffic while the list is paused.
* **Details panel.** Builds highlighted JSON from text nodes, with Copy JSON (falls back to `execCommand` on non-secure LAN pages) and Mute.
* **Saved in `localStorage`:** `packetspy.theme`, `packetspy.stack`, `packetspy.cat`, `packetspy.sort`.
* **Theme colors:** CSS variables on `:root`. Dark is the default; light is set by `data-theme="light"` or by the system setting when the theme is `system`. Color only ever means direction (`--in` cyan, `--out` orange).
* **Keyboard:** `/` focuses search, `Esc` closes settings/details, ↑/↓ move through packets when the list has focus.

---

## Installing (players)

1. Install Fabric Loader and Fabric API for your Minecraft version.
2. Put the matching jar from `release\` into `.minecraft/mods` (see `SUPPORTED_VERSIONS.md`; a wrong jar is refused with a clear message).
3. Start the game and press **F12**.

To view from a phone on the same network, set `"allowLanAccess": true` in `config/packetspy.json`, restart, and open the address printed in the game log (`>> Mobile : http://192.168.x.x:8888`).
