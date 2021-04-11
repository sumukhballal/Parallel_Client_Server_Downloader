package main;

public class FileChunk {
    String fileName;
    byte[] fileData;
    String chunkMd5;
    int chunkSize;
    int chunkId;

    public FileChunk(String fileName, byte[] fileData, int chunkSize, int chunkId) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.chunkSize = chunkSize;
        this.chunkId = chunkId;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public String getChunkMd5() {
        return chunkMd5;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public int getChunkId() {
        return chunkId;
    }
}
