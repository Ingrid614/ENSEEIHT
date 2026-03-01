import java.rmi.Naming;
import java.io.ByteArrayInputStream;
import java.util.zip.GZIPInputStream;

public class CompressClient {

    public static void main(String[] args) {
        try {
            
            FileService service = (FileService) Naming.lookup("//localhost/FileService");
            
            System.out.println("-starting download-");

           //timing
            long start = System.currentTimeMillis();

            
            byte[] compressedData = service.downloadFile("testfile.dat");
            
            // decompressing
            ByteArrayInputStream ins = new ByteArrayInputStream(compressedData);
            GZIPInputStream zip = new GZIPInputStream(ins);

            byte[] decompressedData = zip.readAllBytes();

            long end = System.currentTimeMillis();
            

               //------ Mostriamo la differenza di dimensioni
               System.out.println("Downloaded file size: " + (compressedData.length / 1024) + " KB");
               System.out.println("Decompressed file size: " + (decompressedData.length / 1024) + " KB");
               
            System.out.println(" \n time taken: " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}