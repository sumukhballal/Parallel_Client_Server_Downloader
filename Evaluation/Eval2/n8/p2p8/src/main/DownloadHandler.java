package main;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.HashMap;


public class DownloadHandler extends Thread {

    Logger logger;
    P2PNode node;
    String fileName;
    String folderPath;
    int chunkSize;
    Config config;
    int chunkId;
    HashMap<Integer, FileChunk> fileChunkHashMap;

    DownloadHandler(P2PNode node, String fileName, int chunkId, int chunkSize, HashMap<Integer, FileChunk> fileChunkHashMap, Config config, Logger logger) {
        this.logger = logger;
        this.chunkSize=chunkSize;
        this.chunkId=chunkId;
        this.fileName=fileName;
        this.config=config;
        this.folderPath=config.getHostFilePath();
        this.node=node;
        this.fileChunkHashMap=fileChunkHashMap;
    }

    @Override
    public void run() {


    try {
        /* Download chunked file */
        DataOutputStream output = node.getDataOutputStream();
        DataInputStream input = node.getDataInputStream();
        String clientId=node.getId();

        output.writeUTF(clientId);
        /* Send the request */
        output.writeUTF("download");
        /* Send type of download */
        output.writeUTF("chunk");
        /* Send download file name */
        output.writeUTF(fileName);
        /* Send chunk id */
        output.writeUTF(Integer.toString(chunkId));
        /* Send chunk size */
        output.writeUTF(Integer.toString(chunkSize));
        /* Download chunk */
        download(input);

        /* Exit the node once the request has completed */
        node.exit();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    private void download(DataInputStream input) throws IOException {
        logger.clientLog("Downloading file chunk : "+chunkId+" of size : "+chunkSize);

        /* Read from the node input buffer  64kb */
        byte[] fileBytes = new byte[chunkSize];

        try {
            int bytesRead = input.read(fileBytes, 0, fileBytes.length);
            FileChunk fileChunk = new FileChunk(fileName, fileBytes, chunkSize, chunkId);
            fileChunkHashMap.put(chunkId, fileChunk);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
