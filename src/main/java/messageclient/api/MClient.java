package messageclient.api;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

public class MClient implements AutoCloseable {

    private final List<Client> clients;
    private final InetSocketAddress address;

    public MClient(InetSocketAddress address) {
        this(new ArrayList<>(), address);
    }

    public MClient(List<Client> clients, InetSocketAddress address) {
        this.clients = clients;
        this.address = address;
    }


    @Override
    public void close() throws IOException {
        for (Client c: clients) {
            c.close();
        }
    }

}
