import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

// View - Handles GUI components
public class ChatView extends JFrame implements ChatModelListener {
    private JTextArea chatArea;
    private JTextField inputField;
    private JList<String> channelList;
    private Consumer<String> commandHandler;

    public ChatView() {
        setTitle("Chat Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        initializeComponents();
        pack();
    }

    private void initializeComponents() {
        setLayout(new BorderLayout());

        // Chat area
        chatArea = new JTextArea(20, 40);
        chatArea.setEditable(false);
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        // Input field
        inputField = new JTextField();
        inputField.addActionListener(e -> {
            String text = inputField.getText();
            if (!text.isEmpty() && commandHandler != null) {
                commandHandler.accept(text);
                inputField.setText("");
            }
        });
        add(inputField, BorderLayout.SOUTH);

        // Channel list
        channelList = new JList<>(new DefaultListModel<>());
        add(new JScrollPane(channelList), BorderLayout.EAST);
    }

    public void setCommandHandler(Consumer<String> handler) {
        this.commandHandler = handler;
    }

    @Override
    public void onMessageReceived(String message) {
        SwingUtilities.invokeLater(() ->
                chatArea.append(message + "\n")
        );
    }

    @Override
    public void onError(String error) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    public void showMessage(String message) {
        chatArea.append(message + "\n");
    }

    public void showError(String error) {
        JOptionPane.showMessageDialog(this, error, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
