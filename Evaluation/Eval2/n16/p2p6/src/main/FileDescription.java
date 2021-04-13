package main;

public class FileDescription {
    String fileName;
    String md5;
    int size;

    public FileDescription(String fileName, int size, String md5) {
        this.fileName = fileName;
        this.md5 = md5;
        this.size = size;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMd5() {
        return md5;
    }

    public int getSize() {
        return size;
    }
}
