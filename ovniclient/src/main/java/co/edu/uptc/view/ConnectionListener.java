package co.edu.uptc.view;

public interface ConnectionListener {
  void onConnectRequest(String host, int port, String name);
}