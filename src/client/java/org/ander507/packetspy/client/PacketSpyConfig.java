package org.ander507.packetspy.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class PacketSpyConfig {
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("packetspy.json").toFile();

    public static List<String> silencedPackets = new ArrayList<>();
    public static boolean logPlayerMovementPackets = true;
    public static int httpServerPort = 8888;
    public static int webSocketPort = 8887;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    if (data.silencedPackets != null) {
                        silencedPackets = data.silencedPackets;
                    }
                    logPlayerMovementPackets = data.logPlayerMovementPackets;
                    httpServerPort = data.httpServerPort;
                    webSocketPort = data.webSocketPort;
                }
            } catch (IOException e) {
                PacketspyClient.LOGGER.error("Failed to load config", e);
            }
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            data.silencedPackets = silencedPackets;
            data.logPlayerMovementPackets = logPlayerMovementPackets;
            data.httpServerPort = httpServerPort;
            data.webSocketPort = webSocketPort;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            PacketspyClient.LOGGER.error("Failed to save config", e);
        }
    }

    private static class ConfigData {
        List<String> silencedPackets = new ArrayList<>();
        boolean logPlayerMovementPackets = true;
        int httpServerPort = 8888;
        int webSocketPort = 8887;
    }
}

