import java.io.*;
import java.net.*;
import java.util.*;

public class TestServer {
    // Store connected clients and their output streams
    private static Map<Socket, PrintWriter> clients = new HashMap<>();
    // Store channels and their members
    private static Map<String, Set<Socket>> channels = new HashMap<>();

    public static void main(String[] args) {
        // Default test port - same as your default in ConnectionDialog
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Test server running on port " + port);

            // Main server loop - accept connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected from " + clientSocket.getInetAddress());

                // Create output writer for this client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clients.put(clientSocket, out);

                // Handle each client in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream())
            );
            PrintWriter out = clients.get(socket);
            String nickname = null;

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received: " + inputLine);
                String[] parts = inputLine.split(" ", 3);
                String command = parts[0].toUpperCase();

                // Handle different IRC commands
                switch (command) {
                    case "NICK":
                        if (parts.length > 1) {
                            nickname = parts[1];
                            out.println("REPLY_NICK");
                            System.out.println("Client set nickname to: " + nickname);
                        }
                        break;

                    case "JOIN":
                        if (parts.length > 1) {
                            String channel = parts[1];
                            // Create channel if it doesn't exist
                            channels.putIfAbsent(channel, new HashSet<>());
                            channels.get(channel).add(socket);
                            out.println("REPLY_JOIN " + channel);
                            // Send updated user list to all channel members
                            broadcastToChannel(channel, "NAMES " + channel + " " + nickname);
                            System.out.println("Client joined channel: " + channel);
                        }
                        break;

                    case "PART":
                        if (parts.length > 1) {
                            String channel = parts[1];
                            if (channels.containsKey(channel)) {
                                channels.get(channel).remove(socket);
                                out.println("REPLY_PART " + channel);
                                System.out.println("Client left channel: " + channel);
                            }
                        }
                        break;

                    case "LIST":
                        // Send list of channels
                        String channelList = String.join(" ", channels.keySet());
                        out.println("REPLY_LIST " + channelList);
                        break;

                    case "PRIVMSG":
                        if (parts.length > 2) {
                            String target = parts[1];
                            String message = parts[2];
                            if (target.startsWith("#")) {
                                // Channel message
                                broadcastToChannel(target, "PRIVMSG " + target + " " + nickname + ": " + message);
                            }
                        }
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Client handler error: " + e.getMessage());
        } finally {
            // Cleanup when client disconnects
            try {
                clients.remove(socket);
                for (Set<Socket> members : channels.values()) {
                    members.remove(socket);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static void broadcastToChannel(String channel, String message) {
        if (channels.containsKey(channel)) {
            for (Socket member : channels.get(channel)) {
                PrintWriter out = clients.get(member);
                if (out != null) {
                    out.println(message);
                }
            }
        }
    }
}