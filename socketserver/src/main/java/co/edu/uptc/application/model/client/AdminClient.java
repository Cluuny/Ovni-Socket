package co.edu.uptc.application.model.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminClient extends Client {
  public AdminClient(String name) {
    super(name);
  }
}
