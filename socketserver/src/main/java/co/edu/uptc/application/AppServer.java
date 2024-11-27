package co.edu.uptc.application;

import co.edu.uptc.application.model.SimulationEngine;
import co.edu.uptc.controllers.ConnectionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AppServer implements Runnable {
    private final ServerSocket serverSocket;
    private final SimulationEngine simulationEngine;

    public AppServer() throws IOException {
        this.serverSocket = new ServerSocket(7000);
        this.simulationEngine = new SimulationEngine(800, 600, 400, 300, 50);
        System.out.println("Servidor abierto en el puerto: 7000");
        simulationEngine.startSimulation(10, 500, 5);
    }

    @Override
    public void run() {
        try {
            while (!this.serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                Thread clientThread = new Thread(new ConnectionHandler(socket, simulationEngine));
                clientThread.start();
            }
        } catch (Exception e) {
            this.shutDown();
        }
    }

    public void shutDown() {
        try {
            simulationEngine.stopSimulation(); // Detener simulación al cerrar el servidor.
            serverSocket.close();
        } catch (IOException ignore) {
        }
    }

    public static void main(String[] args) {
        try {
            Thread serverThread = new Thread(new AppServer());
            serverThread.start();
        } catch (IOException ignored) {
        }
    }
}