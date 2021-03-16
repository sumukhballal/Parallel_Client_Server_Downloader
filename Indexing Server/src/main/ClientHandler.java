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

    ClientHandler(Socket socket, ConcurrentHashMap<String, Node> nodes) {
        this.nodes=nodes;
        this.socket=socket;
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
            System.out.println("Connected to client with ID: "+getClientId()+" !");
            System.out.println("Received command "+command+" from client with ID: "+getClientId());

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
                    System.out.println("This option is not available! Exiting! ");
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
        System.out.println("Exiting client with ID "+clientId);

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
        System.out.println("Adding a new file to  client " + clientId + "! ");

        if(isRegistered()) {
            done();
        }
        else {
            System.out.println("Client with ID: "+clientId+" has not been registered before! ");
            error();
        }

        try {
            /* Blocking call to read from all files from client comma separated */
            String fileNames=dataInputStream.readUTF();
            Node currentNode=nodes.get(clientId);
            boolean result=currentNode.addFiles(fileNames.trim());
            if(result) {
                System.out.println("Added all files to client with ID: "+clientId);
                done();
            } else {
                System.out.println("Unable to add files to client with ID: "+clientId);
                error();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateFileDelete() {
        System.out.println("Deleting a file to client " + clientId + "! ");

        if(isRegistered()) {
            done();
        }
        else {
            System.out.println("Client with ID: "+clientId+" has not been registered before! ");
            error();
        }

        try {
            /* Blocking call to read from all files from client comma separated */
            String fileNames=dataInputStream.readUTF();
            Node currentNode=nodes.get(clientId);
            boolean result=currentNode.deleteFiles(fileNames.trim());
            if(result) {
                System.out.println("Deleted all files to client with ID: "+clientId);
                done();
            } else {
                System.out.println("Unable to delete files to client with ID: "+clientId);
                error();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void query() {
        System.out.println("Querying for a file name on all nodes!");

        if(isRegistered()) {
            done();
        }
        else {
            System.out.println("Client with ID: "+clientId+" has not been registered before! ");
            error();
            return;
        }

        try {
            /* Blocking call to read  client which file needs to be checked */
            String fileName=dataInputStream.readUTF();
            List<String> peerNodesWithFile=new ArrayList<>();

            for(Map.Entry node : nodes.entrySet()) {
                Node currentNode = (Node)node.getValue();

                if(currentNode.files.contains(fileName))
                    peerNodesWithFile.add(currentNode.getId());
            }

            dataOutputStream.writeUTF(peerNodesWithFile.toString());
            done();
        } catch (IOException e) {
            e.printStackTrace();
            error();
        }
    }

    private void register() {

        System.out.println("Checking if client "+clientId+" has been registered previously!");
        if(isRegistered()) {
            System.out.println("Client "+clientId+" has been registered already!");
            done();
            return;
        }

        Node node = new Node(clientId, clientIpAddress, clientPortNumber);
        nodes.put(clientId, node);
        System.out.println("Client "+clientId+" has been registered! ");
        done();
    }

    private void unregister() {
        System.out.println("Checking if client "+clientId+" has been registered previously!");

        if(isRegistered()) {
            nodes.remove(clientId);
            System.out.println("Client "+clientId+" has been unregistered!");
            return;
        }

        System.out.println("Client "+clientId+" was not registered previously! ");
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
