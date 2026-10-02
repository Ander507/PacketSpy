package org.ander507.packetspy.client;

import com.mojang.blaze3d.platform.InputConstants;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Locale;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.ander507.packetspy.client.compat.Compat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public class PacketspyClient implements ClientModInitializer {
	public static final String MOD_ID = "packetspy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static volatile PacketSpyServer packetSpyServer;
    private static PacketSpyHttpServer httpServer;

	@Override
	public void onInitializeClient() {
        LOGGER.info("Initializing PacketSpy Client");
        PacketSpyConfig.load();

        int httpPort = PacketSpyConfig.httpServerPort;
        int webSocketPort = PacketSpyConfig.webSocketPort;
        packetSpyServer = new PacketSpyServer(bindAddress(webSocketPort), httpPort);
        packetSpyServer.start();

        httpServer = new PacketSpyHttpServer();
        httpServer.start(bindAddress(httpPort), webSocketPort);
        logLanAddresses(httpPort);

        KeyMapping openWebUIKey = Compat.registerKey("key.packetspy.open_web_ui", InputConstants.KEY_F12);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openWebUIKey.consumeClick()) {
                openWebUI(client);
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> stopServers());
	}

    /** Called from the network thread for every packet the client sends or receives. */
    static void capture(String direction, Packet<?> packet) {
        PacketSpyServer server = packetSpyServer;
        // Do nothing at all while no browser is watching
        if (server == null || !server.hasClients()) {
            return;
        }
        if (!PacketSpyConfig.logPlayerMovementPackets && packet instanceof ServerboundMovePlayerPacket) {
            return;
        }
        server.enqueue(new PacketSpyServer.CapturedPacket(direction, packet, System.currentTimeMillis()));
    }

    private static InetSocketAddress bindAddress(int port) {
        // Only this PC can connect unless LAN access is turned on in config/packetspy.json
        return PacketSpyConfig.allowLanAccess ? new InetSocketAddress(port) : new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
    }

    private static void logLanAddresses(int port) {
        if (!PacketSpyConfig.allowLanAccess) {
            LOGGER.info(">> To open the web UI from a phone, set \"allowLanAccess\": true in config/packetspy.json");
            return;
        }
        try {
            for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress address : Collections.list(ni.getInetAddresses())) {
                    if (address.isSiteLocalAddress()) {
                        LOGGER.info(">> Mobile : http://{}:{}", address.getHostAddress(), port);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warn("Could not list network addresses", e);
        }
    }

    private static void openWebUI(Minecraft client) {
        String url = "http://127.0.0.1:" + PacketSpyConfig.httpServerPort;
        try {
            openInBrowser(url);
            message(client, "PacketSpy: Opened Web UI in browser");
        } catch (Exception e) {
            LOGGER.error("Failed to open Web UI", e);
            message(client, "PacketSpy: Failed to open Web UI, go to " + url);
        }
    }

    private static void openInBrowser(String url) throws IOException {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        ProcessBuilder command;
        if (os.contains("win")) {
            command = new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url);
        } else if (os.contains("mac")) {
            command = new ProcessBuilder("open", url);
        } else {
            command = new ProcessBuilder("xdg-open", url);
        }
        command.start();
    }

    private static void message(Minecraft client, String text) {
        // Action bar message
        Compat.showOverlayMessage(client, Component.literal(text));
    }

    private static void stopServers() {
        PacketSpyServer server = packetSpyServer;
        packetSpyServer = null;
        try {
            if (server != null) {
                server.stop(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (httpServer != null) {
            httpServer.stop();
        }
    }
}
