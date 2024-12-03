package co.edu.uptc.application.model.ovni;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SimulationParameters {
    private int numberOfOvnis;
    private int appearanceInterval;
    private int defaultSpeed;
}
