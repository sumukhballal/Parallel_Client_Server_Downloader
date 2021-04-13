package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Logger {

//    FileWriter serverFileWriter;
//    FileWriter clientFileWriter;
//    BufferedWriter bufferedServerWriter;

    String serverFile;
    String clientFile;
    int mode=0;

    Logger(String serverFile, String clientFile, int mode) {
        this.serverFile = serverFile;
        this.clientFile = clientFile;
        this.mode=mode;
    }

    public void serverLog(String data) {

        if(mode==0)
            System.out.println(data);

        try {

            FileWriter serverFileWriter=new FileWriter(serverFile, true);
            serverFileWriter.write(data);
            serverFileWriter.write(System.lineSeparator());
            serverFileWriter.flush();
            serverFileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientLog(String data) {
        if(mode==0)
            System.out.println(data);
        try {

            FileWriter clientFileWriter=new FileWriter(clientFile, true);
            clientFileWriter.write(data);
            clientFileWriter.write(System.lineSeparator());
            clientFileWriter.flush();
            clientFileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
