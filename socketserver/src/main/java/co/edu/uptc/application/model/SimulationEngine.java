package co.edu.uptc.application.model;

import com.google.gson.JsonObject;

import java.util.concurrent.CopyOnWriteArrayList;

public class SimulationEngine {
    private final CopyOnWriteArrayList<OVNI> ovnis;
    private final OVNIManager ovniManager;
    private final int areaWidth;
    private final int areaHeight;
    private Thread simulationThread;

    public SimulationEngine(int areaWidth, int areaHeight, int destinationX, int destinationY, int destinationRadius) {
        this.ovnis = new CopyOnWriteArrayList<>();
        this.areaWidth = areaWidth;
        this.areaHeight = areaHeight;
        this.ovniManager = new OVNIManager(ovnis, destinationX, destinationY, destinationRadius);
    }

    public void startSimulation(int numOvnis, int interval, int speed) {
        for (int i = 0; i < numOvnis; i++) {
            int x = (int) (Math.random() * areaWidth);
            int y = (int) (Math.random() * areaHeight);
            OVNI ovni = new OVNI(x, y, speed);
            ovnis.add(ovni);
        }

        simulationThread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                ovniManager.updatePositions(areaWidth, areaHeight);

                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        simulationThread.start();
    }

    public void stopSimulation() {
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
        ovnis.clear();
    }

    public JsonObject getStatus() {
        JsonObject status = new JsonObject();
        status.addProperty("movingCount", ovniManager.getMovingCount());
        status.addProperty("crashedCount", ovniManager.getCrashedCount());
        status.add("ovnis", ovniManager.getAllOvnisAsJson());
        return status;
    }
}

