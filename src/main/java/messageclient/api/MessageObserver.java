package messageclient.api;

import java.net.InetSocketAddress;

public interface MessageObserver {
    void receivedMessage(String message);
    void connectionStarted(InetSocketAddress address);
    void connectionClosed();
}
