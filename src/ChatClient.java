import javax.swing.*;

public class ChatClient {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChatModel model = new ChatModel();
            ChatView view = new ChatView();
            ChatController controller = new ChatController(model, view);

            // Show connection dialog
            String host = JOptionPane.showInputDialog("Enter server host:", "localhost");
            String portStr = JOptionPane.showInputDialog("Enter server port:", "12345");
            if (host != null && portStr != null) {
                try {
                    int port = Integer.parseInt(portStr);
                    controller.connect(host, port);
                    view.setVisible(true);
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(null, "Invalid port number", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
