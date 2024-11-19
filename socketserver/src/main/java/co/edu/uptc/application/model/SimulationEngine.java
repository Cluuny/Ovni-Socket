package co.edu.uptc.application.model;

import com.google.gson.JsonObject;

import java.util.concurrent.CopyOnWriteArrayList;

public class SimulationEngine {
    private CopyOnWriteArrayList<OVNI> ovnis;
    private SimulationParameters parameters;
    private OVNIManager ovniManager;

    public SimulationEngine(int destinationX, int destinationY, int destinationRadius) {
        this.ovnis = new CopyOnWriteArrayList<>();
        this.ovniManager = new OVNIManager(ovnis, destinationX, destinationY, destinationRadius);
    }

    public void startSimulation(int numOvnis, int interval, int speed) {
        parameters = new SimulationParameters(numOvnis, interval, speed);
        for (int i = 0; i < numOvnis; i++) {
            int x = (int) (Math.random() * 800);
            int y = (int) (Math.random() * 600);
            OVNI ovni = new OVNI(x, y, speed);
            ovnis.add(ovni);
        }
    }

    public void stopSimulation() {
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
