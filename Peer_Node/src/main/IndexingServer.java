package main;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class IndexingServer {

    String id;
    int port;
    String ip;
    Socket socket;
    DataOutputStream dataOutputStream;
    DataInputStream dataInputStream;

    public IndexingServer(String ip, int port, Socket socket, DataOutputStream dataOutputStream, DataInputStream dataInputStream) {
        this.id=ip+":"+port;
        this.ip=ip;
        this.port=port;
        this.socket = socket;
        this.dataOutputStream = dataOutputStream;
        this.dataInputStream = dataInputStream;
    }

    public String getId() {
        return id;
    }

    public int getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public Socket getSocket() {
        return socket;
    }

    public DataOutputStream getDataOutputStream() {
        return dataOutputStream;
    }

    public DataInputStream getDataInputStream() {
        return dataInputStream;
    }

    public void exit() {
        try {

            dataOutputStream.close();
            dataInputStream.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class FileChunk {

        String fileName;
        int chunkId;
        int size;
        String md5Checksum;

        public FileChunk(String fileName, int chunkId, int size, String md5Checksum) {
            this.fileName = fileName;
            this.chunkId = chunkId;
            this.size = size;
            this.md5Checksum = md5Checksum;
        }

        public String getFileName() {
            return fileName;
        }

        public int getChunkId() {
            return chunkId;
        }

        public int getSize() {
            return size;
        }

        public String getMd5Checksum() {
            return md5Checksum;
        }
    }
}
