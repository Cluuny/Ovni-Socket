package co.edu.uptc.application.model;

import com.google.gson.JsonObject;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OVNI {
    private int x;
    private int y;
    private int speed;
    private boolean crashed;
    private int angle;

    public OVNI(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.crashed = false;
        this.angle = (int) (Math.random() * 360);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("x", this.x);
        json.addProperty("y", this.y);
        json.addProperty("speed", this.speed);
        json.addProperty("crashed", this.crashed);
        json.addProperty("angle", this.angle);
        return json;
    }
}
