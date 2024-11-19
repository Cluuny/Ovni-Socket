package co.edu.uptc.controllers;

import java.io.IOException;

public interface MessageListener {
    void onMessageReceived();

    void sendMessage(String message) throws IOException;

    void broadcastMessage(String message) throws IOException;
}
