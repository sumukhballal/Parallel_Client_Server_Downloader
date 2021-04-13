package main;

public class Config {

    private int indexingNodePort;
    private String indexingNodeIp;

    public Config(int indexingNodePort, String indexingNodeIp) {
        this.indexingNodePort = indexingNodePort;
        this.indexingNodeIp = indexingNodeIp;
    }


    public int getIndexingNodePort() {
        return indexingNodePort;
    }

    public String getIndexingNodeIp() {
        return indexingNodeIp;
    }

}
