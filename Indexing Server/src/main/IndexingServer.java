package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class IndexingServer {


    public static void main(String[] args) {

        /* Start the Peer Node and perform all intial duties */

        /* Initial Duties
        1.) Read config properties
        2.) Listen to Socket Connections.
        3.) Accept and send it to Client Handler.
        * */


        /* Read config file */

        IndexingServer indexingServer = new IndexingServer();
        Config config=indexingServer.readConfigFile();
        /* A hashmap of Client ID , Node object */
        ConcurrentHashMap<String, Node> nodes=new ConcurrentHashMap<>();

        try {

            ServerSocket serverSocket = new ServerSocket(config.getIndexingNodePort());
            int totalRequests=0;
            System.out.println("Started Indexing server!");
            System.out.println("Listening on port "+config.getIndexingNodePort());
            /* Infinite Loop which listens to sockets */
            while(true) {
                totalRequests+=1;
                Socket socket = serverSocket.accept();
                new ClientHandler(socket, nodes).start();
                System.out.println("Accepted a request!"+" Number of Requests: "+totalRequests);
            }
        } catch (IOException e ){
            e.printStackTrace();
            System.out.println("IOException has been caused in the indexing server! ");
        }
    }

    /* Read config file */
    private Config readConfigFile() {

        String configFilePath=System.getProperty("user.dir")+"/resources/config.properties";
        HashMap<String,String> configProperties=new HashMap<>();

        try {

            BufferedReader reader = new BufferedReader(new FileReader(configFilePath));
            String line=reader.readLine();

            while(line!=null) {
                String[] lineArray=line.split("=");
                configProperties.put(lineArray[0], lineArray[1]);
                line=reader.readLine();
            }

            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /* Create config object */

        Config config = new Config(
                Integer.parseInt(configProperties.get("indexing_server_port")),
                configProperties.get("indexing_server_ip"));

        return config;
    }
}
