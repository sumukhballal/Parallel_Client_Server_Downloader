package main;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Client extends Thread {


    /* Fixed Chunk size of 64KB */
    private final int CHUNK_SIZE=64000;

    IndexingServer indexingServer;
    Config config;
    String clientId;
    String[] args;
    Logger logger;

    Client(IndexingServer indexingServer, Config config, String[] args, Logger logger) {
        this.indexingServer=indexingServer;
        this.config=config;
        this.clientId=setClientId();
        this.args=args;
        this.logger=logger;
    }

    /* As a client we should be able to send a download request for a certain file
    * Two things should be done here
    * Send a query request to indexing server
    * Choose a random peer to download file from
    *  */
    @Override
    public void run() {

        /* Based on the args we can run this using scanner or not */

        if(args.length==0) {
            Scanner scanner = new Scanner(System.in);

            while (true) {
                logger.clientLog("\n \n Choose your operation! \n 1.) Download a file \n > \n");
                Integer input = Integer.parseInt(scanner.next());

                switch (input) {
                    case 1:
                        logger.clientLog("Enter File Name! \n >");
                        String fileName = scanner.next();
                        downloadFile(fileName);
                        break;
                    default:
                        logger.clientLog("Not a valid option! ");
                }
            }
        } else {

            String clientMode=args[0];

            /* Evaluation 1 */
            if(clientMode.equals("1")) {

                /* Wait for other processes to start */
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                String fileName=args[1];
                downloadFile(fileName);

            }

            /* Evaluation 2 */
            if(clientMode.equals("2")) {

                String fileNames=args[2];

                for(String fileName : fileNames.trim().split(",")) {
                    downloadFile(fileName);
                }
            }
        }
    }

    /* First send a query request to Indexing server
    * Second select a peer node and download from it.
    *  */
    private void downloadFile(String fileName) {
        try {
            DataOutputStream output=indexingServer.getDataOutputStream();
            DataInputStream input=indexingServer.getDataInputStream();

            /* Query the indexing server */
           logger.clientLog("Querying the index Server for file : "+fileName);

            /* Calculate response time for query to Indexing server */
            long startTime=System.nanoTime();
            output.writeUTF("query");
            String response=input.readUTF();

            if(response.equals("error")) {
                logger.clientLog("Client is unable to download a file because indexing server threw an error during query! ");
                return;
            }

            /* Send filename to be checked */
            output.writeUTF(fileName);
            String nodeListWithFileDescription=input.readUTF();
            response=input.readUTF();
            /* Received a response so calculate response time here */
            long elapsedTime=System.nanoTime() - startTime;

            logger.clientLog("avg_response_time: It took "+elapsedTime+" Nanoseconds to get a response from the Indexing Server!");

            if(response.equals("error")) {
                logger.clientLog("Client is unable to download a file because indexing server threw an error during query! ");
                return;
            }

            /* Download the file from the node */
            downloadRequest(nodeListWithFileDescription, fileName);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadRequest(String nodeListWithFileDescription, String fileName) {

        try {

            /* Get number of nodes we can parallely download from */
            String[] nodeListWithFileDescriptionArray = nodeListWithFileDescription.split(";");
            int numberOfNodes=nodeListWithFileDescriptionArray[0].split(":").length;
            int filesize=Integer.parseInt(nodeListWithFileDescriptionArray[1]);
            String md5Checksum=nodeListWithFileDescriptionArray[2];
            String[] nodeArray=nodeListWithFileDescriptionArray[0].split(":");

            if(numberOfNodes==1 || filesize <= CHUNK_SIZE) {

                logger.clientLog("Downloading file in whole! ");

                /* Download whole file at once */
                P2PNode node = getNodeObject(nodeArray[0]);
                DataOutputStream output = node.getDataOutputStream();
                DataInputStream input = node.getDataInputStream();

                output.writeUTF(clientId);
                /* Send the request */
                output.writeUTF("download");
                /* Send type of download */
                output.writeUTF("whole");
                /* Send download file name */
                output.writeUTF(fileName);

                /* Send the ID first */
                long startTime=System.nanoTime();
                /* Download the file serially */
                downloadSerial(fileName, config.getHostFilePath(), filesize, input);
                /* Received a response so calculate response time here */
                long elapsedTime=System.nanoTime() - startTime;
                logger.clientLog("avg_download_time: It took "+elapsedTime+" Nanoseconds to get download a file "+fileName+" of size "+filesize+" !");

                /* Exit the node once the request has completed */
                node.exit();

            } else {
                /* Download based on chunks */
                logger.clientLog("Downloading file in chunks parallely! ");
                int numberOfChunks=filesize/CHUNK_SIZE;
                int extraChunkSize=filesize-(numberOfChunks*CHUNK_SIZE);

                P2PNode node=null;
                int chunkThreshold=numberOfChunks/numberOfNodes;
                int k=0;
                Thread[] chunkDownloadThreads=new Thread[numberOfChunks];

                int chunkSize=CHUNK_SIZE;

                HashMap<Integer, FileChunk> fileChunkHashMap=new HashMap<Integer, FileChunk>();
                for(int i=0;i<numberOfChunks;i++) {

                    if(i%chunkThreshold==0 && k<numberOfNodes) {
                        node=getNodeObject(nodeArray[k++]);
                    }

                    if(i==numberOfChunks-1)
                        chunkSize=chunkSize+extraChunkSize;

                    chunkDownloadThreads[i]=new Thread(new DownloadHandler(node, fileName, i, chunkSize, fileChunkHashMap , config, logger));
                    chunkDownloadThreads[i].start();
                }

                /* Wait for all threads to finish */
                for(int i=0;i<numberOfChunks;i++) {
                    chunkDownloadThreads[i].join();
                }


                /* Write to output file */

                writeToOutputFile(fileChunkHashMap);

            }

            /* Check the md5 now that the file is downloaded */
            if(checkMd5Checksum(fileName, md5Checksum)) {
                logger.clientLog("MD5 of file and file downloaded matches! ");
                /* Let indexing server know */
                logger.clientLog("Informing Indexing server that filename "+fileName+" has been downloaded to this node! ");
                informIndexingServer(true, fileName);
            } else {
                logger.clientLog("MD5 of file and file downloaded does not match, retry the request! ");
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void writeToOutputFile(HashMap<Integer, FileChunk> fileChunkHashMap) {

        logger.clientLog("File chunks have been downloaded!" );
        logger.clientLog("Recreating file from chunks! ");

        

        for(Integer key : fileChunkHashMap.keySet()) {


            FileChunk fileChunk = fileChunkHashMap.get(key);

        }
    }

    private boolean checkMd5Checksum(String filename, String hostMd5Checksum) {

    }

    /* Inform the indexing server a file has been added/deleted from Server */
    /* Args - supports added/deleted whcih will get see if added is true else it is deleted */
    private void informIndexingServer(boolean added, String fileName) {
        DataOutputStream indexingServerOutput = indexingServer.getDataOutputStream();
        DataInputStream indexingServerInput = indexingServer.getDataInputStream();


        try {
            String operation="update_add";
            if (!added)
                operation="update_delete";

            indexingServerOutput.writeUTF(operation);
            String response=indexingServerInput.readUTF();

            if(response.equals("error")) {
                logger.clientLog("Not possible to inform the indexing server! Retry! ");
                return;
            }

            indexingServerOutput.writeUTF(fileName);
            response=indexingServerInput.readUTF();

            if(response.equals("error")) {
                logger.clientLog("Not possible to inform the indexing server! Retry! ");
                return;
            }

            /* File has been added */
            logger.clientLog("File "+fileName+" has been added to the indexing server! ");


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private P2PNode getNodeObject(String nodeId) {

        try {
            String nodeIP=nodeId.split(":")[0];
            String nodePort=nodeId.split(":")[1];
            InetAddress p2pNodeIP = InetAddress.getByName(nodeIP);
            int p2pNodePort = Integer.parseInt(nodePort);
            Socket p2pNodeSocket = new Socket(p2pNodeIP, p2pNodePort);

            P2PNode clientNode = new P2PNode(nodeId, p2pNodeSocket, new DataOutputStream(p2pNodeSocket.getOutputStream()), new DataInputStream(p2pNodeSocket.getInputStream()));
            return clientNode;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /* Sets the client id for the P2P node to tell other nodes who it is */
    private String setClientId() {
        String clientIp=config.getPeerNodeIp();
        int clientPort=config.getPeerNodePort();

        if(clientIp.equals("localhost"))
            clientIp="127.0.0.1";

        return clientIp+":"+clientPort;
    }

    private void downloadSerial(String fileName, String filePath, int fileSize, DataInputStream input)  {
        logger.clientLog("Downloading file  : " + fileName + " to directory " + filePath + " of size " + fileSize+" bytes!");
        File file = new File(filePath+"/"+fileName);
        byte[] fileBytes = new byte[fileSize];

            try {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(file));
                int bytesRead = input.read(fileBytes, 0, fileBytes.length);
                bufferedOutputStream.write(fileBytes, 0, bytesRead);
                bufferedOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        logger.clientLog("\n File "+fileName+" downloaded! \n");
    }
}
