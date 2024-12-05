import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Main chat window that displays messages and channels.
 * Implements ChatModel.ChatListener to receive updates from the model.
 */
public class ChatView extends JFrame implements ChatModel.ChatListener {

    // GUI Components
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private JList<String> channelList;
    private JList<String> userList;
    private JLabel statusLabel;
    private JButton joinButton;
    private JButton leaveButton;
    private static final int MIN_WIDTH = 600;
    private static final int MIN_HEIGHT = 400;
    private ChatController controller;

    public ChatView() {
        super("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        createComponents();
        createMenuBar();
        setSize(800, 600);
        setVisible(true);
    }

    public void initialise() {
        SwingUtilities.invokeLater(() -> {
            showConnectionDialog();
            setVisible(true);
        });
    }

    private void createComponents() {
        setLayout(new BorderLayout(5, 5));

        // Status bar at top
        statusLabel = new JLabel("Not Connected");
        add(statusLabel, BorderLayout.NORTH);

        // Message area in center
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.CENTER);

        // Input panel at bottom
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> handleSendMessage());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Lists panel on right
        JPanel listsPanel = new JPanel(new GridLayout(2, 1));

        // Channel list
        JPanel channelPanel = new JPanel(new BorderLayout());
        channelList = new JList<>();
        channelPanel.add(new JLabel("Channels"), BorderLayout.NORTH);
        channelPanel.add(new JScrollPane(channelList), BorderLayout.CENTER);

        // Channel buttons
        JPanel channelButtons = new JPanel(new FlowLayout());
        joinButton = new JButton("Join");
        leaveButton = new JButton("Leave");
        joinButton.addActionListener(e -> handleJoinChannel());
        leaveButton.addActionListener(e -> handleLeaveChannel());
        channelButtons.add(joinButton);
        channelButtons.add(leaveButton);
        channelPanel.add(channelButtons, BorderLayout.SOUTH);

        // User list
        JPanel userPanel = new JPanel(new BorderLayout());
        userList = new JList<>();
        userPanel.add(new JLabel("Users"), BorderLayout.NORTH);
        userPanel.add(new JScrollPane(userList), BorderLayout.CENTER);

        listsPanel.add(channelPanel);
        listsPanel.add(userPanel);
        add(listsPanel, BorderLayout.EAST);
    }


    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu connectionMenu = new JMenu("Connection");
        JMenuItem connectItem = new JMenuItem("Connect...");
        connectItem.addActionListener(e -> showConnectionDialog());
        connectionMenu.add(connectItem);
        menuBar.add(connectionMenu);
        setJMenuBar(menuBar);
    }

    //
    private void handleSendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            controller.handleCommand("PRIVMSG " + message);
            inputField.setText("");
        }
    }

    private void handleJoinChannel() {
        String channel = channelList.getSelectedValue();
        if (channel != null && controller != null) {
            controller.handleCommand("JOIN " + channel);
        }
    }

    private void handleLeaveChannel() {
        String channel = channelList.getSelectedValue();
        if (channel != null && controller != null) {
            controller.handleCommand("PART " + channel);
        }
    }

    private void showConnectionDialog() {
        ConnectionDialog dialog = new ConnectionDialog(this);
        dialog.setVisible(true);
        if (dialog.wasOkPressed() && controller != null) {
            try {
                controller.connect(dialog.getServer(), dialog.getPort());
                dialog.dispose();

            } catch (IOException e) {
                showError("Failed to connect: " + e.getMessage());
            }
        }
    }

    public void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(this,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        });
    }

    // Methods for Controller to update the view
    public void setController(ChatController controller) {
        this.controller = controller;
    }

    public void updateChannelList(String[] channels) {
        channelList.setListData(channels);
    }

    public void updateUserList(String[] users) {
        userList.setListData(users);
    }

    // Listener implementation
    @Override
    public void onConnectionStatusChanged(boolean connected, String message) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            inputField.setEnabled(connected);
            sendButton.setEnabled(connected);
        });
    }

    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() -> {
            messageArea.append(message + "\n");
            messageArea.setCaretPosition(messageArea.getDocument().getLength());
        });
    }

    @Override
    public void handleCommand(String command) {
    }

    @Override
    public void onChannelJoined(String channel) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Joined channel: " + channel);
            joinButton.setEnabled(false);
            leaveButton.setEnabled(true);
        });

    }

    @Override
    public void onChannelLeft(String channel) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Left channel: " + channel);
            joinButton.setEnabled(true);
            leaveButton.setEnabled(false);
        });
    }

    @Override
    public void onChannelListReceived(String[] channels) {
        SwingUtilities.invokeLater(() -> {
            channelList.setListData(channels);
        });
    }

    @Override
    public void onUserListReceived(String[] users) {
        SwingUtilities.invokeLater(() -> {
            userList.setListData(users);
        });
    }

}