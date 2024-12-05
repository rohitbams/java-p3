import javax.swing.*;

public class ChatClient {
    public static void main(String[] args) {
        // SwingUtilities to ensure GUI is created on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                ChatModel model = new ChatModel();
                ChatView view = new ChatView();
                ChatController controller = new ChatController(model, view);
                view.initialise();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Error starting chat client: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}