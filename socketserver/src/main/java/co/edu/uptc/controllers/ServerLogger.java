package co.edu.uptc.controllers;

public class ServerLogger {
  public static void log(String message, String type) {
    switch (type) {
      case "error":
        System.out.println("[ERROR]: " + message);
        break;
      default:
        System.out.println("[INFO]: " + message);
        break;
    }
  }
}
