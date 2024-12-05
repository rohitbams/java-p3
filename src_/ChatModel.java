import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * The ChatModel class.
 * This is the Model class in the MVC design patter.
 * This class implements the networking, I/O, threading, and
 * user details of the chat client.
 */
public class ChatModel {

    private Socket socket;
    private BufferedReader br;
    private OutputStream os;
    private InputStream is;
    private final List<ChatListener> listeners;
    private String currentChannel;
    private String currentNickname;
    private boolean isRegistered = false;
    private String currentMessage;
    private boolean isConnected;

    /**
     * Chat model.
     */
    public ChatModel() {
        this.listeners = new ArrayList<>();
    }

    public void addListener(ChatListener listener) {
        listeners.add(listener);
    }

    /**
     * The connect method.
     * This method implements the networking part of the chat client.
     * It takes the server address and port number inputs and connects
     * to the host server, then updates the listener class about the
     * connection status. It calls the startMessageListener() method.
     *
     * @param host server address
     * @param port port
     */
    public void connect(String host, int port) {
        try {
            this.socket = new Socket(host, port);
            is = socket.getInputStream();
            os = socket.getOutputStream();
            br = new BufferedReader(new InputStreamReader(is));
            notifyConnectionStatus(true, "Connected to " + host + ":" + port);
            startMessageListener();
        } catch (Exception e) {
            notifyConnectionStatus(false, "Failed to connect: " + e.getMessage());
            cleanup();
        }
    }

    // Creates a new thread to listen to incoming messages
    // from the server and update the listener
    private void startMessageListener() {
        new Thread(() -> {
            try {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.equals("exit")) {
                        throw new DisconnectedException("Server closed connection");
                    }

                    if (line.startsWith("REPLY_LIST")) {
                        line = line.replace(":", "").trim();
                        String[] parts = line.split("\\s+");
                        String[] channels = new String[parts.length - 1];
                        System.arraycopy(parts, 1, channels, 0, channels.length);
                        for (ChatListener listener : listeners) {
                            listener.onChannelListReceived(channels);
                        }
                    } else if (line.startsWith("REPLY_NAMES")) {
                        line = line.replace(":", "").trim();
                        String[] parts = line.split("\\s+");
                        if (parts.length > 2) {
                            String[] users = new String[parts.length - 2];
                            System.arraycopy(parts, 2, users, 0, users.length);
                            for (ChatListener listener : listeners) {
                                listener.onUserListReceived(users);
                            }

                        }
                    }
                    notifyMessageReceived(line);
                }
            } catch (Exception e) {
                cleanup();
            }
        }).start();
    }

    /**
     * Method for sending protocol commands to the server.
     * It converts strings to bytes to transfer over the network.
     *
     * @param message client output
     */
    public void sendMessage(String message) {
        try {
            os.write((message + "\n").getBytes());
            os.flush();
        } catch (IOException e) {
            cleanup();
        }
    }

    // Method for cleaning up the I/O connections and close resources
    private void cleanup() {
        try {
            if (br != null) br.close();
            if (os != null) os.close();
            if (is != null) is.close();
            if (socket != null) socket.close();
            notifyConnectionStatus(false, "Disconnected from server");
        } catch (IOException e) {
            System.out.println("Error during cleanup: " + e.getMessage());
        }
    }

    public void setCurrentChannel(String channel) {
        this.currentChannel = channel;
    }

    public String getCurrentChannel() {
        return currentChannel;
    }

    // update connection status
    private void notifyConnectionStatus(boolean connected, String message) {
        for (ChatListener listener : listeners) {
            listener.onConnectionStatusChanged(connected, message);
        }
    }

    // update message received.
    private void notifyMessageReceived(String message) {
        for (ChatListener listener : listeners) {
            listener.onMessageReceived(message);
        }
    }

    // validate nickname
    public boolean hasNickname() {
        return currentNickname != null && !currentNickname.isEmpty();
    }

    public void setCurrentNickname(String currentNickname) {
        this.currentNickname = currentNickname;
        this.isRegistered = true;
    }

    public String getCurrentNickname() {
        return currentNickname;
    }

    public boolean isRegistered() {
        return isRegistered;
    }

    public boolean isInChannel(String channel) {
        return currentChannel != null && !currentChannel.isEmpty();
    }

    public void setCurrentMessage(String currentMessage) {
        this.currentMessage = currentMessage;
    }

    public String getCurrentMessage() {
        return currentMessage;
    }


    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }


    /**
     * Interface class for notifying view of changes.
     */
    public interface ChatListener {
        void onConnectionStatusChanged(boolean connected, String message);

        void onMessageReceived(String message);

        void handleCommand(String command);

        void onChannelJoined(String channel);

        void onChannelLeft(String channel);

        void onChannelListReceived(String[] channels);

        void onUserListReceived(String[] users);


    }
}

