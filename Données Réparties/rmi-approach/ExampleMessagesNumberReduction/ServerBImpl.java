import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class ServerBImpl extends UnicastRemoteObject implements ServerB {

    private HashMap<String, String> repository;

    public ServerBImpl() throws RemoteException {
        
        //the repository with names and phone numbers
        repository = new HashMap<>();
        for (int i = 0; i < Configuration.recordnum; i++) {
            String name = "Name" + i;
            String number = "+3933" + i;
            repository.put(name, number);
        }
    }

    @Override
    public String getPhone(String name) throws RemoteException {
        return repository.get(name);
    }

    public static void main(String[] args) {
        try {
            ServerB bserver = new ServerBImpl();
            //saving in rmi registry
            Naming.rebind("//localhost/RepositoryService", bserver);
            System.out.println("Repository service ready");
        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}