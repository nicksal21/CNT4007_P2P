package main.java;

import java.io.*;
import java.util.*;
import java.net.*;

/*
StartFileClient is a file in which we can directly test the
StartFileSever. This function manually uses the specified socket
 */

public class StartFileClient {

    private Socket clientSocket;
    private FileInputStream in;
    private OutputStream out;
    LinkedHashMap<String, Integer> cInfo;

    /*

     */

    public void startConnection(Peer client, int server, LinkedHashMap<String, Integer> commonInfo) {
        cInfo = commonInfo;
        Socket [] socketArr = client.getClientSockets();
        //Maybe should be socket[] and set it for the peer
       clientSocket = socketArr[server];
        out = clientSocket.getOutputStream();
        in = new FileInputStream("java/project_config_file_small/project_config_file_small/" + client.getPeerID()+ "/thefile")

    }


    public void setSentPiece (Peer client, Peer[] peerList , int Piece) {
        int cid = client.getPeerID();
        int Peer0Id = peerList[0].getPeerID();
        byte [][] pieces = client.getPieces();
        if(pieces[cid - Peer0Id][Piece] != null) {
            pieceStart = commonInfo.get("PieceSize") * Piece;
        }

    }

    public String sendMessage(String msg) {
        out.println(msg);
        String resp = in.readLine();
        return resp;
    }

    public void stopConnection() {
        in.close();
        out.close();
        clientSocket.close();
    }


    }
}
