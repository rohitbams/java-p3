import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static javax.management.remote.JMXConnectorFactory.connect;

/**
 * The ConnectionDialog class.
 * This class implements a dialog window for getting
 * the server address, port number,from the user.
 * It creates a modal dialog box that blocks main
 * window until completed.
 */
public class ConnectionDialog extends JDialog {
    private JTextField serverInputField;
    private JTextField portInputField;
    private String server;
    private int port;
    private boolean wasOkPressed;

    /**
     *
     * @param parent
     */
    public ConnectionDialog(JFrame parent) {
        super(parent, "Connect to Server", true);

        // main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // input fields panel
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        serverInputField = new JTextField("localhost", 20);
        portInputField = new JTextField("12345", 20);

        inputPanel.add(new JLabel("Server:"));
        inputPanel.add(serverInputField);
        inputPanel.add(new JLabel("Port:"));
        inputPanel.add(portInputField);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton connectButton = new JButton("Connect");
        JButton cancelButton = new JButton("Cancel");
        serverInputField.addActionListener(e -> handleconnectButton());
        portInputField.addActionListener(e -> handleconnectButton());
        connectButton.addActionListener(e -> handleCancelButton());
        cancelButton.addActionListener(e -> handleCancelButton());
        buttonPanel.add(connectButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(mainPanel);

        pack();
        setLocationRelativeTo(parent);
        setMinimumSize(new Dimension(300, 150));
    }

    private void handleconnectButton() {
        if (validateInput()) {
            wasOkPressed = true;
            setVisible(false);
        }
    }

    private void handleCancelButton() {
        wasOkPressed = false;
        setVisible(false);
    }

    private boolean validateInput() {
        try {
            server = serverInputField.getText().trim();
            String portString = portInputField.getText().trim();

            if (server.isEmpty() || portString.isEmpty()) {
                showError("Please enter both the fields.");
                return false;
            }

            port = Integer.parseInt(portString);
            if (port < 1 || port > 65535) {
                showError("Port must be between 1 and 65535");
                return false;
            }
            return true;

        } catch (NumberFormatException e) {
            showError("Port must be a number");
            return false;
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    public String getServer() { return server; }
    public int getPort() { return port; }
    public boolean wasOkPressed() { return wasOkPressed; }
}