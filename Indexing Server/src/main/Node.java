package main;

import java.util.ConcurrentModificationException;
import java.util.HashSet;

public class Node {


    /* Unique by the nodes IP and Port Number - Assuming a node with the same IP and Port number will not exist
    * In a system deployed to the same host, we assume a Peer Node process will be running on a different port number.
    * */
    String id;
    String ipAddress;
    int portNumber;
    HashSet<String> files;

    protected Node(String clientId, String ipAddress, int portNumber) {
        this.id=clientId;
        this.ipAddress=ipAddress;
        this.portNumber=portNumber;
        files=new HashSet<>();
    }

    public String getId() {
        return id;
    }

    protected boolean addFiles(String filesToAdd) {

        if(filesToAdd==null)
            return false;

        try {
            String[] fileArray = filesToAdd.trim().split(",");

            for(String file : fileArray) {
                files.add(file);
                System.out.println("Added file: "+file+" to client with ID "+id);
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            System.out.println("Unable to add files since two or more threads are accessing the file hashset! ");
            return false;
        }

        return true;
    }

    protected boolean deleteFiles(String filesToDelete) {

        if(filesToDelete==null)
            return false;

        try {
            String[] fileArray = filesToDelete.trim().split(",");

            for(String file : fileArray) {
                files.remove(file);
                System.out.println("Deleted file: "+file+" from client with ID "+id);
            }
        } catch (ConcurrentModificationException e) {
            e.printStackTrace();
            System.out.println("Unable to delete files since two or more threads are accessing the file hashset! ");
            return false;
        }

        return true;
    }

}
