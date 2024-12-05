package ChatServer;

import java.io.*;
import java.net.Socket;

public class extraClient {

    private Socket socket;
    private BufferedReader serverReader;
    private BufferedWriter serverWriter;
    private boolean isRunning = true;

    public void connect(String host, int port) {
        try {
            // Establish connection
            socket = new Socket(host, port);
            System.out.println("Connected to server at " + host + ":" + port);

            // Set up input/output streams
            serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            // Start server listener thread
            startServerListener();

            // Start handling user input
            handleUserInput();

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    private void handleUserInput() {
        try {
            BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
            String message;

            while (isRunning && (message = userInput.readLine()) != null) {
                sendToServer(message);
                if (message.equals("QUIT")) {
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading user input: " + e.getMessage());
        }
    }

    private void startServerListener() {
        new Thread(() -> {
            try {
                String serverMessage;
                while (isRunning && (serverMessage = serverReader.readLine()) != null) {
                    System.out.println("Server: " + serverMessage);
                }
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error reading from server: " + e.getMessage());
                }
            }
        }).start();
    }

    private void sendToServer(String message) {
        try {
            serverWriter.write(message);
            serverWriter.newLine();  // Important: Add newline character
            serverWriter.flush();    // Important: Flush the buffer
        } catch (IOException e) {
            System.err.println("Error sending message to server: " + e.getMessage());
        }
    }

    private void disconnect() {
        isRunning = false;
        try {
            if (serverWriter != null) serverWriter.close();
            if (serverReader != null) serverReader.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        extraClient client = new extraClient();
        client.connect("localhost", 12345);
    }
}

