package co.edu.uptc.presenter;

import co.edu.uptc.model.ConnectionHandler;
import co.edu.uptc.model.OVNI;
import co.edu.uptc.view.ConnectionView;
import co.edu.uptc.view.SimulationView;
import lombok.Getter;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.swing.*;

import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class Main {
    private ConnectionHandler model;
    private ConnectionView connectionView;
    private SimulationView simulationView;

    public Main() {
        model = new ConnectionHandler();
        connectionView = new ConnectionView(this::connectToServer);
        connectionView.setVisible(true);
    }

    private void connectToServer(String host, int port, String name) {
        try {
            model.connect(host, port);
            model.sendName(name);

            SwingUtilities.invokeLater(() -> {
                if (simulationView == null) {
                    connectionView.dispose();
                    try {
                        simulationView = new SimulationView(800, 600, model.clone(), this::startSimulationUpdates);
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                    }
                    simulationView.setVisible(true);
                }
            });

            new Thread(() -> {
                boolean dimensionsReceived = false;
                while (true) {
                    try {
                        String response = model.receiveMessage();

                        if (response.startsWith("{") && response.endsWith("}")) {
                            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();

                            if (jsonResponse.has("width") && jsonResponse.has("height") && !dimensionsReceived) {
                                int width = jsonResponse.get("width").getAsInt();
                                int height = jsonResponse.get("height").getAsInt();
                                dimensionsReceived = true;

                                SwingUtilities.invokeLater(() -> {
                                    if (simulationView != null) {
                                        simulationView.setSize(width, height);
                                    }
                                });

                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    if (simulationView != null) {
                                        simulationView.updateSimulation(this.convertToOvnis(jsonResponse));
                                        simulationView.updateStats(jsonResponse);
                                    }
                                });
                            }
                        } else {
                            System.err.println("Respuesta no es un JSON válido: " + response);
                        }
                    } catch (IOException e) {
                        System.err.println("Error en la recepción de datos: " + e.getMessage());
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (IOException e) {
            handleConnectionError(e);
        }
    }

    private void startSimulationUpdates() {
        new Thread(() -> {
            try {
                while (true) {
                    String jsonData = this.model.receiveMessage();
                    JsonObject simulationData = JsonParser.parseString(jsonData).getAsJsonObject();

                    SwingUtilities.invokeLater(() -> {
                        if (simulationView != null) {
                            simulationView.updateSimulation(this.convertToOvnis(simulationData));
                            simulationView.updateStats(simulationData);
                        }
                    });
                }
            } catch (IOException e) {
                System.err.println("Error en la recepción de datos de simulación: " + e.getMessage());
            }
        }).start();
    }

    private void handleConnectionError(Exception e) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    simulationView != null ? simulationView : connectionView,
                    "Error de conexión: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            if (simulationView != null)
                simulationView.dispose();
            if (connectionView != null)
                connectionView.dispose();
        });
    }

    private List<OVNI> convertToOvnis(JsonObject simulationData) {
        JsonArray ovnisJson = simulationData.getAsJsonArray("ovnis");
        List<OVNI> ovnis = new ArrayList<>();

        for (int i = 0; i < ovnisJson.size(); i++) {
            JsonObject ovniJson = ovnisJson.get(i).getAsJsonObject();
            OVNI ovni = new OVNI(
                    ovniJson.get("x").getAsInt(),
                    ovniJson.get("y").getAsInt(),
                    ovniJson.get("speed").getAsInt());

            ovni.setAngle(ovniJson.get("angle").getAsInt());
            ovni.setCrashed(ovniJson.get("crashed").getAsBoolean());
            ovni.setId(ovniJson.get("id").getAsInt());

            if (ovniJson.has("clientName") && !ovniJson.get("clientName").isJsonNull()) {
                ovni.setClientName(ovniJson.get("clientName").getAsString());
            } else {
                ovni.setClientName("");
            }

            ovnis.add(ovni.clone());
        }
        return ovnis;
    }

    public void sendSpeedChange(OVNI selectedOVNI, int speed, ConnectionHandler modelFromClient) throws IOException {
        JsonObject changeSpeedRequest = new JsonObject();
        changeSpeedRequest.addProperty("action", "changeSpeed");
        changeSpeedRequest.addProperty("ovniId", selectedOVNI.getId());
        changeSpeedRequest.addProperty("newSpeed", speed);
        modelFromClient.sendMessage(changeSpeedRequest.toString());
    }

    public boolean sendSelectRequest(OVNI ovni, String action, ConnectionHandler modelFromClient) throws IOException {
        boolean wasSended = false;
        if (action.equals("deselect")) {
            JsonObject deselectRequest = new JsonObject();
            deselectRequest.addProperty("action", "selectOvni");
            deselectRequest.addProperty("ovniId", ovni.getId());
            deselectRequest.addProperty("deselect", true);
            modelFromClient.sendMessage(deselectRequest.toString());
            wasSended = true;
        } else {
            JsonObject selectRequest = new JsonObject();
            selectRequest.addProperty("action", "selectOvni");
            selectRequest.addProperty("ovniId", ovni.getId()); // Usamos el ID
            selectRequest.addProperty("deselect", false);
            modelFromClient.sendMessage(selectRequest.toString());
            wasSended = true;
        }
        return wasSended;
    }

    public void sendTrajectoryToServer(OVNI selectedOVNI, List<Point> trajectory, ConnectionHandler modelFromClient)
            throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "setCustomPath");
        request.addProperty("ovniId", selectedOVNI.getId()); // Usamos el ID

        // Convertir la trayectoria a JSON
        JsonArray trajectoryJson = new JsonArray();
        for (Point point : trajectory) {
            JsonObject pointJson = new JsonObject();
            pointJson.addProperty("x", point.x);
            pointJson.addProperty("y", point.y);
            trajectoryJson.add(pointJson);
        }

        request.add("trajectory", trajectoryJson);
        System.out.println(request.toString());
        modelFromClient.sendMessage(request.toString());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
