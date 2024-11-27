package co.edu.uptc.presenter;

import co.edu.uptc.model.ConnectionHandler;
import co.edu.uptc.view.ConnectionView;
import co.edu.uptc.view.SimulationView;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import javax.swing.*;
import java.io.IOException;
import java.io.StringReader;

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
    
            // Mover cierre de ConnectionView aquí
            SwingUtilities.invokeLater(() -> {
                if (simulationView == null) {
                    connectionView.dispose(); // Cerrar ventana de conexión
                    simulationView = new SimulationView(800, 600, model, this::startSimulationUpdates); // Crear SimulationView con dimensiones predeterminadas
                    simulationView.setVisible(true); // Mostrar la nueva ventana
                }
            });
    
            new Thread(() -> {
                boolean dimensionsReceived = false; // Para verificar si se recibieron dimensiones
                while (true) {
                    try {
                        String response = model.receiveMessage();
                        System.out.println("Respuesta recibida: " + response);
    
                        if (response.startsWith("{") && response.endsWith("}")) {
                            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
    
                            if (jsonResponse.has("width") && jsonResponse.has("height") && !dimensionsReceived) {
                                int width = jsonResponse.get("width").getAsInt();
                                int height = jsonResponse.get("height").getAsInt();
                                dimensionsReceived = true;
    
                                SwingUtilities.invokeLater(() -> {
                                    if (simulationView != null) {
                                        simulationView.setSize(width, height); // Actualizar dimensiones dinámicamente
                                        System.out.println("Actualizando dimensiones de SimulationView: " + width + "x" + height);
                                    }
                                });
    
                            } else {
                                SwingUtilities.invokeLater(() -> {
                                    if (simulationView != null) {
                                        simulationView.updateSimulation(jsonResponse);
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
                        System.err.println("Error inesperado: " + e.getMessage());
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
                    String jsonData = model.receiveMessage();
                    JsonObject simulationData = JsonParser.parseString(jsonData).getAsJsonObject();

                    SwingUtilities.invokeLater(() -> {
                        if (simulationView != null) {
                            simulationView.updateSimulation(simulationData);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
