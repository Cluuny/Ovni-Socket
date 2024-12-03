package co.edu.uptc.application;

import co.edu.uptc.application.model.ovni.SimulationEngine;
import co.edu.uptc.controllers.ConnectionHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class AppServer implements Runnable {
    private final ServerSocket serverSocket;
    private final SimulationEngine simulationEngine;

    public AppServer(String port) throws IOException {
        this.serverSocket = new ServerSocket(Integer.parseInt(port));
        this.simulationEngine = new SimulationEngine(800, 600, 400, 300, 50);
        simulationEngine.startSimulation(20, 500, 5);
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
            simulationEngine.stopSimulation();
            serverSocket.close();
        } catch (IOException ignore) {
        }
    }

    public static void main(String[] args) {
        try {
            Thread serverThread = new Thread(new AppServer(args[0]));
            serverThread.start();
        } catch (IOException ignored) {
        }
    }
}