package ClientHotel;

import agents.HotelAgent;
import platform.*;

public class ClientHotel {
    public static void main(String[] args) throws Exception {

        AgentServer origin = new AgentServer(2000);

        new Thread(() -> {
            try {
                origin.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(1000);

        HotelAgent agent = new HotelAgent();
        agent.init("AgentHotels", new Node("localhost", 2000));
        agent.move(new Node("localhost", 2000));
    }
}
