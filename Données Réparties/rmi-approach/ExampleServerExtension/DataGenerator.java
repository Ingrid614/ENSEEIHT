//generates repating data to test its compression
public class DataGenerator {
    
    public static byte[] generateFile(int x) {
        int xbyte = x * 1024;
        byte[] data = new byte[xbyte];
        
        // we fill the file with As 
        for (int i = 0; i < xbyte; i++) {
            data[i] = (byte) 'A';
        }
        
        return data;
    }
}


// javac *.java
// rmiregistry
// java FileServer
// java CompressClient
