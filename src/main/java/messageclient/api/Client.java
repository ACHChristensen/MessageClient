package messageclient.api;

import messageclient.Utils;
import messageclient.ui.ServerPrompt;

import javax.swing.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The Message Client
 */
public class Client implements AutoCloseable {
    private final SocketChannel socket;
    private final Set<MessageObserver> observers = new HashSet<>();

    private Client(SocketChannel socket) {
        this.socket = socket;
    }

    public static Client open(SocketAddress address) throws IOException {
        return new Client(SocketChannel.open(address));
    }

    public void sendMessage(String message) throws IOException {
        CharBuffer buffer = CharBuffer.wrap(message.toCharArray());
        socket.write(StandardCharsets.UTF_8.encode(buffer));
    }

    private String readMessage() throws IOException{
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        socket.read(buffer);
        buffer.flip();
        return StandardCharsets.UTF_8.decode(buffer).toString();
    }

    public void run() {
        // JOptionPane.showMessageDialog(null, "Connection To Server Lost");
        try {
            while (true) {
                String message = readMessage();
                for (MessageObserver o : observers) {
                    o.receivedMessage(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static InetSocketAddress interactivelyGetAddress() throws InterruptedException {
        return new ServerPrompt("localhost", 2222)
                .waitForAddress();
    }

    public static void main(String[] args) throws InterruptedException {
        InetSocketAddress address = args.length == 2
                ? Utils.parseInetAddress(args[0], args[1])
                : interactivelyGetAddress();

        try (Client client = Client.open(address)) {
            System.out.println(client.socket);
            client.sendMessage("hello");
            client.register(System.out::println);
            client.run();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    public void register(MessageObserver observer) {
        observers.add(observer);
    }

    public void deregister(MessageObserver observer) {
        observers.remove(observer);
    }

}