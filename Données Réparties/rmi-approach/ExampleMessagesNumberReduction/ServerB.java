import java.rmi.Remote;
import java.rmi.RemoteException;


//server b manages a telephone repository
public interface ServerB extends Remote {
// to test n times with the client
   String getPhone(String name) throws RemoteException;
}