
package HotelServer;

import java.io.ObjectInputFilter.Config;
import java.util.ArrayList;
import java.util.List;

import platform.AgentServer;
import platform.Configuration;

public class HotelServer {
    public static void main(String[] args) throws Exception {
        AgentServer server = new AgentServer(2001);
        List<String> list = new ArrayList<>();
        for(int i = 0; i < Configuration.recordnum; i++){
            list.add("Hotel"+i);
        }

        // Objet local simple
        server.bind("hotels",list);

        server.start();
    }

   
}
