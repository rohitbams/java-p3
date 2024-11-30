import java.io.IOException;

// Controller - Handles user actions and coordinates Model and View
public class ChatController {
    private final ChatModel model;
    private final ChatView view;

    public ChatController(ChatModel model, ChatView view) {
        this.model = model;
        this.view = view;

        // Register view as listener for model updates
        model.addListener(view);

        // Set up view callbacks
        view.setCommandHandler(this::handleCommand);
    }

    private void handleCommand(String command) {
        try {
            if (command.startsWith("/nick ")) {
                model.setNickname(command.substring(6));
            } else if (command.startsWith("/join ")) {
                model.joinChannel(command.substring(6));
            } else if (command.startsWith("/msg ")) {
                String[] parts = command.substring(5).split(" ", 2);
                if (parts.length == 2) {
                    model.sendMessage(parts[0], parts[1]);
                }
            }
        } catch (Exception e) {
            view.showError("Error processing command: " + e.getMessage());
        }
    }

    public void connect(String host, int port) {
        try {
            model.connect(host, port);
            view.showMessage("Connected to " + host + ":" + port);
        } catch (IOException e) {
            view.showError("Connection failed: " + e.getMessage());
        }
    }
}

