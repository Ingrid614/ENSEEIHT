package agents;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import platform.Agent;
import platform.AgentMessage;
import platform.MoveException;
import platform.Node;

public class HotelAgent implements Agent {

    private String name;
    private Node origin;
    private transient Hashtable<String, Object> nameServer;
    private int step = 0;
    private List<String> hotelNames;
    private Map<String,String> phones = new HashMap<>();
    private long startTime = -1;

    @Override
    public void init(String name, Node origin) {
        this.name = name;
        this.origin = origin;
    }

    @Override
    public void setNameServer(Hashtable<String, Object> ns) {
        this.nameServer = ns;
    }

    @Override
    public Hashtable<String, Object> getNameServer() {
        return this.nameServer;
    }

    @Override
    public void main() throws MoveException {
        // time measuring
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }
        if (step == 0) {

            System.out.println("Liste initiale d'hotels : " + hotelNames);
            System.out.println("Moving to Hotel server");
            step = 1;
            move(new Node("localhost", 2001));
            return;
        }
        else if (step == 1) {
            // Serveur 1 : hotels
            hotelNames = (List<String>) nameServer.get("hotels");
            System.out.println("Moving to Directory server to fill numbers");
            step = 2;
            move(new Node("localhost", 2002));
            return;
        }

        else if (step == 2) {
            // Serveur 2 : annuaire
            Map<String,String> directory =
                (Map<String,String>) nameServer.get("directory");

            for (String h : hotelNames)
                phones.put(h, directory.get(h));

            System.out.println("Going to origin node");
            step = -1;
            back();
            return;
        }

        else {
            // Retour origine
            System.out.println("Liste d'hotels récupérée dans les deux seveurs");
            phones.forEach((h,p) ->
                System.out.println(h + " → " + p));
        }
        long endTime = System.currentTimeMillis();

            System.out.println("\nThe Agent test took "
                + (endTime - startTime) + " ms");

    }

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

    public void back() throws MoveException {
        move(origin);
    }

    // Envoie du code de l'agent et de toutes les classes dont il dépend(HotelAgent, Interface Agent, classe Object)
    private void collectClass(Class<?> c,
                          Map<String, byte[]> map)
        throws IOException {

        if (map.containsKey(c.getName()))
            return;

        String path =
            "/" + c.getName().replace('.', '/') + ".class";

        InputStream is = c.getResourceAsStream(path);
        byte[] code = is.readAllBytes();
        map.put(c.getName(), code);

        if (c.getSuperclass() != null)
            collectClass(c.getSuperclass(), map);

        for (Class<?> i : c.getInterfaces())
            collectClass(i, map);
    }


}
