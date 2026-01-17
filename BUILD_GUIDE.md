# Building PacketSpy for Different Versions

Currently, the project is configured for Minecraft 1.21.1.

## Option 1: Multi-Version Support (Current Setup)
The `fabric.mod.json` is currently set to support a range of versions:
```json
{
  "minecraft": ">=1.21 <=1.21.11"
}
```
This means the single JAR built with `1.21.1` should theoretically work on 1.21.0 through 1.21.11, provided Mojang hasn't broken the specific APIs you use.

To build this JAR:
1. Open Terminal.
2. Run: `./gradlew build`
3. Find the JAR in `build/libs/`.

## Option 2: Targeting a Specific Version (e.g., 1.20.1)
If you need to build specifically for an older or newer version (e.g., 1.20.1), you must update the dependencies in `gradle.properties`.

1. **Find Versions**: Go to [fabricmc.net/develop](https://fabricmc.net/develop/) to find the correct versions for:
   - Minecraft Version
   - Yarn Mappings
   - Fabric Loader
   - Fabric API

2. **Edit `gradle.properties`**:
   Update these lines with the new versions:
   ```properties
   minecraft_version=1.20.1
   yarn_mappings=1.20.1+build.10
   loader_version=0.15.11
   fabric_version=0.92.0+1.20.1
   ```

3. **Update `fabric.mod.json`**:
   Edit `src/main/resources/fabric.mod.json` to match the target version:
   ```json
   "depends": {
       "minecraft": "1.20.1"
   }
   ```

4. **Refresh Gradle**: Click the Gradle refresh button in your IDE.

5. **Build**: Run `./gradlew build`.

## Troubleshooting
If you switch versions, you might encounter compilation errors if Minecraft code has changed (e.g., method names, class locations). You will need to fix these errors in the Java code.

