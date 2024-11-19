package co.edu.uptc.application.model;

import com.google.gson.JsonArray;

import java.util.concurrent.CopyOnWriteArrayList;

public class OVNIManager {
    private CopyOnWriteArrayList<OVNI> ovnis;

    public OVNIManager(CopyOnWriteArrayList<OVNI> ovnis, int destinationX, int destinationY, int destinationRadius) {
        this.ovnis = ovnis;
    }

    public int getMovingCount() {
        return (int) ovnis.stream().filter(ovni -> !ovni.isCrashed()).count();
    }

    public int getCrashedCount() {
        return (int) ovnis.stream().filter(OVNI::isCrashed).count();
    }

    public JsonArray getAllOvnisAsJson() {
        JsonArray jsonArray = new JsonArray();
        for (OVNI ovni : ovnis) {
            jsonArray.add(ovni.toJson());
        }
        return jsonArray;
    }
}
