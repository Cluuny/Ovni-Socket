package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;

public class ConnectionView extends JFrame {
    private final JTextField hostField;
    private final JTextField portField;
    private final JTextField nameField;
    private final JButton connectButton;

    public ConnectionView(ConnectionListener listener) {

        setTitle("Conexión Simulación OVNI");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());
        setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titleLabel = new JLabel("Conexión Simulación OVNI", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Host
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        add(new JLabel("Host:"), gbc);

        gbc.gridx = 1;
        hostField = new JTextField("localhost", 15);
        add(hostField, gbc);

        // Puerto
        gbc.gridy = 2;
        gbc.gridx = 0;
        add(new JLabel("Puerto:"), gbc);

        gbc.gridx = 1;
        portField = new JTextField("7000", 15);
        add(portField, gbc);

        // Nombre
        gbc.gridy = 3;
        gbc.gridx = 0;
        add(new JLabel("Nombre:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField("Cliente", 15);
        add(nameField, gbc);

        // Botón Conectar
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> {
            try {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                String name = nameField.getText().trim(); // Captura el nombre
                listener.onConnectRequest(host, port, name); // Llama al método incluyendo el nombre
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Puerto inválido",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
        add(connectButton, gbc);
    }

    public interface ConnectionListener {
        void onConnectRequest(String host, int port, String name);
    }
}