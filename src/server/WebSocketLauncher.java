package server;
// File: WebSocketLauncher.java
import org.glassfish.tyrus.server.Server;
import jakarta.websocket.server.ServerEndpointConfig;

public class WebSocketLauncher {
    private static Server server;

    public static void startServer() throws Exception {
        server = new Server("0.0.0.0", 8080, "/", null, SyncWebSocketServer.class);

        server.start();
        System.out.println("WebSocket server started at ws://0.0.0.0:8080/sync");
    }

    public static void stopServer() throws Exception {
        if (server != null) {
            server.stop();
        }
    }
}