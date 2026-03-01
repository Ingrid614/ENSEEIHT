import java.rmi.Remote;
import java.rmi.RemoteException;


// method for downloading the file that with rmi is raw (not compressed)
// taking the name of the file and returningits content as a byte array

public interface FileService extends Remote {
    byte[] downloadFile(String fileName) throws RemoteException;
}