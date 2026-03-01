import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

// server a returns a list of names 
public interface ServerA extends Remote {
   List<String> getNames() throws RemoteException;
}