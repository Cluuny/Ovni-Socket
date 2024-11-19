package co.edu.uptc.model;

import java.io.*;
import java.net.*;

public class ConnectionHandler {
    private Socket clientSocket;
    private DataInputStream reader;
    private DataOutputStream writer;

    public void connect(String host, int port) throws IOException {
        clientSocket = new Socket(host, port);
        reader = new DataInputStream(clientSocket.getInputStream());
        writer = new DataOutputStream(clientSocket.getOutputStream());
    }

    public void sendMessage(String message) throws IOException {
        writer.writeUTF(message);
    }

    public String receiveMessage() throws IOException {
        return reader.readUTF();
    }

    public void disconnect() throws IOException {
        if (clientSocket != null) {
            reader.close();
            writer.close();
            clientSocket.close();
        }
    }
}
