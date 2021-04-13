package main;

import java.util.ConcurrentModificationException;
import java.util.HashMap;

public class Node {


    /* Unique by the nodes IP and Port Number - Assuming a node with the same IP and Port number will not exist
    * In a system deployed to the same host, we assume a Peer Node process will be running on a different port number.
    * */
    String id;
    String ipAddress;
    int portNumber;
    HashMap<String, FileDescription> files;
    Logger logger;

    protected Node(String clientId, String ipAddress, int portNumber, Logger logger) {
        this.id=clientId;
        this.ipAddress=ipAddress;
        this.portNumber=portNumber;
        this.logger=logger;
        files=new HashMap<>();
    }

    public String getId() {
        return id;
    }

    protected boolean addFiles(FileDescription file) {

        if(file==null)
            return false;

        try {
                files.put(file.getFileName(), file);
                logger.serverLog("Added file: "+file.getFileName()+" to client with ID : "+id);
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            logger.serverLog("Unable to add files since two or more threads are accessing the file hashset! ");
            return false;
        }

        return true;
    }

    protected boolean deleteFiles(String filename) {

        if(filename==null)
            return false;

        try {
            files.remove(filename);
            logger.serverLog("Deleted file: "+filename+" to client with ID : "+id);
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            logger.serverLog("Unable to add files since two or more threads are accessing the file hashset! ");
            return false;
        }

        return true;
    }

}
