package co.edu.uptc.view;

import com.google.gson.JsonObject;

import co.edu.uptc.model.ConnectionHandler;
import co.edu.uptc.model.OVNI;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

public class SimulationView extends JFrame {
    private OVNIPanel ovniPanel;
    private List<OVNI> ovnis = new ArrayList<>();
    private JLabel liveCountLabel;
    private JLabel crashedCountLabel;

    public SimulationView(int width, int height, ConnectionHandler model, SimulationListener listener) {
        setTitle("Simulación OVNI");
        setSize(width, height);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        ovniPanel = new OVNIPanel(model);
        add(ovniPanel, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(1, 2));

        liveCountLabel = new JLabel("OVNIs vivos: 0");
        crashedCountLabel = new JLabel("OVNIs estrellados: 0");

        statsPanel.add(liveCountLabel);
        statsPanel.add(crashedCountLabel);

        add(statsPanel, BorderLayout.SOUTH);

        listener.onSimulationViewReady();
    }

    public void updateSimulation(List<OVNI> ovnis) {
        ovniPanel.setOvnis(ovnis);
        ovniPanel.repaint();
    }

    public void updateStats(JsonObject simulationData) {
        int liveCount = simulationData.get("movingCount").getAsInt();
        int crashedCount = simulationData.get("crashedCount").getAsInt();

        for (OVNI ovni : ovnis) {
            if (ovni.isCrashed()) {
                crashedCount++;
            } else {
                liveCount++;
            }
        }

        liveCountLabel.setText("OVNIs vivos: " + liveCount);
        crashedCountLabel.setText("OVNIs estrellados: " + crashedCount);
    }
}
