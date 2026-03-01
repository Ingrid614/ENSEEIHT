// servers/DirectoryServer.java
package DirectoryServer;

import platform.AgentServer;
import platform.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DirectoryServer {

    public static void main(String[] args) throws Exception {
        AgentServer server = new AgentServer(2002);
        Map<String,String> map = new HashMap<>();
        for(int i = 0; i < Configuration.recordnum; i++){
            map.put("Hotel"+i,"+33 69000000"+i);
        }

        server.bind("directory",map);

        server.start();
    }
}
