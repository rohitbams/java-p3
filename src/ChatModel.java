import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatModel  { // Model - Handles data and server communication

    private Socket socket;
    private BufferedReader serverReader;
    private PrintWriter serverWriter;

    private List<ChatModelListener> listeners = new ArrayList<>();
    private String currentNickname;
    private Set<String> currentChannels = new HashSet<>();

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        serverWriter = new PrintWriter(socket.getOutputStream(), true);

        // Start listening for server messages
        new Thread(this::listenForServerMessages).start();
    }

    private void listenForServerMessages() {
        try {
            String line;
            while ((line = serverReader.readLine()) != null) {
                final String message = line;
                // Notify all listeners of new message
                listeners.forEach(listener -> listener.onMessageReceived(message));
            }
        } catch (IOException e) {
            notifyError("Lost connection to server: " + e.getMessage());
        }
    }

    public void sendCommand(String command) {
        if (serverWriter != null) {
            serverWriter.println(command);
        }
    }

    public void setNickname(String nickname) {
        sendCommand("NICK " + nickname);
        this.currentNickname = nickname;
    }

    public void joinChannel(String channel) {
        sendCommand("JOIN " + channel);
        currentChannels.add(channel);
    }

    public void sendMessage(String target, String message) {
        sendCommand("PRIVMSG " + target + " :" + message);
    }

    public void addListener(ChatModelListener listener) {
        listeners.add(listener);
    }

    private void notifyError(String error) {
        listeners.forEach(listener -> listener.onError(error));
    }
}
