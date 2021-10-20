package main.java;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.util.Set;
import java.net.*;

public class StartFileClient {
    public static void main(LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo) throws Exception{
        for(int i = 1001;i<=1008; i++) {
            if(peerInfo.get(i)[2] == "1") {
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(peerInfo.get(i)[2]));
                Socket sr = serverSocket.accept();
                FileInputStream tf = new FileInputStream("java/project_config_file_small/project_config_file_small/1001/thefile");
                byte []b = new byte[commonInfo.get("PieceSize")];
                tf.read(b, 0, b.length);

                OutputStream Os = sr.getOutputStream();
                Os.write(b,0,b.length);
            }
        }
    }
}
