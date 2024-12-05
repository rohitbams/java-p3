import java.io.IOException;

/**
 * The ChatController class.
 * This is the Controller component in the MVC design pattern.
 * It handles user actions and coordinates Model and View.
 */
public class ChatController implements ChatModel.ChatListener {

    private final ChatModel model;
    private final ChatView view;

    /**
     * @param model
     * @param view
     */
    public ChatController(ChatModel model, ChatView view) {
        this.model = model;
        this.view = view;
        model.addListener(this);
        view.setController(this);
    }

    /**
     * Connect
     * @param host
     * @param port
     */
    public void connect(String host, int port) throws IOException {
        model.connect(host, port);
    }



    /**
     * Handle command from the user.
     *
     * @param command
     */
    @Override
    public void handleCommand(String command) {
        String[] commandParts = command.split(" ", 2);
        String action = commandParts[0].toUpperCase();
        String args = commandParts.length > 1 ? commandParts[1] : "";

        try {
            if (!model.isConnected()) {

            }

            switch (action) {
                case "NICK":
                    if (model.isRegistered()) {
                        model.sendMessage("NICK " + args);
                    } else {
                        model.setCurrentNickname(args);
                        model.sendMessage("NICK " + args);
                    }
                    break;
                case "PRIVMSG":
                    handlePrivateMessage(args);
                    break;
                case "JOIN":
                    if (!args.startsWith("#")) {
                        view.showError("The JOIN command must include a '#' before the channel name.");
                        return;
                    }
                    model.setCurrentChannel(args);
                    model.sendMessage("JOIN " + args);
                    model.sendMessage("NAMES " + args);
                    break;
                case "PART":
                    model.sendMessage("PART " + args);
                    break;
                case "LIST":
                    model.sendMessage("LIST");
                    break;
                case "QUIT":
                    model.sendMessage("QUIT");
                    break;
                default:
                    view.showError("Invalid command: " + command + ". Use the following commands: ");
                    break;
            }
        } catch (Exception e) {
            view.showError("Error handling command: " + e.getMessage());
        }
    }

    /**
     * Handle private messages to channels or users.
     * Messages must follow the format: target :message
     * where target is either a channel (starts with #) or username
     */
    private void handlePrivateMessage(String args) {

        System.out.println("Handling private message: " + args);
        System.out.println("Current channel: " + model.getCurrentChannel());

        // regular chat messages
        if (!args.contains(":")) {

            String currentChannel = model.getCurrentChannel();
            if (currentChannel != null) {
                model.sendMessage("PRIVMSG " + currentChannel + " :" + args);
            } else {
                view.showError("Please join a channel first");
            }
            return;
        }

        // For explicit PRIVMSG commands, parse target and message
        String[] parts = args.split(":", 2);
        String target = parts[0].trim();
        String message = parts[1].trim();

        // Validate the message format
        if (target.isEmpty()) {
            view.showError("Message target cannot be empty");
            return;
        }

        // Send to channel or user
        if (target.startsWith("#") || !target.contains(" ")) {
            model.sendMessage("PRIVMSG " + target + " :" + message);
        } else {
            view.showError("Invalid message target. Use a nickname or channel starting with '#'");
        }
    }
    private void processServerResponse(String message) {
        if (message.startsWith("REPLY_LIST")) {
            // Parse channel list from server response
            // Format: REPLY_LIST #channel1 #channel2 #channel3
            String[] parts = message.split(" ");
            String[] channels = new String[parts.length - 1];
            System.arraycopy(parts, 1, channels, 0, parts.length - 1);
            view.updateChannelList(channels);
        }
    }

    public void refreshUserList() {
        String currentChannel = model.getCurrentChannel();
        if (currentChannel != null) {
            model.sendMessage("NAMES " + currentChannel);
        }
    }

    @Override
    public void onConnectionStatusChanged(boolean connected, String message) {
        view.onConnectionStatusChanged(connected, message);
    }

    @Override
    public void onMessageReceived(String message) {
        view.onMessageReceived(message);
    }

    @Override
    public void onChannelJoined(String channel) {
        view.onChannelJoined(channel);
    }

    @Override
    public void onChannelLeft(String channel) {
        view.onChannelLeft(channel);
    }

    @Override
    public void onChannelListReceived(String[] channels) {
        view.updateChannelList(channels);
    }

    @Override
    public void onUserListReceived(String[] users) {
        view.updateUserList(users);
    }
}


