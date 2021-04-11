package main;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

public class P2P {

    int noOfClients=0;
    static Logger logger;

    public static void main(String[] args) {

        /* Start the Peer Node and perform all intial duties */

        /* Initial Duties
        1.) Read config properties
        2.) Connect to Indexing Server on startup
        3.)
        * */

        int mode=0;
        if(args.length!=0) {
            mode=Integer.parseInt(args[1]);
        }

        P2P p2p = new P2P();
        /* Read config properties */
        Config config=p2p.readConfigFile();
        /* Create the log files */
        p2p.createLogFile(mode);
        logger.serverLog("Starting up the P2P Node with ID : "+config.getId());
        IndexingServer indexingServer=p2p.connectToIndexingServer(config);
        p2p.registerFiles(indexingServer, config);

        try {
            /* Start server socket */
            ServerSocket serverSocket = new ServerSocket(config.getPeerNodePort());
            logger.serverLog("P2P Node started up!");
            logger.serverLog("Listening on "+config.getPeerNodePort());

            /* Client - The client is configured as a thread. */
            new Client(indexingServer, config, args, logger).start();

            /* This is the server */
            while(true) {
                /* Accept a connection from another P2P node */
                Socket socket = serverSocket.accept();
                DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());

                /* Read which client is requesting */
                String clientId=dataInputStream.readUTF();
                logger.serverLog("Accepted Client with ID: "+clientId+" ! Total clients! "+(++p2p.noOfClients));

                /* Service the client */
                String response=dataInputStream.readUTF();
                if(response.equals("download")) {
                    /* Send socket to upload thread and let it do the rest */
                    new UploadHandler(new P2PNode(clientId, socket, dataOutputStream, dataInputStream), config, p2p, logger).start();
                }

                /* Done */
            }


        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            indexingServer.exit();
        }

    }

    /* Connect to the indexing server */
    private IndexingServer connectToIndexingServer(Config config) {

        IndexingServer indexingServer=null;

        try {

            InetAddress indexingServerIP = InetAddress.getByName(config.getIndexingNodeIp());
            int indexingServerPort = config.getIndexingNodePort();
            Socket indexingServerSocket = new Socket(indexingServerIP, indexingServerPort);
            DataInputStream indexingServerInputStream = new DataInputStream(indexingServerSocket.getInputStream());
            DataOutputStream indexingServerOutputStream = new DataOutputStream(indexingServerSocket.getOutputStream());
            indexingServerOutputStream.writeUTF(Integer.toString(config.getPeerNodePort()));
            logger.serverLog("Connected to the Indexing Server! ");

            indexingServer = new IndexingServer(indexingServerIP.toString(), indexingServerPort, indexingServerSocket, indexingServerOutputStream, indexingServerInputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return indexingServer;
    }

    /* Register the files in the files directory to the indexing server */
    private void registerFiles(IndexingServer indexingServer, Config config) {

        try {
            /* Read all files present on the server and register them with the Indexing server */

            DataOutputStream output = indexingServer.getDataOutputStream();
            DataInputStream input = indexingServer.getDataInputStream();

            String hostFilesDirectory=config.getHostFilePath();
            File hostFilesFolder = new File(hostFilesDirectory);

            if(hostFilesFolder==null){
                logger.serverLog("The file directory does not exist!");
            }

            StringBuilder resultFiles = new StringBuilder();

            for(File file : hostFilesFolder.listFiles()) {
                    resultFiles.append(file.getName());
                    resultFiles.append(":");
                    /* Send File size */
                    resultFiles.append(file.length());
                    resultFiles.append(":");
                    /* Get MD5 of file */
                    String md5checkSum=getMD5Checksum(MessageDigest.getInstance("MD5"), file.getPath());
                    /* Send MD5 checksum */
                    resultFiles.append(md5checkSum);
                    resultFiles.append(",");
            }

            if(resultFiles.length() > 0 && resultFiles.charAt(resultFiles.length()-1)==',')
                resultFiles.deleteCharAt(resultFiles.length()-1);

            /* Speak to the indexing server */

            output.writeUTF("register");
                /* Check if register happened successfully */
            String response = input.readUTF();

            if(response.equals("done")) {
                logger.serverLog("Successfully registered with Indexing server ");
            }

            /* Register happened successfully */
            /* Register the files with the server */

            output.writeUTF("update_add");
            response=input.readUTF();

            if(response.equals("done")) {
                /* Client is registerd, send the files */
                output.writeUTF(resultFiles.toString());
                response = input.readUTF();

                if (response.equals("done")) {
                    logger.serverLog("All files have been regsitered with the Indexing server! ");
                }
            } else {
                logger.serverLog("Client has not been registered with Indexing server before! ");
            }

        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String getMD5Checksum(MessageDigest messageDigest, String filePath) throws IOException {

        StringBuilder md5Hash = new StringBuilder();

        try(DigestInputStream dis = new DigestInputStream(new FileInputStream(filePath), messageDigest)) {

            while(dis.read() != -1) {
                messageDigest=dis.getMessageDigest();
            }
        } catch(IOException e) {
            logger.serverLog("Something when wrong when calculating the md5 hash! ");
            e.printStackTrace();
        }

        for( byte b : messageDigest.digest()) {
            md5Hash.append(String.format("%02x",b));
        }

        return md5Hash.toString();
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
                Integer.parseInt(configProperties.get("peer_node_port")),
                configProperties.get("peer_node_ip"),
                Integer.parseInt(configProperties.get("indexing_server_port")),
                configProperties.get("indexing_server_ip"),
                configProperties.get("host_files_directory"));

        return config;
    }

    /* Create a log file in logs directory */
    private void createLogFile(int mode) {
        String serverLogPath=System.getProperty("user.dir")+"/logs/server.log";
        String clientLogPath=System.getProperty("user.dir")+"/logs/client.log";

        File serverFile = new File(serverLogPath);
        File clientFile = new File(clientLogPath);

        try {
        /* Create the log files if not created */
            serverFile.createNewFile();
            clientFile.createNewFile();

            /* Assign logger */
            logger=new Logger(serverLogPath, clientLogPath, mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
