package main;

import java.io.*;
import java.net.Socket;

public class UploadHandler extends Thread {


    P2PNode client;
    Config config;
    P2P p2p;
    Logger logger;

    UploadHandler(P2PNode client, Config config, P2P p2p, Logger logger) {
        this.client=client;
        this.config=config;
        this.p2p=p2p;
        this.logger=logger;
    }

    @Override
    public void run() {

        DataOutputStream output = client.getDataOutputStream();
        DataInputStream input = client.getDataInputStream();

        logger.serverLog("--------------------------------------------------------------");
        logger.serverLog("Download request received from client with ID: "+client.getId());


        try {
            /* Get type of download - chunked or whole */
            String typeOfDownload=input.readUTF();
            logger.serverLog("Download request Type: "+typeOfDownload);

            /* Whole upload */
            if(typeOfDownload.equals("whole")) {

                /* Get the file name */
                String fileName=input.readUTF();
                /* Send the file */
                uploadFile(fileName, input, output);
                /* Request has been serviced shutdown the socket */

            } else { /* Chunked upload */

                /* Get the file name */
                String fileName = input.readUTF();
                logger.serverLog("Filename: " + fileName);
                /* Get the File offset to start sending file */
                String offset = input.readUTF();
                logger.serverLog("Offset: " + offset);
                /* Send the file size */
                String chunkSize = input.readUTF();
                logger.serverLog("Chunk Size : " + chunkSize);
                /* Send the file */
                uploadFile(fileName, Integer.parseInt(offset), Integer.parseInt(chunkSize), output);
                /* Request has been serviced shutdown the socket */
            }
            client.exit();
            /* Reduce the client by 1 */
            p2p.noOfClients--;
            /* Shut down thread - Auto Stop Let java handle it*/
            logger.serverLog("--------------------------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
          client.exit();
        }

    }

    private void uploadFile(String fileName, DataInputStream input, DataOutputStream output) {
        String folderPath=config.getHostFilePath();
        File file = new File(folderPath+"/"+fileName);
        byte[] fileBytes = new byte[(int) file.length()];
        logger.serverLog("Writing to client buffer "+fileBytes.length+" bytes! ");

        try {

            /* Load into buffered input stream */
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            bufferedInputStream.read(fileBytes, 0, fileBytes.length);
            /* Write to client socket */
            output.write(fileBytes, 0, fileBytes.length);
            output.flush();
            bufferedInputStream.close();
            System.out.println("Uploaded file "+fileName+" to client with ID: "+client.getId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Chunk size here is in int so it only supports upto 2^32 or 10^9 values, so about a size of 1GB and nothing more than that */
    private void uploadFile(String fileName, int offset, int chunkSize, DataOutputStream output) {
        String folderPath=config.getHostFilePath();

        try {
            RandomAccessFile file = new RandomAccessFile(folderPath+"/"+fileName,"r");
            byte[] fileBytes = new byte[chunkSize];

            /* Seek and set file pointer to that point */
            file.seek((long) offset * chunkSize);
            /* Read into byte array */
            int bytesRead=file.read(fileBytes, 0, fileBytes.length);
            /* Write to client socket */
            if(bytesRead==fileBytes.length) {
                output.write(fileBytes, 0, fileBytes.length);
                output.flush();
            } else {
                output.writeUTF("error");
            }
            logger.serverLog("Uploaded file : "+fileName+" Chunk Number : "+offset+" of Size : "+chunkSize+" to client with ID: "+client.getId());
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
