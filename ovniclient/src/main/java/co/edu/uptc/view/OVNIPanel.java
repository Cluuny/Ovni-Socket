package co.edu.uptc.view;

import javax.swing.*;

import com.google.gson.JsonObject;

import co.edu.uptc.model.ConnectionHandler;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class OVNIPanel extends JPanel {
    private List<OVNI> ovnis = new ArrayList<>();
    private OVNI selectedOVNI;
    private ConnectionHandler connectionHandler;
    private final Map<String, Color> clientColors = new HashMap<>();
    private final Color[] availableColors = { Color.BLUE, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };
    private int colorIndex = 0;

    public void setOvnis(List<OVNI> ovnis) {
        this.ovnis = ovnis;
        repaint();
    }

    public OVNI getSelectedOVNI() {
        return selectedOVNI;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (OVNI ovni : ovnis) {
            if (!ovni.isCrashed()) {
                g.setColor(Color.GREEN);
            } else {
                g.setColor(Color.RED);
            }
            g.fillOval(ovni.getX(), ovni.getY(), 10, 10);

            if (ovni.getClientName() != null) {
                g.setColor(getColorForClient(ovni.getClientName())); // Color único para cada cliente
                g.drawString(ovni.getClientName(), ovni.getX() + 15, ovni.getY() - 5); // Dibuja el nombre
            }
        }
    }

    public OVNIPanel(ConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
        // Agrega el MouseListener
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectOVNI(e.getX(), e.getY());
            }
        });
    }

    private void selectOVNI(int mouseX, int mouseY) {
        OVNI previouslySelected = selectedOVNI; // Guardar el OVNI previamente seleccionado
        selectedOVNI = null;

        for (OVNI ovni : ovnis) {
            if (Math.abs(ovni.getX() - mouseX) <= 10 && Math.abs(ovni.getY() - mouseY) <= 10) {
                selectedOVNI = ovni;
                break;
            }
        }

        if (selectedOVNI != null) {
            try {
                if (previouslySelected != null && !selectedOVNI.equals(previouslySelected)) {
                    // Deseleccionar el OVNI anterior en el servidor
                    JsonObject deselectRequest = new JsonObject();
                    deselectRequest.addProperty("action", "selectOvni");
                    deselectRequest.addProperty("ovniIndex", ovnis.indexOf(previouslySelected));
                    deselectRequest.addProperty("deselect", true);
                    connectionHandler.sendMessage(deselectRequest.toString());
                }

                // Seleccionar el nuevo OVNI en el servidor
                JsonObject selectRequest = new JsonObject();
                selectRequest.addProperty("action", "selectOvni");
                selectRequest.addProperty("ovniIndex", ovnis.indexOf(selectedOVNI));
                selectRequest.addProperty("deselect", false);
                connectionHandler.sendMessage(selectRequest.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        repaint();
    }

    private Color getColorForClient(String clientName) {
        if (!clientColors.containsKey(clientName)) {
            clientColors.put(clientName, availableColors[colorIndex]);
            colorIndex = (colorIndex + 1) % availableColors.length; // Cicla entre los colores disponibles
        }
        return clientColors.get(clientName);
    }

    private void displayOVNIDetails(OVNI ovni) {
        if (ovni != null) {
            JOptionPane.showMessageDialog(this,
                    "OVNI seleccionado:\n" +
                            "Posición: (" + ovni.getX() + ", " + ovni.getY() + ")\n" +
                            "Estado: " + (ovni.isCrashed() ? "Estrellado" : "En movimiento"),
                    "Detalles del OVNI", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}