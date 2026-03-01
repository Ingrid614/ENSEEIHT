// servers/ClientOrigin.java
package ClientDocument;

import agents.CompressionAgent;
import platform.*;

public class ClientDocument {

    public static void main(String[] args) throws Exception {

        AgentServer origin = new AgentServer(2003);

        new Thread(() -> {
            try {
                origin.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);

        CompressionAgent agent = new CompressionAgent();
        agent.init("CompressionAgent",
                   new Node("localhost", 2003));

        agent.move(new Node("localhost", 2004));
    }
}
