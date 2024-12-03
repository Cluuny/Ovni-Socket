package co.edu.uptc.view;

import javax.swing.*;

import co.edu.uptc.model.ConnectionHandler;
import co.edu.uptc.model.OVNI;
import co.edu.uptc.presenter.Main;
import lombok.Getter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class OVNIPanel extends JPanel {
    private Main main = new Main();
    private ConnectionHandler connectionHandler;
    private List<OVNI> ovnis = new ArrayList<>();
    private List<Point> trajectory = new ArrayList<>();
    private OVNI selectedOVNI;
    private final Map<String, Color> clientColors = new HashMap<>();
    private final Color[] availableColors = { Color.BLUE, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };
    private int colorIndex = 0;
    private Point destination;

    public OVNIPanel(ConnectionHandler model) {
        this.connectionHandler = model;
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                selectOVNI(e.getX(), e.getY());
                if (selectedOVNI != null) {
                    trajectory.clear();
                    trajectory.add(e.getPoint());
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectedOVNI != null && !trajectory.isEmpty()) {
                    sendTrajectoryToServer();
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (selectedOVNI != null) {
                    trajectory.add(e.getPoint());
                    repaint();
                }
            }
        });
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

            if (ovni == selectedOVNI) {
                g.setColor(Color.YELLOW);
                g.fillOval(ovni.getX() - 5, ovni.getY() - 5, 20, 20);
            }

            if (ovni.getClientName() != null) {
                g.setColor(getColorForClient(ovni.getClientName()));
                g.drawString(ovni.getClientName(), ovni.getX() + 15, ovni.getY() - 5);
            }

            if (ovni.hasCustomPath()) {
                g.setColor(Color.CYAN);
                List<Point> customPath = ovni.getCustomPath();
                for (int i = 0; i < customPath.size() - 1; i++) {
                    Point start = customPath.get(i);
                    Point end = customPath.get(i + 1);
                    g.drawLine(start.x, start.y, end.x, end.y);
                }
            }
        }

        if (destination != null) {
            g.setColor(Color.BLACK);
            g.fillOval(destination.x - 5, destination.y - 5, 10, 10);
        }

        if (!trajectory.isEmpty()) {
            g.setColor(Color.BLUE);
            for (int i = 0; i < trajectory.size() - 1; i++) {
                Point start = trajectory.get(i);
                Point end = trajectory.get(i + 1);
                g.drawLine(start.x, start.y, end.x, end.y);
            }
        }
    }

    public void changeSpeed(OVNI selectedOvni, int speed) {
        try {
            main.sendSpeedChange(selectedOvni, speed, connectionHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendTrajectoryToServer() {
        if (selectedOVNI != null && !trajectory.isEmpty()) {
            try {
                main.sendTrajectoryToServer(selectedOVNI, trajectory, connectionHandler);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void selectOVNI(int mouseX, int mouseY) {
        OVNI previouslySelected = selectedOVNI;
        selectedOVNI = null;

        for (OVNI ovni : ovnis) {
            if (Math.abs(ovni.getX() - mouseX) <= 10 && Math.abs(ovni.getY() - mouseY) <= 10) {
                selectedOVNI = ovni;
                break;
            }
        }

        try {
            if (selectedOVNI != null) {
                if (previouslySelected != null && !selectedOVNI.equals(previouslySelected)) {
                    main.sendSelectRequest(previouslySelected, "deselect", this.connectionHandler);
                }
                main.sendSelectRequest(selectedOVNI, "select", this.connectionHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        repaint();
    }

    private Color getColorForClient(String clientName) {
        if (!clientColors.containsKey(clientName)) {
            clientColors.put(clientName, availableColors[colorIndex]);
            colorIndex = (colorIndex + 1) % availableColors.length;
        }
        return clientColors.get(clientName);
    }

    private void removeCrashedOvnis() {
        ovnis.removeIf(ovni -> ovni.isCrashed());
    }

    public void setOvnis(List<OVNI> ovnis) {
        this.ovnis = new ArrayList<>(ovnis);
        removeCrashedOvnis();
        repaint();
    }
}
