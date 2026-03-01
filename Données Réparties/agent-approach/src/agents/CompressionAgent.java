// agents/CompressionAgent.java
package agents;

import platform.*;

import java.io.*;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

public class CompressionAgent implements Agent {

    private String name;
    private Node origin;
    private transient Hashtable<String,Object> nameServer;

    private byte[] compressedDocument;
    private int step = 0;
    private long startTime = -1;
    private int originalSize = -1;



    @Override
    public void init(String name, Node origin) {
        this.name = name;
        this.origin = origin;
    }

    @Override
    public void setNameServer(Hashtable<String,Object> ns) {
        this.nameServer = ns;
    }

    @Override
    public Hashtable<String,Object> getNameServer() {
        return nameServer;
    }

    @Override
    public void main() throws MoveException {

        

        if (step == 0) {
            // Sur le serveur document
            
            byte[] document = (byte[]) nameServer.get("document");

            originalSize = document.length;

            System.out.println("Document received");
            System.out.println("Original document size: "
                + originalSize + " bytes");

            System.out.println("Compressing document...");
            compressedDocument = compress(document);

            step = 1;
            back();
            return;
        }

        if (step == 1) {
            // Retour client
            long endTime = System.currentTimeMillis();

            System.out.println("\n=== Results ===");
            System.out.println("Original size   : "
                + originalSize + " bytes");
            System.out.println("Compressed size : "
                + compressedDocument.length + " bytes");

            double ratio =
                (double) compressedDocument.length / originalSize;

            System.out.printf("Compression ratio: %.3f%n", ratio);

            System.out.println("\nThe Agent test took "
                + (endTime - startTime) + " ms");
        }
    }



    // Algorithme CLIENT
    private byte[] compress(byte[] data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
     public void move(Node target) throws MoveException {
        try{
            // Sérialisation de l’agent
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oosAgent = new ObjectOutputStream(bos);
            oosAgent.writeObject(this);
            oosAgent.flush();
            byte[] agentState = bos.toByteArray();

            // Récupération du bytecode
            HashMap<String, byte[]> classes = new HashMap<>();
            collectClass(this.getClass(), classes);

            // Construction du message
            AgentMessage msg = new AgentMessage();
            msg.agentState = agentState;
            msg.classes = classes;
            msg.mainClass = this.getClass().getName();

            // Envoi TCP
            Socket socket = new Socket(target.host, target.port);
            ObjectOutputStream out =
                new ObjectOutputStream(socket.getOutputStream());

            out.writeObject(msg);
            out.flush();
            socket.close();
            

        } catch(Exception e) {
            throw new MoveException(e.getMessage());
        }
    }

    @Override
    public void back() throws MoveException {
        move(origin);
    }

    private void collectClass(Class<?> c,
                              Map<String, byte[]> map)
        throws IOException {

        if (map.containsKey(c.getName()))
            return;

        String path =
            "/" + c.getName().replace('.', '/') + ".class";

        InputStream is = c.getResourceAsStream(path);
        map.put(c.getName(), is.readAllBytes());

        if (c.getSuperclass() != null)
            collectClass(c.getSuperclass(), map);

        for (Class<?> i : c.getInterfaces())
            collectClass(i, map);
    }
}
