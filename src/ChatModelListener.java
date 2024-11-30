// Interface for Model listeners
public interface ChatModelListener {
    void onMessageReceived(String message);
    void onError(String error);
}
