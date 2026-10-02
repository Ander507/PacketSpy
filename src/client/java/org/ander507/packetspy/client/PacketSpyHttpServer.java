package org.ander507.packetspy.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PacketSpyHttpServer {
    private HttpServer server;

    public void start(InetSocketAddress address, int webSocketPort) {
        try {
            this.server = HttpServer.create(address, 0);
            this.server.createContext("/", new StaticFileHandler(webSocketPort));
            this.server.setExecutor(Executors.newSingleThreadExecutor(runnable -> {
                Thread thread = new Thread(runnable, "PacketSpy HTTP");
                thread.setDaemon(true);
                return thread;
            }));
            this.server.start();
            PacketspyClient.LOGGER.info("HTTP Server started.");
            PacketspyClient.LOGGER.info(">> Desktop: http://127.0.0.1:{}", address.getPort());
        } catch (IOException e) {
            PacketspyClient.LOGGER.error("Failed to start HTTP server on port " + address.getPort(), e);
        }
    }

    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
            this.server = null;
        }
    }

    record StaticFileHandler(int webSocketPort) implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                String path = t.getRequestURI().getPath();
                if (path.equals("/")) {
                    path = "/index.html";
                }
                // Only plain file names below /web, nothing like /../
                if (!path.matches("(/[A-Za-z0-9_-][A-Za-z0-9._-]*)+") || path.contains("..")) {
                    send(t, 404, "text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
                    return;
                }

                byte[] bytes;
                try (InputStream is = PacketSpyHttpServer.class.getResourceAsStream("/web" + path)) {
                    if (is == null) {
                        send(t, 404, "text/plain", "404 Not Found".getBytes(StandardCharsets.UTF_8));
                        return;
                    }
                    bytes = is.readAllBytes();
                }
                if (path.equals("/index.html")) {
                    // Tell the page which port the WebSocket is on (configurable in packetspy.json)
                    bytes = new String(bytes, StandardCharsets.UTF_8)
                            .replace("__PACKETSPY_WS_PORT__", String.valueOf(webSocketPort))
                            .getBytes(StandardCharsets.UTF_8);
                }
                send(t, 200, contentType(path), bytes);
            } finally {
                t.close();
            }
        }

        private static void send(HttpExchange t, int status, String contentType, byte[] body) throws IOException {
            t.getResponseHeaders().set("Content-Type", contentType);
            t.getResponseHeaders().set("Cache-Control", "no-cache");
            t.sendResponseHeaders(status, body.length);
            t.getResponseBody().write(body);
        }

        private static String contentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=utf-8";
            if (path.endsWith(".js")) return "text/javascript; charset=utf-8";
            if (path.endsWith(".css")) return "text/css; charset=utf-8";
            if (path.endsWith(".json")) return "application/json";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".svg")) return "image/svg+xml";
            return "application/octet-stream";
        }
    }
}
