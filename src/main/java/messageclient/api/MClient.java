package messageclient.api;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class MClient implements AutoCloseable {

    @NotNull private final List<Client> clients;
    @NotNull private final InetSocketAddress address;

    @NotNull private final Set<ClientListObserver> observers;

    private final AtomicInteger noOfClients = new AtomicInteger(0);

    public MClient(InetSocketAddress address) {
        this(new ArrayList<>(), address);
    }

    public MClient(@NotNull List<Client> clients, @NotNull InetSocketAddress address) {
        this.clients = clients;
        this.address = address;
        this.observers = new HashSet<>();
    }

    public Client newClient(InetSocketAddress currentAddress) throws IOException {
        Client client = Client.open(noOfClients.incrementAndGet(), currentAddress);
        addClient(client);
        return client;
    }

    private void addClient(Client client) {
        synchronized (this) {
            clients.add(client);
        }
        for (var observer : observers)
            observer.clientListChanged();
    }

    public void register(ClientListObserver observer) {
        observers.add(observer);
    }

    public void deregister(ClientListObserver observer) {
        observers.remove(observer);
    }


    @Override
    public void close() throws IOException {
        for (Client c: clients) {
            c.close();
        }
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public Iterable<Client> findClients() {
        return List.copyOf(clients);
    }

    public synchronized void refreshClients() {
        ArrayList<Client> newclients = new ArrayList<>();
        for (Client c: clients) {
            try {
                newclients.add(c.reconnect());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clients.clear();
        clients.addAll(newclients);
    }
}
