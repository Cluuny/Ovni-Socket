package co.edu.uptc.application.model.ovni;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OVNI {
    private static final AtomicInteger idGenerator = new AtomicInteger(0);

    private int id;
    private int x;
    private int y;
    private int speed;
    private boolean crashed;
    private int angle;
    private boolean hasDestination;
    private int destinationX;
    private int destinationY;
    private List<Point> customPath;
    private String clientName;

    public OVNI(int x, int y, int speed) {
        this.id = idGenerator.incrementAndGet();
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.crashed = false;
        this.angle = (int) (Math.random() * 360);
        this.customPath = new ArrayList<>();
    }

    public boolean hasCustomPath() {
        return !customPath.isEmpty();
    }

    public void setCustomPath(List<Point> customPath) {
        this.customPath = customPath;
    }

    public boolean hasDestination() {
        return hasDestination;
    }

    public int getDestinationX() {
        return destinationX;
    }

    public int getDestinationY() {
        return destinationY;
    }

    public void setDestination(int x, int y) {
        this.destinationX = x;
        this.destinationY = y;
        this.hasDestination = true;
    }

    public void clearDestination() {
        this.hasDestination = false;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
        if (crashed) {
            OVNIManager.incrementCrashedCount();
        }
        this.clearDestination();
        this.customPath.clear();
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("id", this.id);
        json.addProperty("x", this.x);
        json.addProperty("y", this.y);
        json.addProperty("speed", this.speed);
        json.addProperty("crashed", this.crashed);
        json.addProperty("angle", this.angle);
        json.addProperty("clientName", this.clientName);
        return json;
    }

    @Override
    public String toString() {
        return "OVNI{" +
                "id=" + id +
                ", x=" + x +
                ", y=" + y +
                ", speed=" + speed +
                ", crashed=" + crashed +
                ", angle=" + angle +
                ", hasDestination=" + hasDestination +
                ", destinationX=" + destinationX +
                ", destinationY=" + destinationY +
                ", customPath=" + customPath +
                ", clientName='" + clientName + '\'' +
                '}';
    }
}
