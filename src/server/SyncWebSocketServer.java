package server;
// File: SyncWebSocketServer.java

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@ServerEndpoint("/sync")
public class SyncWebSocketServer {

    private static final Set<Session> clients = Collections.synchronizedSet(new HashSet<>());

    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        System.out.println("Client connected: " + session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        clients.remove(session);
        System.out.println("Client disconnected: " + session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received: " + message);
    }

    public static void broadcast(String message) {
        synchronized (clients) {
            for (Session client : clients) {
                try {
                    client.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Failed to send: " + e.getMessage());
                }
            }
        }
    }
}


