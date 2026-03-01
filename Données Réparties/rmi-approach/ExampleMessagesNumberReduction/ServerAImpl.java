import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ServerAImpl extends UnicastRemoteObject implements ServerA {

    private List<String> names;

    public ServerAImpl() throws RemoteException {

        // generate recordnum names
        names = new ArrayList<>();

        for (int i = 0; i < Configuration.recordnum; i++) {
            names.add("Name" + i);
        }
    
    }

    @Override
    public List<String> getNames() throws RemoteException {
        System.out.println("SA: name list: ");
        return names;
    }

    public static void main(String[] args) {
        try {
            ServerA aserver = new ServerAImpl();

            //saving in rmi registry
            Naming.rebind("//localhost/NameService", aserver);
            System.out.println("Name Service ready");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}