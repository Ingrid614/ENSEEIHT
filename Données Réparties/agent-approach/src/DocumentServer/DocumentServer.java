package DocumentServer;

import platform.AgentServer;
import platform.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class DocumentServer {

    public static void main(String[] args) throws Exception {

        AgentServer server = new AgentServer(2004);
        
        int xbyte = Configuration.sizeOfFileKB * 1024;
        byte[] document = new byte[xbyte];
        new Random().nextBytes(document);
        

        server.bind("document", document);
        server.start();
    }
}
