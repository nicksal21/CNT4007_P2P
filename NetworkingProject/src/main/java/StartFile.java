// Bullock, Peltekis, & Salazar
// Imports
import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.util.Set;
import java.net.*;

public class StartFile {//https://www.youtube.com/watch?v=WeaB8pAGlDw&ab_channel=Thecodersbay
    public static void main(LinkedHashMap<Integer, String[]> peerInfo) throws Exception {
    byte [] b = new byte[16384];//Number of bits based off the Common.cfg
        for(int i = 1001;i<=1008; i++){//Going through peers
            if(Integer.parseInt(peerInfo.get(i)[2]) != 1) {
                Socket sc = new Socket(peerInfo.get(i)[0], Integer.parseInt(peerInfo.get(i)[1]));
                InputStream fI = sc.getInputStream();
                FileOutputStream fo = new FileOutputStream("java/project_config_file_small/project_config_file_small/"+peerInfo.get(i)[0]+"/thefile");
                fI.read(b,0,b.length);
                fo.write(b,0,b.length);
            }
            }
        }

    }

