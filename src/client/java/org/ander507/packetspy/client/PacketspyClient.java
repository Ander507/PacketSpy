package org.ander507.packetspy.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Environment(EnvType.CLIENT)
public class PacketspyClient implements ClientModInitializer {
	public static final String MOD_ID = "packetspy";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static PacketSpyServer packetSpyServer;
    private static PacketSpyHttpServer httpServer;

	@Override
	public void onInitializeClient() {
        LOGGER.info("Initializing PacketSpy Client");
        PacketSpyConfig.load();

        packetSpyServer = new PacketSpyServer(PacketSpyConfig.webSocketPort);
        packetSpyServer.start();

        httpServer = new PacketSpyHttpServer();
        httpServer.start(PacketSpyConfig.httpServerPort);

        KeyBinding openWebUIKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.packetspy.open_web_ui", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F12, // Default key
                "category.packetspy.general" // The translation key of the keybinding's category.
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openWebUIKey.wasPressed()) {
                if (client.player != null) {
                    try {
                        Util.getOperatingSystem().open(new URI("http://localhost:" + PacketSpyConfig.httpServerPort));
                        client.player.sendMessage(Text.of("PacketSpy: Opened Web UI in browser"), true);
                    } catch (Exception e) {
                        LOGGER.error("Failed to open Web UI", e);
                        client.player.sendMessage(Text.of("PacketSpy: Failed to open Web UI"), false);
                    }
                }
            }
        });
	}

    public static void broadcast(String message) {
        if (packetSpyServer != null) {
            packetSpyServer.broadcast(message);
        }
    }
}
