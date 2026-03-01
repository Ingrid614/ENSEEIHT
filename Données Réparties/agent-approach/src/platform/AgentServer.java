// platform/AgentServer.java
package platform;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public class AgentServer {

    private final int port;
    private final Hashtable<String,Object> nameServer =
        new Hashtable<>();

    public AgentServer(int port) {
        this.port = port;
    }

    public void bind(String name, Object obj) {
        nameServer.put(name, obj);
    }

    public void start() throws Exception {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("Server listening on " + port);

        while (true) {
            Socket s = ss.accept();
            new Thread(() -> handle(s)).start();
        }
    }

    private void handle(Socket s) {
        try (ObjectInputStream ois =
                 new ObjectInputStream(s.getInputStream())) {

            AgentMessage msg =
                (AgentMessage) ois.readObject();

            AgentClassLoader loader =
                new AgentClassLoader(msg.classes);

            Class<?> agentClass =
                loader.loadClass(msg.mainClass);

            Agent agent =
                (Agent) agentClass
                    .getDeclaredConstructor()
                    .newInstance();

            ObjectInputStream stateIn =
                new ObjectInputStream(
                    new ByteArrayInputStream(msg.agentState));

            agent = (Agent) stateIn.readObject();
            agent.setNameServer(nameServer);
            agent.main();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
