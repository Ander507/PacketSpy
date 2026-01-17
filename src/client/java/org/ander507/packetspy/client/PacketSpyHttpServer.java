package org.ander507.packetspy.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class PacketSpyHttpServer {
    private HttpServer server;

    public void start(int port) {
        try {
            // Bind to "0.0.0.0" to allow connections from LAN
            this.server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            this.server.createContext("/", new StaticFileHandler());
            this.server.setExecutor(null);
            this.server.start();

            String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
            PacketspyClient.LOGGER.info("HTTP Server started.");
            PacketspyClient.LOGGER.info(">> Desktop: http://localhost:" + port);
            PacketspyClient.LOGGER.info(">> Mobile : http://" + localIp + ":" + port);
        } catch (IOException e) {
            PacketspyClient.LOGGER.error("Failed to start HTTP server", e);
        }
    }

    @SuppressWarnings("unused")
    public void stop() {
        if (this.server != null) {
            this.server.stop(0);
        }
    }

    static class StaticFileHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            String path = t.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            InputStream is = PacketspyClient.class.getResourceAsStream("/web" + path);
            if (is == null) {
                String response = "404 Not Found";
                t.sendResponseHeaders(404, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            } else {
                byte[] bytes = is.readAllBytes();
                is.close();
                t.sendResponseHeaders(200, bytes.length);
                OutputStream os = t.getResponseBody();
                os.write(bytes);
                os.close();
            }
        }
    }
}
