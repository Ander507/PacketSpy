//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ander507.packetspy.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.InetSocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

@Environment(EnvType.CLIENT)
public class PacketSpyServer extends WebSocketServer {
    private static final Gson GSON = new Gson();

    public PacketSpyServer(int port) {
        super(new InetSocketAddress(port));
    }

    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        PacketspyClient.LOGGER.info("New connection: " + conn.getRemoteSocketAddress());
        JsonObject state = new JsonObject();
        state.addProperty("type", "config");
        state.addProperty("logPlayerMovementPackets", PacketSpyConfig.logPlayerMovementPackets);
        state.add("silencedPackets", GSON.toJsonTree(PacketSpyConfig.silencedPackets));
        conn.send(GSON.toJson(state));
    }

    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        PacketspyClient.LOGGER.info("Closed connection: " + conn.getRemoteSocketAddress());
    }

    public void onMessage(WebSocket conn, String message) {
        PacketspyClient.LOGGER.info("Message from client: " + message);

        try {
            JsonObject json = JsonParser.parseString(message).getAsJsonObject();
            if (json.has("action")) {
                String action = json.get("action").getAsString();
                if ("toggleMovement".equals(action)) {
                    if (json.has("value")) {
                        PacketSpyConfig.logPlayerMovementPackets = json.get("value").getAsBoolean();
                        PacketSpyConfig.save();
                        JsonObject state = new JsonObject();
                        state.addProperty("type", "config");
                        state.addProperty("logPlayerMovementPackets", PacketSpyConfig.logPlayerMovementPackets);
                        state.add("silencedPackets", GSON.toJsonTree(PacketSpyConfig.silencedPackets));
                        this.broadcast(GSON.toJson(state));
                        PacketspyClient.LOGGER.info("Updated logPlayerMovementPackets to " + PacketSpyConfig.logPlayerMovementPackets);
                    }
                } else if ("silencePacket".equals(action)) {
                    if (json.has("packetName")) {
                        String packetName = json.get("packetName").getAsString();
                        if (!PacketSpyConfig.silencedPackets.contains(packetName)) {
                            PacketSpyConfig.silencedPackets.add(packetName);
                            PacketSpyConfig.save();
                            JsonObject state = new JsonObject();
                            state.addProperty("type", "config");
                            state.addProperty("logPlayerMovementPackets", PacketSpyConfig.logPlayerMovementPackets);
                            state.add("silencedPackets", GSON.toJsonTree(PacketSpyConfig.silencedPackets));
                            this.broadcast(GSON.toJson(state));
                        }
                    }
                } else if ("unsilencePacket".equals(action) && json.has("packetName")) {
                    String packetName = json.get("packetName").getAsString();
                    if (PacketSpyConfig.silencedPackets.remove(packetName)) {
                        PacketSpyConfig.save();
                        JsonObject state = new JsonObject();
                        state.addProperty("type", "config");
                        state.addProperty("logPlayerMovementPackets", PacketSpyConfig.logPlayerMovementPackets);
                        state.add("silencedPackets", GSON.toJsonTree(PacketSpyConfig.silencedPackets));
                        this.broadcast(GSON.toJson(state));
                    }
                }
            }
        } catch (Exception e) {
            PacketspyClient.LOGGER.error("Failed to process message: " + message, e);
        }

    }

    public void onError(WebSocket conn, Exception ex) {
        PacketspyClient.LOGGER.error("WebSocket error", ex);
    }

    public void onStart() {
        PacketspyClient.LOGGER.info("WebSocket server started on port: " + this.getPort());
    }
}
