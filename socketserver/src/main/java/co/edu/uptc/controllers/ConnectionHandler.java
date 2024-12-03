package co.edu.uptc.controllers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import co.edu.uptc.application.model.client.Client;
import co.edu.uptc.application.model.client.RegularClient;
import co.edu.uptc.application.model.ovni.OVNI;
import co.edu.uptc.application.model.ovni.OVNIManager;
import co.edu.uptc.application.model.ovni.SimulationEngine;

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
                JsonObject request = this.readRequest(gson);

                JsonObject response = new JsonObject();
                String action = request.get("action").getAsString();
                JsonObject dimensions = new JsonObject();

                switch (action) {
                    case "registerRegularClient":
                        ServerLogger.log(clientName + ": registra", "info");
                        clientName = request.get("name").getAsString();
                        this.simulationEngine.getClientManager().addClient(new RegularClient(clientName));
                        JsonObject dimensionResponse = new JsonObject();
                        dimensionResponse.addProperty("width", 800);
                        dimensionResponse.addProperty("height", 600);
                        System.out.println(gson.toJson(dimensionResponse));
                        writer.writeUTF(gson.toJson(dimensionResponse));
                        break;

                    case "getStatus":
                        ServerLogger.log(clientName + ": actualiza", "info");
                        JsonObject statusResponse = new JsonObject();
                        statusResponse.add("status", gson.toJsonTree(simulationEngine.getStatus()));
                        writer.writeUTF(gson.toJson(statusResponse));
                        break;

                    case "getDimensions":
                        ServerLogger.log(clientName + ": enruta", "info");
                        dimensions.addProperty("width", 800);
                        dimensions.addProperty("height", 600);
                        System.out.println(gson.toJson(dimensions));
                        this.writer.writeUTF(gson.toJson(dimensions));
                        break;

                    case "selectOvni":
                        ServerLogger.log(clientName + ": " + request + ": selecciona", "info");
                        int ovniIndexForSelect = request.get("ovniId").getAsInt();
                        boolean deselect = request.get("deselect").getAsBoolean();
                        if (deselect) {
                            simulationEngine.getOvniManager().selectOvniById(ovniIndexForSelect, null);
                        } else {
                            simulationEngine.getOvniManager().selectOvniById(ovniIndexForSelect, clientName);
                        }
                        break;
                    case "changeSpeed":
                        System.out.println(request);
                        int ovniIndexForSpeedChange = request.get("ovniId").getAsInt();
                        int newSpeed = request.get("newSpeed").getAsInt();
                        simulationEngine.getOvniManager().changeSpeed(ovniIndexForSpeedChange, newSpeed);
                        break;
                    case "setCustomPath":
                        ServerLogger.log(clientName + ": enruta", "info");
                        int selectedOvniId = request.get("ovniId").getAsInt(); // Usar el ID
                        JsonArray trajectoryJson = request.get("trajectory").getAsJsonArray();
                        List<Point> newPath = new ArrayList<>();
                        for (int i = 0; i < trajectoryJson.size(); i++) {
                            JsonObject pointJson = trajectoryJson.get(i).getAsJsonObject();
                            int x = pointJson.get("x").getAsInt();
                            int y = pointJson.get("y").getAsInt();
                            newPath.add(new Point(x, y));
                        }
                        simulationEngine.getOvniManager().setCustomPathById(selectedOvniId, newPath);
                        break;

                    default:
                        response.addProperty("error", "Invalid action");
                        ServerLogger.log(response.toString(), "error");
                        this.writer.writeUTF(gson.toJson(response));
                }
            }
        } catch (Exception e) {
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
                    String message = gson.toJson(status) + "\n";
                    writer.write(message.getBytes("UTF-8"));
                    writer.flush();
                }
                Thread.sleep(50);
            }
        } catch (IOException | InterruptedException e) {
            closeConnection();
        }
    }

    public JsonObject readRequest(Gson gson) {
        try {
            String input = this.reader.readUTF();
            return gson.fromJson(input, JsonObject.class);
        } catch (IOException e) {
            this.closeConnection();
        }
        return null;
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
