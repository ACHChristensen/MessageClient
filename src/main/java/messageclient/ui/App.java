package messageclient.ui;

import messageclient.Utils;
import messageclient.api.Client;
import messageclient.api.ClientListObserver;
import messageclient.api.MClient;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;

public class App extends JFrame implements ClientListObserver {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 16);

    private final MClient mclient;
    private final JTextField addressField;
    private final JTextField portField;
    private final DefaultListModel<String> model;
    private final JList<String> list;

    public App(MClient mclient) {
        super("Message Client App");
        this.mclient = mclient;

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLocationRelativeTo(null);
        setResizable(false);

        InetSocketAddress address = mclient.getAddress();
        this.addressField =
                new JTextField(address.getAddress().getCanonicalHostName(), 16);

        this.portField =
                new JTextField("" + address.getPort(), 5);
        add(createClientControls(this.addressField, this.portField),
                BorderLayout.NORTH);

        this.model = new DefaultListModel<>();
        this.list = createClientList(this.model);
        add(list, BorderLayout.CENTER);

        pack();
        setVisible(true);
    }

    private JComponent createClientControls(JTextField addressField, JTextField portField) {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        addressField.setFont(FONT);
        panel.add(addressField);

        panel.add(Box.createHorizontalStrut(4));

        portField.setFont(FONT);
        panel.add(portField);


        JButton add;
        try {
            Image img = ImageIO.read(ClassLoader.getSystemResource("circle-plus.png"));
            img = img.getScaledInstance(16, 16, 16);
            ImageIcon icon = new ImageIcon(img);
            add = new JButton(icon);
        } catch (IOException e) {
            add = new JButton("+");
        }

        panel.add(Box.createHorizontalStrut(4));
        add.addActionListener(e -> this.addClient());
        add.setBorder(null);
        add.setFont(FONT);
        panel.add(add);

        JButton refresh;
        try {
            // Icons from https://lineicons.com/free/
            Image img = ImageIO.read(ClassLoader.getSystemResource("circle-reload.png"));
            img = img.getScaledInstance(16, 16, 16);
            ImageIcon icon = new ImageIcon(img);
            refresh = new JButton(icon);
        } catch (IOException e) {
            refresh = new JButton("⟳");
        }

        refresh.setBorder(null);
        refresh.setFont(FONT);
        panel.add(Box.createHorizontalStrut(4));
        panel.add(refresh);
        refresh.addActionListener(e -> this.refreshClients());

        panel.add(Box.createHorizontalStrut(6));
        return panel;
    }

    private void refreshClients() {
        mclient.restartClients();
    }

    private JList<String> createClientList(ListModel<String> model) {
        return new JList<String>(model);
    }

    private InetSocketAddress getCurrentAddress() {
        return new InetSocketAddress(addressField.getText(),
                Integer.parseInt(portField.getText()));
    }

    private void addClient() {
        try {
            Client client = mclient.newClient(getCurrentAddress());
            ClientWindow window = ClientWindow.fromClient(client);
            client.connect();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    public static void main(String[] args) {
        InetSocketAddress address = args.length == 2
                ? Utils.parseInetAddress(args[0], args[1])
                : new InetSocketAddress("165.232.73.234", 6666);
        var mclient = new MClient(address);

        SwingUtilities.invokeLater(() -> {
            App app = new App(mclient);
            mclient.register(app);
        });

    }

    @Override
    public void clientListChanged() {
        model.clear();
        for (Client c : mclient.findClients()) {
            String name = c.toString();
            model.addElement(name);
        }

        this.pack();
        this.repaint();
    }
}
