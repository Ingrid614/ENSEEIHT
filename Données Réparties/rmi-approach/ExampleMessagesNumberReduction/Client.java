import java.rmi.Naming;
import java.util.List;

public class Client {
    public static void main(String[] args) {
        try {
            
            ServerA nameserv = (ServerA) Naming.lookup("//localhost/NameService");
            
            ServerB repositoryserv = (ServerB) Naming.lookup("//localhost/RepositoryService");

            System.out.println("client-server test with " + Configuration.recordnum + " elements ");




            // time measuring
            long start = System.currentTimeMillis();

                // getting names from server a
                List<String> nomi = nameserv.getNames();
                System.out.println(nomi.size() + " names received");

                //getting phone numbers (of the names from server a) from server b
                for (String nome : nomi) {
                    String numero = repositoryserv.getPhone(nome);
                        System.out.println( nome + "'s phone number: " + numero);

                }

            long end = System.currentTimeMillis();
            


    
            System.out.println("\n \n The RMI test took " + (end - start) + " ms");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}