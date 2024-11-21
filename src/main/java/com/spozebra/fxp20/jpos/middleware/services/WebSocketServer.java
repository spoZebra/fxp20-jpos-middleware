package com.spozebra.fxp20.jpos.middleware.services;

import java.io.*;
import java.util.concurrent.*;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;

@ServerEndpoint("/fxp20")
public class WebSocketServer {
    private static IWebSocketListener listener;
    private static final CopyOnWriteArrayList<Session> clients = new CopyOnWriteArrayList<>();

   // Setter to allow setting the listener later
   public static void setListener(IWebSocketListener l) {
        listener = l;
    }
    
    @OnOpen
    public void onOpen(Session session) {
        clients.add(session);
        System.out.println("New client connected: " + session.getId());
        if (listener != null) {
            listener.onClientConnected(session);
        }
    }
    @OnMessage
    public void onMessage(String message, Session session) {
        System.out.println("Received from client " + session.getId() + ": " + message);

        if (listener != null) {
            listener.onMessageReceived(session, message);
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        clients.remove(session);
        System.out.println("Client disconnected: " + session.getId());

        if (listener != null) {
            listener.onClientDisconnected(session);
        }
    }
    
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("Error on client " + session.getId() + ": " + throwable.getMessage());
    }


    public static void sendMessageToClient(Session client, String message) {
        if (client.isOpen()) {
            try {
                client.getBasicRemote().sendText(message);
            } catch (IOException e) {
                System.err.println("Error sending message to client: " + client.getId());
            }
        }
    }

    public static void broadcastMessage(String message) {
        for (Session client : clients) {
            if (client.isOpen()) {
                try {
                    client.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    System.err.println("Error sending message to client: " + client.getId());
                }
            }
        }
    }
}