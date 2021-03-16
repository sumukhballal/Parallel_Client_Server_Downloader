package main;

import java.io.*;
import java.net.Socket;

public class UploadHandler extends Thread {


    P2PNode client;
    Config config;

    UploadHandler(P2PNode client, Config config) {
        this.client=client;
        this.config=config;
    }

    @Override
    public void run() {

        DataOutputStream output = client.getDataOutputStream();
        DataInputStream input = client.getDataInputStream();

        try {
            /* Get the file name */
            String fileName=input.readUTF();
            /* Send the file size */
            output.writeUTF(getFileSize(fileName));
            /* Send the file */
            uploadFile(fileName, input, output);
            /* Request has been serviced shutdown the socket */
            client.exit();
            /* Shut down thread - Auto Stop Let java handle it*/

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void uploadFile(String fileName, DataInputStream input, DataOutputStream output) {
        String folderPath=config.getHostFilePath();
        File file = new File(folderPath+"/"+fileName);
        byte[] fileBytes = new byte[(int) file.length()];

        try {

            /* Load into buffered input stream */
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            bufferedInputStream.read(fileBytes, 0, fileBytes.length);
            bufferedInputStream.close();
            /* Write to client socket */
            output.write(fileBytes, 0, fileBytes.length);
            output.flush();
            System.out.println("Uploaded file "+fileName+" to client with ID: "+client.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String getFileSize(String fileName) {
        if(fileName==null)
            return "0";

        String folderPath=config.getHostFilePath();
        File file = new File(folderPath+"/"+fileName);

        return Long.toString(file.length());
    }
}
