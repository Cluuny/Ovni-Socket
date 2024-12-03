package co.edu.uptc.application.model.client;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegularClient extends Client {
  public RegularClient(String name) {
    super(name);
  }
}
