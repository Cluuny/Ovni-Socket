package co.edu.uptc.application.model.ovni;

import com.google.gson.JsonObject;

import co.edu.uptc.application.model.client.ClientManager;
import co.edu.uptc.controllers.ServerLogger;
import lombok.Getter;

import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class SimulationEngine {
    private final CopyOnWriteArrayList<OVNI> ovnis;
    private final OVNIManager ovniManager;
    private final ClientManager clientManager;
    private final int areaWidth;
    private final int areaHeight;
    private Thread simulationThread;

    public SimulationEngine(int areaWidth, int areaHeight, int destinationX, int destinationY, int destinationRadius) {
        this.ovnis = new CopyOnWriteArrayList<>();
        this.clientManager = new ClientManager();
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
                try {
                    ovniManager.updatePositions(areaWidth, areaHeight);
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
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
        status.add("destX", ovniManager.intToJsonElement(ovniManager.getDestinationX()));
        status.add("destY", ovniManager.intToJsonElement(ovniManager.getDestinationY()));
        status.add("destR", ovniManager.intToJsonElement(ovniManager.getDestinationRadius()));
        ServerLogger.log(status, "info");
        return status;
    }
}
