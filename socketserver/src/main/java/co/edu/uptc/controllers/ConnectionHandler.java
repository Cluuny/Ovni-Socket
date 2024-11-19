package co.edu.uptc.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import co.edu.uptc.application.model.SimulationEngine;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ConnectionHandler implements Runnable {
    private final Socket connectionSocket;
    private final SimulationEngine simulationEngine;
    private DataInputStream reader;
    private DataOutputStream writer;
    private boolean running;

    public ConnectionHandler(Socket connectionSocket, SimulationEngine simulationEngine) {
        this.connectionSocket = connectionSocket;
        this.simulationEngine = simulationEngine;
        this.running = true;
        try {
            this.reader = new DataInputStream(this.connectionSocket.getInputStream());
            this.writer = new DataOutputStream(this.connectionSocket.getOutputStream());
        } catch (IOException e) {
            this.closeConnection();
        }
    }

    @Override
    public void run() {
        Gson gson = new Gson();
        new Thread(this::sendSimulationUpdates).start();

        try {
            while (this.connectionSocket.isConnected() && running) {
                String input = this.reader.readUTF();
                JsonObject request = gson.fromJson(input, JsonObject.class);

                JsonObject response = new JsonObject();
                String action = request.get("action").getAsString();
                switch (action) {
                    case "getStatus":
                        response.add("status", gson.toJsonTree(simulationEngine.getStatus()));
                        this.writer.writeUTF(gson.toJson(response));
                        break;

                    default:
                        response.addProperty("error", "Invalid action");
                        this.writer.writeUTF(gson.toJson(response));
                }
            }
        } catch (IOException e) {
            this.closeConnection();
        }
    }

    private void sendSimulationUpdates() {
        Gson gson = new Gson();
        try {
            while (running) {
                String simulationState = gson.toJson(simulationEngine.getStatus());
                this.writer.writeUTF(simulationState);
                Thread.sleep(1000); // Enviar actualizaciones cada segundo.
            }
        } catch (IOException | InterruptedException e) {
            this.closeConnection();
        }
    }

    public void closeConnection() {
        this.running = false;
        try {
            this.reader.close();
            this.writer.close();
            this.connectionSocket.close();
        } catch (IOException ignore) {
        }
    }
}
