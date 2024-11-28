package co.edu.uptc.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import co.edu.uptc.model.ConnectionHandler;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class SimulationView extends JFrame {
    private OVNIPanel ovniPanel;
    private List<OVNI> ovnis = new ArrayList<>();
    private SimulationListener listener;

    private JLabel liveCountLabel; // Para mostrar el número de OVNIs vivos
    private JLabel crashedCountLabel; // Para mostrar el número de OVNIs estrellados

    // Constructor
    public SimulationView(int width, int height, ConnectionHandler connectionHandler, SimulationListener listener) {
        this.listener = listener;
        System.out.println("Constructor de SimulationView llamado con dimensiones: " + width + "x" + height);

        setTitle("Simulación OVNI");
        setSize(width, height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ovniPanel = new OVNIPanel(connectionHandler);
        add(ovniPanel, BorderLayout.CENTER);

        // Crear panel para los contadores
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 2));

        liveCountLabel = new JLabel("OVNIs vivos: 0");
        crashedCountLabel = new JLabel("OVNIs estrellados: 0");

        statsPanel.add(liveCountLabel);
        statsPanel.add(crashedCountLabel);

        add(statsPanel, BorderLayout.SOUTH);

        listener.onSimulationViewReady();
        System.out.println("SimulationView inicializada correctamente.");
    }

    public void updateSimulation(JsonObject simulationData) {
        System.out.println("Actualizando simulación con datos: " + simulationData);

        JsonArray ovnisJson = simulationData.getAsJsonArray("ovnis");
        List<OVNI> ovnis = new ArrayList<>();

        for (int i = 0; i < ovnisJson.size(); i++) {
            JsonObject ovniJson = ovnisJson.get(i).getAsJsonObject();
            OVNI ovni = new OVNI(
                    ovniJson.get("x").getAsInt(),
                    ovniJson.get("y").getAsInt(),
                    ovniJson.get("speed").getAsInt(),
                    ovniJson.get("angle").getAsInt(),
                    ovniJson.get("crashed").getAsBoolean());
            // Si clientName está presente, asignarlo al OVNI
            if (ovniJson.has("clientName")) {
                ovni.setClientName(ovniJson.get("clientName").getAsString());
            }
            ovnis.add(ovni);
        }

        ovniPanel.setOvnis(ovnis); // Actualiza los OVNIs en el panel
        updateStats(simulationData); // Actualiza los contadores de OVNIs vivos y estrellados
        System.out.println("Se actualizó la lista de OVNIs: " + ovnis.size());
        ovniPanel.repaint(); // Redibuja el panel
    }

    private void updateStats(JsonObject simulationData) {
        // Obtener los valores de los contadores directamente desde el servidor
        int liveCount = simulationData.get("movingCount").getAsInt();
        int crashedCount = simulationData.get("crashedCount").getAsInt();

        // Contar los OVNIs vivos (no estrellados)
        for (OVNI ovni : ovnis) {
            if (ovni.isCrashed()) {
                crashedCount++; // Incrementa el contador de OVNIs estrellados
            } else {
                liveCount++; // Incrementa el contador de OVNIs vivos
            }
        }

        // Actualizar las etiquetas de la interfaz
        System.out.println("OVNIs vivos: " + liveCount);
        System.out.println("OVNIs estrellados: " + crashedCount);

        liveCountLabel.setText("OVNIs vivos: " + liveCount);
        crashedCountLabel.setText("OVNIs estrellados: " + crashedCount);
    }

    public interface SimulationListener {
        void onSimulationViewReady();
    }
}
