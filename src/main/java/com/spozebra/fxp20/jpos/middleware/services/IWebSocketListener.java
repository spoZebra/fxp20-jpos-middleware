package com.spozebra.fxp20.jpos.middleware.services;

import jakarta.websocket.Session;

public interface IWebSocketListener {
    void onClientConnected(Session client);
    void onMessageReceived(Session client, String message);
    void onClientDisconnected(Session client);
}
