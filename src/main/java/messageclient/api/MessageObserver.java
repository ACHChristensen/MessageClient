package messageclient.api;

public interface MessageObserver {
    void receivedMessage(String message);
}
