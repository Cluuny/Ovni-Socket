package co.edu.uptc.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

import java.io.*;
import java.net.*;

@Getter
public class ConnectionHandler implements Cloneable {
    private Socket clientSocket;
    private DataInputStream reader;
    private DataOutputStream writer;
    private String clientName;

    public void connect(String host, int port) throws IOException {
        clientSocket = new Socket(host, port);
        reader = new DataInputStream(clientSocket.getInputStream());
        writer = new DataOutputStream(clientSocket.getOutputStream());
    }

    public void sendName(String name) throws IOException {
        this.clientName = name;
        JsonObject request = new JsonObject();
        request.addProperty("action", "registerRegularClient");
        request.addProperty("name", name);
        sendMessage(request.toString());
    }

    public void sendMessage(String message) throws IOException {
        writer.writeUTF(message);
        ;
    }

    public String receiveMessage() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream(), "UTF-8"));
        String response = bufferedReader.readLine();
        return response;
    }

    public void disconnect() throws IOException {
        if (clientSocket != null) {
            reader.close();
            writer.close();
            clientSocket.close();
        }
    }

    public void requestSimulationDimensions() throws IOException {
        JsonObject request = new JsonObject();
        request.addProperty("action", "getDimensions");
        sendMessage(request.toString());
    }

    public int[] parseSimulationDimensions(String jsonResponse) {
        JsonObject dimensionsJson = JsonParser.parseString(jsonResponse).getAsJsonObject();
        return new int[] {
                dimensionsJson.get("width").getAsInt(),
                dimensionsJson.get("height").getAsInt()
        };
    }

    public boolean isSocketConnected() {
        return clientSocket != null && !clientSocket.isClosed();
    }

    @Override
    public ConnectionHandler clone() throws CloneNotSupportedException {
        ConnectionHandler cloned = (ConnectionHandler) super.clone();
        return cloned;
    }
}
