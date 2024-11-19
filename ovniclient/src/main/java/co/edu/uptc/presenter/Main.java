package co.edu.uptc.presenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import co.edu.uptc.model.ConnectionHandler;
import co.edu.uptc.view.View;

public class Main {
    private final ConnectionHandler model;
    private final View view;

    public Main(ConnectionHandler model, View view) {
        this.model = model;
        this.view = view;

        view.getConnectButton().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
    }

    private void connectToServer() {
        try {
            model.connect(view.getHost(), view.getPort());
            view.appendMessage("Connected to server.");
            startListeningForMessages();
        } catch (IOException e) {
            view.appendMessage("Error: " + e.getMessage());
        }
    }

    private void startListeningForMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    String message = model.receiveMessage();
                    view.appendMessage("Server: " + message);
                }
            } catch (IOException e) {
                view.appendMessage("Connection lost: " + e.getMessage());
            }
        }).start();
    }

    public static void main(String[] args) {
        ConnectionHandler model = new ConnectionHandler();
        View view = new View();
        new Main(model, view);
        view.setVisible(true);
    }
}