package messageclient.api;

public interface MessageObserver {
    void receivedMessage(String message);
    void connectionStarted(Client client);
    void connectionClosed();
}
