package co.edu.uptc.application.model.client;

import java.util.concurrent.CopyOnWriteArrayList;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientManager {
  private CopyOnWriteArrayList<Client> clients;

  public ClientManager() {
    this.clients = new CopyOnWriteArrayList<>();
  }

  public boolean addClient(Client client) {
    return clients.add(client);
  }

  public boolean removeClient(String name) {
    this.clients.forEach(client -> {
      if (client.getName().equals(name)) {
        this.clients.remove(client);
      }
    });
    return true;
  }
}
