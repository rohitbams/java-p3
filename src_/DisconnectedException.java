/**
 * DisconnectedException class.
 * Custom exception for handling client/server disconnections cleanly.
 * This helps distinguish disconnection events from other types of IO errors.
 */
public class DisconnectedException extends Exception {
    public DisconnectedException(String message) {
        super(message);
    }
}
