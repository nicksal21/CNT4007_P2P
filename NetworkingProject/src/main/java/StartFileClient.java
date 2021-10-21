package main.java;

import java.io.*;
import java.util.*;
import java.net.*;

public class StartFileClient {

    StartFileClient(LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo) throws Exception{
        //Number of bits based off the Common.cfg
        byte [] b = new byte[commonInfo.get("PieceSize")];
        //Get the int keys for the for loop
        List<Integer> keys = new ArrayList<Integer>(peerInfo.keySet());
        //Make a socket List in order to keep track of each of the client sockets
        List<Socket> ClientSockets = null;
        /*
        For Loop runs through each of the peers and creates sockets for each of the peers
        and adds them to the client socket list. Then Loop uses ClientSockets to make an InputStream
        to send information to servers.
        For now this loop will make 1 client socket for every peer.
        We will make more peer client sockets after ensuring we properly established a connection
        between peers and are able to send over information properly.
        All of the port socket locations are the same for each of the peers.
         */
        for(int i = keys.get(0);i<=keys.get(keys.size()-1); i++){
                ClientSockets.add(new Socket(peerInfo.get(i)[0], Integer.parseInt(peerInfo.get(i)[1])));//Make client socket
                //Setting Up IN and Out Streams for Connection
                FileInputStream fI = new FileInputStream("java/project_config_file_small/project_config_file_small/" + peerInfo.get(i)[0]+ "/thefile");
                OutputStream fO = ClientSockets.get(i-keys.get(0)).getOutputStream();
                fI.read(b,0,b.length);
                fO.write(b);

        }
    }
}
