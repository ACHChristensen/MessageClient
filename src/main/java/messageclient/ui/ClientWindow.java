package messageclient.ui;

import messageclient.api.Client;
import messageclient.api.MessageObserver;

import javax.swing.*;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;

public class ClientWindow extends JFrame implements MessageObserver {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);
    private final Client client;
    private final JTextArea textArea;

    public ClientWindow(Client client) {
        super("MessageClient");
        textArea = createTextArea();
        this.client = client;

        add(createScrollableTextArea(textArea), BorderLayout.CENTER);
        add(createTextField(), BorderLayout.SOUTH);

        setSize(800, 800);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    }

    public static ClientWindow fromClient(Client client) {
        ClientWindow w = new ClientWindow(client);
        client.register(w);
        return w;
    }


    public void append(String string)  {
        textArea.append(string);
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(FONT);
        textField.addActionListener(e -> {
            var msg = e.getActionCommand();
            try {
                client.sendMessage(msg + "\n");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            textField.setText("");
        });
        return textField;
    }

    private static JTextArea createTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(FONT);
        DefaultCaret caret = new DefaultCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        textArea.setCaret(caret);
        return textArea;
    }

    private static JScrollPane createScrollableTextArea(JTextArea textArea) {
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
        return scroll;
    }

    @Override
    public void receivedMessage(String message) {
        append(message);
    }

    @Override
    public void connectionStarted(InetSocketAddress address) {
        append("-- Connected to " + address + "\n");
    }

    @Override
    public void connectionClosed() {
        append("-- Connection closed.\n");
    }
}
