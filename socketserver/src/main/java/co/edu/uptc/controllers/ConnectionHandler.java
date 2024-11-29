package co.edu.uptc.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import co.edu.uptc.application.model.OVNI;
import co.edu.uptc.application.model.OVNIManager;
import co.edu.uptc.application.model.SimulationEngine;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ConnectionHandler implements Runnable {
    private final Socket connectionSocket;
    private final SimulationEngine simulationEngine;
    private DataInputStream reader;
    private DataOutputStream writer;
    private boolean running;
    private String clientName;

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
                JsonObject dimensions = new JsonObject();

                switch (action) {
                    case "registerName":
                        clientName = request.get("name").getAsString();
                        System.out.println("Cliente registrado: " + clientName);

                        JsonObject dimensionResponse = new JsonObject();
                        dimensionResponse.addProperty("width", 800);
                        dimensionResponse.addProperty("height", 600);
                        writer.writeUTF(gson.toJson(dimensionResponse)); // Enviar dimensiones correctamente
                        break;

                    case "getStatus":
                        JsonObject statusResponse = new JsonObject();
                        statusResponse.add("status", gson.toJsonTree(simulationEngine.getStatus()));
                        writer.writeUTF(gson.toJson(statusResponse)); // Envía un objeto JSON completo
                        break;

                    case "getDimensions":
                        dimensions.addProperty("width", 800);
                        dimensions.addProperty("height", 600);
                        this.writer.writeUTF(gson.toJson(dimensions));
                        break;

                    case "selectOvni":
                        int ovniIndex = request.get("ovniId").getAsInt(); // Usamos el id del OVNI
                        boolean deselect = request.get("deselect").getAsBoolean();
                        if (deselect) {
                            simulationEngine.getOvniManager().selectOvniById(ovniIndex, null); // Deseleccionar
                        } else {
                            simulationEngine.getOvniManager().selectOvniById(ovniIndex, clientName); // Seleccionar
                        }
                        break;

                    case "setCustomPath":
                        // Recibir la trayectoria personalizada
                        int selectedOvniId = request.get("ovniId").getAsInt(); // Usar el ID
                        JsonArray trajectoryJson = request.get("trajectory").getAsJsonArray();
                        List<Point> newPath = new ArrayList<>();

                        // Convertir los puntos de la trayectoria
                        for (int i = 0; i < trajectoryJson.size(); i++) {
                            JsonObject pointJson = trajectoryJson.get(i).getAsJsonObject();
                            int x = pointJson.get("x").getAsInt();
                            int y = pointJson.get("y").getAsInt();
                            newPath.add(new Point(x, y));
                        }

                        // Establecer la nueva trayectoria personalizada en el OVNI
                        simulationEngine.getOvniManager().setCustomPathById(selectedOvniId, newPath);
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
                JsonObject status = new JsonObject();
                OVNIManager ovniManager = simulationEngine.getOvniManager();

                status.addProperty("movingCount", ovniManager.getMovingCount());
                status.addProperty("crashedCount", ovniManager.getCrashedCount());

                JsonArray ovnisArray = new JsonArray();
                for (OVNI ovni : ovniManager.getOvnis()) {
                    ovnisArray.add(ovni.toJson());
                }
                status.add("ovnis", ovnisArray);

                synchronized (writer) {
                    String message = gson.toJson(status) + "\n"; // Añade \n como delimitador
                    writer.write(message.getBytes("UTF-8")); // Envía como bytes UTF-8
                    writer.flush(); // Asegura el envío
                }
                Thread.sleep(50); // Intervalo entre mensajes
            }
        } catch (IOException | InterruptedException e) {
            closeConnection();
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
