package platform;

import java.io.Serializable;

public class Node implements Serializable {
    public String host;
    public int port;

    public Node(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String toString() {
        return host + ":" + port;
    }
}
