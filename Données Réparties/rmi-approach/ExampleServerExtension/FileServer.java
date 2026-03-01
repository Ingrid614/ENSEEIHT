import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;
import java.io.IOException;

public class FileServer extends UnicastRemoteObject implements FileService {

    public FileServer() throws RemoteException {
        super();
    }

    //compressing the file into zip
    private byte[] compress(byte[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream zip = new GZIPOutputStream(out);
        zip.write(data);
        zip.close();
        return out.toByteArray();
    }

    @Override
    public byte[] downloadFile(String fileName) throws RemoteException {
        


        
        //to change for tests
        //------------------------------------------------------------------------------
        int sizeKB = 10000; 
        //----------------------------------------------------------------------




        System.out.println("Sending " + sizeKB + " KB file");
        
        byte[] data = DataGenerator.generateFile(sizeKB);

        try {
            
            byte[] compressedData = compress(data);

            System.out.println("File size: " + (data.length / 1024) + " KB") ;
            System.out.println( " Compressed file size: " + (compressedData.length / 1024) + " KB");
            return compressedData;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RemoteException("Compression error ", e);
        }
    }

    public static void main(String[] args) {
        try {
            FileServer server = new FileServer();

            // RMI Registry
            Naming.rebind("//localhost/FileService", server);
            System.out.println("File Server ready.");

        } 
        catch (Exception e) {
            e.printStackTrace();

        }
    }
}