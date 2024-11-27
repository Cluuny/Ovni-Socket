package co.edu.uptc.view;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OVNI {
    private int x;
    private int y;
    private int speed;
    private int angle;
    private boolean crashed;
    private String clientName; 

    // Constructor requerido
    public OVNI(int x, int y, int speed, int angle, boolean crashed) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = angle;
        this.crashed = crashed;
    }

    // Getters y setters para cada atributo
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getSpeed() { return speed; }
    public void setSpeed(int speed) { this.speed = speed; }

    public int getAngle() { return angle; }
    public void setAngle(int angle) { this.angle = angle; }

    public boolean isCrashed() { return crashed; }
    public void setCrashed(boolean crashed) { this.crashed = crashed; }

    // Método para representar al OVNI como JSON (opcional)
    public String toJson() {
        return String.format("{\"x\":%d,\"y\":%d,\"speed\":%d,\"angle\":%d,\"crashed\":%b}",
                x, y, speed, angle, crashed);
    }
}
