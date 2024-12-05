import javax.swing.*;

public class ChatClient {
    public static void main(String[] args) {
        // Use SwingUtilities to ensure GUI is created on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create MVC components
                ChatModel model = new ChatModel();
                ChatView view = new ChatView();
                ChatController controller = new ChatController(model, view);

                // Show the view
                view.initialise();
            } catch (Exception e) {
                System.err.println("Error starting chat client: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                        "Error starting chat client: " + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}