package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientHandler extends  Thread {

    ConcurrentHashMap<String, Node> nodes;
    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;
    String clientIpAddress;
    int clientPortNumber;
    String clientId;
    Logger logger;

    ClientHandler(Socket socket, ConcurrentHashMap<String, Node> nodes, Logger logger) {
        this.nodes=nodes;
        this.socket=socket;
        this.logger=logger;
        setDataStreams();
        setClientId();
    }

    /* Commands supported
     *  register
     *  unregister
     *  update file
     *  query
     *  exit
     * */

    @Override
    public void run() {

        try {
        while(true) {

            String command=dataInputStream.readUTF();
            logger.serverLog("Connected to client with ID: "+getClientId()+" !");
            logger.serverLog("Received command "+command+" from client with ID: "+getClientId());

            switch (command) {

                case "register":
                    register();
                    break;
                case "unregister":
                    unregister();
                    break;
                case "update_add":
                    updateFileAdd();
                    break;
                case "update_delete":
                    updateFileDelete();
                    break;
                case "query":
                    query();
                    break;
                case "exit":
                    exit();
                    break;
                default:
                    logger.serverLog("This option is not available! Exiting! ");
                    exit();
                    break;
            }
        }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            unregister();
            exit();
        }
    }

    private void exit() {
        logger.serverLog("Exiting client with ID "+clientId);

        try {
            dataInputStream.close();
            dataOutputStream.close();
            socket.close();
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFileAdd() {
        logger.serverLog("Adding a new file to  client " + clientId + "! ");

        if(isRegistered()) {
            done();
        }
        else {
            logger.serverLog("Client with ID: "+clientId+" has not been registered before! ");
            error();
            return;
        }

        try {
            /* Blocking call to read from all files from client comma separated */
            String fileDescriptions=dataInputStream.readUTF();
            Node currentNode=nodes.get(clientId);

            for(String fileDescription : fileDescriptions.split(",")) {
                String[] fileDescriptingArray=fileDescription.split(":");
                FileDescription fileDescriptor = new FileDescription(fileDescriptingArray[0], Integer.parseInt(fileDescriptingArray[1]),
                        fileDescriptingArray[2]);
                currentNode.addFiles(fileDescriptor);
            }

            logger.serverLog("Added all files to client with ID: "+clientId);
            done();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFileDelete() {
        logger.serverLog("Deleting a file to client " + clientId + "! ");

        if(isRegistered()) {
            done();
        }
        else {
            logger.serverLog("Client with ID: "+clientId+" has not been registered before! ");
            error();
        }

        try {
            /* Blocking call to read from all files from client comma separated */
            String fileNames=dataInputStream.readUTF();
            Node currentNode=nodes.get(clientId);
            boolean result=currentNode.deleteFiles(fileNames.trim());
            if(result) {
                logger.serverLog("Deleted all files to client with ID: "+clientId);
                done();
            } else {
                logger.serverLog("Unable to delete files to client with ID: "+clientId);
                error();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void query() {
        logger.serverLog("Querying for a file name on all nodes!");

        if(isRegistered()) {
            done();
        }
        else {
            logger.serverLog("Client with ID: "+clientId+" has not been registered before! ");
            error();
            return;
        }

        try {
            /* Blocking call to read  client which file needs to be checked */
            String fileName=dataInputStream.readUTF();
            StringBuilder result = new StringBuilder();

            int size=-1;
            String md5="x";

            for(Map.Entry node : nodes.entrySet()) {
                Node currentNode = (Node) node.getValue();

                if (currentNode.files.containsKey(fileName)) {

                    FileDescription fileDescription = currentNode.files.get(fileName);
                    result.append(currentNode.getId());
                    result.append(":");
                    size=fileDescription.getSize();
                    md5=fileDescription.getMd5();
                }
            }

            result.deleteCharAt(result.length()-1);

            if(size!=-1 && !md5.equals("x")) {
                result.append(";");
                result.append(size);
                result.append(";");
                result.append(md5);
                dataOutputStream.writeUTF(result.toString());
                done();
            } else {
                /* No nodes were found to match the query */
                dataOutputStream.writeUTF("none");
                logger.serverLog("No node found matching the query for filename : "+fileName);
                error();
            }

        } catch (IOException e) {
            e.printStackTrace();
            error();
        }
    }

    private void register() {

        logger.serverLog("Checking if client "+clientId+" has been registered previously!");
        if(isRegistered()) {
            logger.serverLog("Client "+clientId+" has been registered already!");
            done();
            return;
        }

        Node node = new Node(clientId, clientIpAddress, clientPortNumber, logger);
        nodes.put(clientId, node);
        logger.serverLog("Client "+clientId+" has been registered! ");
        done();
    }

    private void unregister() {
        logger.serverLog("Checking if client "+clientId+" has been registered previously!");

        if(isRegistered()) {
            nodes.remove(clientId);
            logger.serverLog("Client "+clientId+" has been unregistered!");
            return;
        }

        logger.serverLog("Client "+clientId+" was not registered previously! ");
    }

    private void setDataStreams() {

        try {
            this.dataInputStream = new DataInputStream(socket.getInputStream());
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* A helper function which lets the client know it can start writing new data to the this thread */
    private void done() {
        try {
            dataOutputStream.writeUTF("done");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* A helper function which lets the client know its request was not processed */
    private void error() {
        try {
            dataOutputStream.writeUTF("error");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* A helper function which checks if client has been registered */
    private boolean isRegistered() {

        if(nodes.containsKey(clientId))
            return true;

        return false;
    }

    private void setClientId() {
        try {
            String portNumber = dataInputStream.readUTF();
            clientIpAddress = socket.getRemoteSocketAddress().toString();
            clientPortNumber = Integer.parseInt(portNumber);

            if (clientIpAddress.charAt(0) == '/') {
                clientIpAddress = clientIpAddress.substring(1);
            }
            clientIpAddress=clientIpAddress.split(":")[0];

            clientId = clientIpAddress+":"+clientPortNumber;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getClientId() {
        return clientId;
    }
}
