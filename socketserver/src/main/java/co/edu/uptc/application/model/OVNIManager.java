package co.edu.uptc.application.model;

import com.google.gson.JsonArray;

import lombok.Getter;

import java.awt.Point;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
public class OVNIManager {
    private final CopyOnWriteArrayList<OVNI> ovnis;
    private final int destinationX;
    private final int destinationY;
    private final int destinationRadius;
    private static int crashedCount = 0; // Contador de OVNIs estrellados

    public OVNIManager(CopyOnWriteArrayList<OVNI> ovnis, int destinationX, int destinationY, int destinationRadius) {
        this.ovnis = ovnis;
        this.destinationX = destinationX;
        this.destinationY = destinationY;
        this.destinationRadius = destinationRadius;
    }

    public synchronized void updatePositions(int areaWidth, int areaHeight) {
        System.out.println("---- Comenzando actualización de posiciones ----");

        ovnis.forEach(ovni -> {
            System.out.println("Antes de mover: " + ovni.toJson());
        });

        ovnis.removeIf(ovni -> {
            if (!ovni.isCrashed()) {
                if (isInDestinationArea(ovni)) {
                    crashedCount++; // Actualización del contador en caso de alcanzar la zona de destino
                    System.out.println("OVNI alcanzó la zona de destino y fue eliminado: " + ovni.toJson());
                    return true; // Eliminar OVNI al alcanzar la zona de destino
                }

                if (ovni.hasCustomPath()) {
                    Point nextPoint = ovni.getCustomPath().get(0);
                    moveOvniTowards(ovni, nextPoint.x, nextPoint.y);

                    if (ovni.getX() == nextPoint.x && ovni.getY() == nextPoint.y) {
                        ovni.getCustomPath().remove(0);
                        System.out.println("OVNI avanzó en su ruta personalizada: " + ovni.toJson());
                    }
                } else if (ovni.hasDestination()) {
                    moveOvniTowards(ovni, ovni.getDestinationX(), ovni.getDestinationY());
                    System.out.println("OVNI moviéndose hacia destino: " + ovni.toJson());
                } else {
                    int newX = ovni.getX() + (int) (ovni.getSpeed() * Math.cos(Math.toRadians(ovni.getAngle())));
                    int newY = ovni.getY() + (int) (ovni.getSpeed() * Math.sin(Math.toRadians(ovni.getAngle())));

                    if (newX < 0 || newX >= areaWidth || newY < 0 || newY >= areaHeight) {
                        crashedCount++; // Actualización del contador en caso de salida del área
                        System.out.println("OVNI salió del área y fue eliminado: " + ovni.toJson());
                        return true; // Eliminar OVNI al salir del área
                    } else {
                        ovni.setX(newX);
                        ovni.setY(newY);
                        System.out.println("OVNI movido a nueva posición: " + ovni.toJson());
                    }
                }
            }
            return false; // No eliminar OVNI
        });

        // Verificar colisiones
        checkCollisions();

        System.out.println("Después de mover:");
        ovnis.forEach(ovni -> {
            System.out.println(ovni.toJson());
        });

        System.out.println("Estado actual:");
        System.out.println("OVNIs en movimiento: " + getMovingCount());
        System.out.println("OVNIs estrellados: " + getCrashedCount());
        System.out.println("---- Fin de actualización de posiciones ----");
    }

    private void moveOvniTowards(OVNI ovni, int targetX, int targetY) {
        int deltaX = targetX - ovni.getX();
        int deltaY = targetY - ovni.getY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        if (distance <= ovni.getSpeed()) {
            ovni.setX(targetX);
            ovni.setY(targetY);
        } else {
            int moveX = (int) (ovni.getSpeed() * deltaX / distance);
            int moveY = (int) (ovni.getSpeed() * deltaY / distance);
            ovni.setX(ovni.getX() + moveX);
            ovni.setY(ovni.getY() + moveY);
        }
    }

    private boolean isInDestinationArea(OVNI ovni) {
        int deltaX = ovni.getX() - destinationX;
        int deltaY = ovni.getY() - destinationY;
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return distance <= destinationRadius;
    }

    private void checkCollisions() {
        for (OVNI ovni1 : ovnis) {
            for (OVNI ovni2 : ovnis) {
                if (ovni1 != ovni2 && !ovni1.isCrashed() && !ovni2.isCrashed()) {
                    int deltaX = ovni1.getX() - ovni2.getX();
                    int deltaY = ovni1.getY() - ovni2.getY();
                    double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

                    if (distance < 20) { // Distancia de colisión (ajustar si es necesario)
                        ovni1.setCrashed(true);
                        ovni2.setCrashed(true);
                        System.out.println(
                                "Colisión detectada entre: " + ovni1.getClientName() + " y " + ovni2.getClientName());
                    }
                }
            }
        }
    }

    public synchronized JsonArray getAllOvnisAsJson() {
        JsonArray ovnisJson = new JsonArray();
        for (OVNI ovni : ovnis) {
            ovnisJson.add(ovni.toJson());
        }
        return ovnisJson;
    }

    public void selectOvni(int ovniIndex, String clientName) {
        if (ovniIndex >= 0 && ovniIndex < ovnis.size()) {
            OVNI selectedOvni = ovnis.get(ovniIndex);

            if (clientName == null) {
                // Deseleccionar el OVNI
                selectedOvni.setClientName(null);
            } else {
                // Asegurarse de que ningún otro OVNI esté seleccionado por este cliente
                for (OVNI ovni : ovnis) {
                    if (clientName.equals(ovni.getClientName())) {
                        ovni.setClientName(null); // Deseleccionar cualquier OVNI previamente seleccionado
                    }
                }
                selectedOvni.setClientName(clientName);
            }
        }
    }

    public int getMovingCount() {
        int count = 0;
        for (OVNI ovni : ovnis) {
            if (!ovni.isCrashed()) {
                count++;
            }
        }
        return count;
    }

    public int getCrashedCount() {
        return crashedCount;
    }

    public static void incrementCrashedCount() {
        crashedCount++;
    }
}
