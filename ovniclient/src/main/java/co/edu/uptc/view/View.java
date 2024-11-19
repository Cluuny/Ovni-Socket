package co.edu.uptc.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class View extends JFrame {
    private final JTextArea messageArea = new JTextArea(20, 40);
    private final JButton connectButton = new JButton("Connect");
    private final JTextField hostField = new JTextField("localhost");
    private final JTextField portField = new JTextField("7000");

    public View() {
        setTitle("Client");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Host:"));
        topPanel.add(hostField);
        topPanel.add(new JLabel("Port:"));
        topPanel.add(portField);
        topPanel.add(connectButton);

        add(topPanel, BorderLayout.NORTH);
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        setSize(500, 400);
    }

    public JButton getConnectButton() {
        return connectButton;
    }

    public String getHost() {
        return hostField.getText();
    }

    public int getPort() {
        return Integer.parseInt(portField.getText());
    }

    public void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message + "\n"));
    }
}
