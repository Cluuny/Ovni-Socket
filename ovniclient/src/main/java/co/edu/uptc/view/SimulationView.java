package co.edu.uptc.view;

import com.google.gson.JsonObject;

import co.edu.uptc.model.ConnectionHandler;
import co.edu.uptc.model.OVNI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SimulationView extends JFrame {
    private OVNIPanel ovniPanel;
    private List<OVNI> ovnis = new ArrayList<>();
    private JLabel liveCountLabel;
    private JLabel crashedCountLabel;
    private JTextField speedTextField;
    private JButton changeSpeedButton;

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

        add(statsPanel, BorderLayout.NORTH);

        JPanel speedPanel = new JPanel();
        speedPanel.setLayout(new FlowLayout());

        speedTextField = new JTextField(10);
        changeSpeedButton = new JButton("Cambiar velocidad");
        changeSpeedButton.addActionListener(e -> {
            try {
                int newSpeed = Integer.parseInt(speedTextField.getText());
                this.ovniPanel.changeSpeed(this.ovniPanel.getSelectedOVNI(), newSpeed);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Puerto inválido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (NullPointerException npe) {
                JOptionPane.showMessageDialog(this,
                        "Ningún OVNI Seleccionado.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        Dimension preferredSize = new Dimension(150, 30);

        speedTextField.setPreferredSize(preferredSize);
        changeSpeedButton.setPreferredSize(preferredSize);

        speedPanel.add(speedTextField);
        speedPanel.add(changeSpeedButton);

        add(speedPanel, BorderLayout.SOUTH);

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
