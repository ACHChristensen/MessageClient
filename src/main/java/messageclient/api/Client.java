package messageclient.api;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The Message Client
 */
public class Client implements AutoCloseable, Runnable {
    private final int id;
    private final SocketChannel socket;
    private final Set<MessageObserver> observers = new HashSet<>();
    private final InetSocketAddress address;
    private final Thread thread;

    private Client(int id, InetSocketAddress address, SocketChannel socket) {
        this.id = id;
        this.address = address;
        this.socket = socket;
        this.thread = new Thread(this);
    }

    public static Client open(int id, InetSocketAddress address) throws IOException {
        return new Client(id, address, SocketChannel.open());
    }

    public void connect() throws IOException {
        this.socket.connect(this.address);
        observers.forEach(ob -> ob.connectionStarted(this));
        this.thread.start();
    }

    public void sendMessage(String message) throws IOException {
        CharBuffer buffer = CharBuffer.wrap(message.toCharArray());
        int x = socket.write(StandardCharsets.UTF_8.encode(buffer));
    }

    private String readMessage() throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(4096);
        int x = socket.read(buffer);
        buffer.flip();
        String msg = StandardCharsets.UTF_8.decode(buffer).toString();
        return msg;
    }

    @Override
    public void run() {
        try {
            while (true) {
                String message = readMessage();
                observers.forEach(ob -> ob.receivedMessage(message));
            }
        } catch (IOException e) { }
    }

    @Override
    public void close() throws IOException {
        socket.close();
        thread.interrupt();
        observers.forEach(MessageObserver::connectionClosed);
    }

    public synchronized void register(MessageObserver observer) {
        observers.add(observer);
    }

    public synchronized void deregister(MessageObserver observer) {
        observers.remove(observer);
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public boolean isOpen() {
        return socket.isOpen();
    }

    public Client reconnect() throws IOException {
        Client client = Client.open(this.id, this.address);
        synchronized (this) {
        client.observers.addAll(this.observers);
        }
        this.close();
        client.connect();
        return client;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", address=" + address +
                ", open=" + isOpen() +
                '}';
    }
}