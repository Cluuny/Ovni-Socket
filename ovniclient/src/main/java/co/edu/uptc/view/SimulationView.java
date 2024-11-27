package co.edu.uptc.view;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import co.edu.uptc.model.ConnectionHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationView extends JFrame {
    private OVNIPanel ovniPanel;
    private List<OVNI> ovnis = new ArrayList<>();
    private SimulationListener listener;

    public SimulationView(int width, int height, ConnectionHandler connectionHandler, SimulationListener listener) {
        this.listener = listener;
        System.out.println("Constructor de SimulationView llamado con dimensiones: " + width + "x" + height);
    
        setTitle("Simulación OVNI");
        setSize(width, height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    
        ovniPanel = new OVNIPanel(connectionHandler);
        add(ovniPanel);
    
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
                ovniJson.get("crashed").getAsBoolean()
            );
            // Si clientName está presente, asignarlo al OVNI
            if (ovniJson.has("clientName")) {
                ovni.setClientName(ovniJson.get("clientName").getAsString());
            }
            ovnis.add(ovni);
        }
    
        ovniPanel.setOvnis(ovnis); // Actualiza los OVNIs en el panel
        System.out.println("Se actualizó la lista de OVNIs: " + ovnis.size());
        ovniPanel.repaint(); // Redibuja el panel
    }
    

    public interface SimulationListener {
        void onSimulationViewReady();
    }
}
