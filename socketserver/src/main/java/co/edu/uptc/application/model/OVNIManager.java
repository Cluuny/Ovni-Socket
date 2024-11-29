package co.edu.uptc.application.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.Getter;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
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
        ovnis.removeIf(ovni -> {
            if (!ovni.isCrashed()) {
                // Verificamos si el OVNI está en la zona de destino
                if (isInDestinationArea(ovni)) {
                    crashedCount++;
                    return true; // El OVNI ha llegado al destino y debe ser eliminado
                }

                if (ovni.hasCustomPath()) {
                    // Si tiene una trayectoria personalizada, moverlo de acuerdo a esa trayectoria
                    Point nextPoint = ovni.getCustomPath().get(0); // Tomar el primer punto de la trayectoria
                    moveOvniTowards(ovni, nextPoint.x, nextPoint.y); // Mover hacia el siguiente punto de la trayectoria

                    if (ovni.getX() == nextPoint.x && ovni.getY() == nextPoint.y) {
                        ovni.getCustomPath().remove(0); // Eliminar el punto de la trayectoria una vez alcanzado
                        System.out.println("OVNI avanzó en su ruta personalizada: " + ovni.toJson());
                    }
                } else if (ovni.hasDestination()) {
                    // Si tiene un destino fijo, moverlo hacia allí
                    moveOvniTowards(ovni, ovni.getDestinationX(), ovni.getDestinationY());
                    System.out.println("OVNI moviéndose hacia destino: " + ovni.toJson());
                } else {
                    // Si no tiene destino ni trayectoria personalizada, se mueve de acuerdo a su
                    // ángulo
                    int newX = ovni.getX() + (int) (ovni.getSpeed() * Math.cos(Math.toRadians(ovni.getAngle())));
                    int newY = ovni.getY() + (int) (ovni.getSpeed() * Math.sin(Math.toRadians(ovni.getAngle())));

                    // Verificar si el nuevo punto está fuera de los límites del área
                    if (newX < 0 || newX >= areaWidth || newY < 0 || newY >= areaHeight) {
                        crashedCount++;
                        return true; // El OVNI se ha salido del área y debe ser eliminado
                    } else {
                        ovni.setX(newX);
                        ovni.setY(newY);
                        System.out.println("OVNI movido a nueva posición: " + ovni.toJson());
                    }
                }
            }
            return false; // No eliminar el OVNI
        });

        // Verificar colisiones entre OVNIs
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

    public void selectOvni(int ovniId, String clientName) {
        for (OVNI ovni : ovnis) {
            if (ovni.getId() == ovniId) { // Usamos el ID único para seleccionar
                if (clientName == null) {
                    ovni.setClientName(null); // Deseleccionar
                } else {
                    ovni.setClientName(clientName); // Seleccionar
                }
                break;
            }
        }
    }

    public void setCustomPath(int ovniIndex, List<Point> customPath) {
        if (ovniIndex >= 0 && ovniIndex < ovnis.size()) {
            OVNI selectedOvni = ovnis.stream()
                    .filter(ovni -> ovni.getId() == ovniIndex) // Buscar por ID en lugar de índice
                    .findFirst()
                    .orElse(null);

            if (selectedOvni != null) {
                selectedOvni.setCustomPath(customPath); // Establecer la nueva trayectoria personalizada
                System.out.println("Trayectoria personalizada establecida para OVNI: " + selectedOvni.toJson());
            }
        }
    }

    public void selectOvniById(int ovniId, String clientName) {
        for (OVNI ovni : ovnis) {
            if (ovni.getId() == ovniId) {
                if (clientName == null) {
                    ovni.setClientName(null); // Deseleccionar
                } else {
                    ovni.setClientName(clientName); // Seleccionar
                }
                break;
            }
        }
    }

    public void setCustomPathById(int ovniId, List<Point> customPath) {
        for (OVNI ovni : ovnis) {
            if (ovni.getId() == ovniId) {
                ovni.setCustomPath(customPath);
                break;
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
