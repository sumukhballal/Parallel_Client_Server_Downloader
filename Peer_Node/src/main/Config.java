package main;

public class Config {
    private String id;
    private int peerNodePort;
    private int indexingNodePort;
    private String indexingNodeIp;
    private String peerNodeIp;
    private String hostFilePath;

    public Config(int peerNodePort,  String peerNodeIp, int indexingNodePort, String indexingNodeIp, String hostFilePath) {
        this.peerNodePort = peerNodePort;
        this.indexingNodePort = indexingNodePort;
        this.indexingNodeIp = indexingNodeIp;
        this.peerNodeIp = peerNodeIp;
        this.hostFilePath = hostFilePath;
        this.id=peerNodeIp+":"+peerNodePort;
    }


    public String getId() {
        return id;
    }

    public int getPeerNodePort() {
        return peerNodePort;
    }

    public int getIndexingNodePort() {
        return indexingNodePort;
    }

    public String getIndexingNodeIp() {
        return indexingNodeIp;
    }

    public String getPeerNodeIp() {
        return peerNodeIp;
    }

    public String getHostFilePath() {
        return hostFilePath;
    }
}
