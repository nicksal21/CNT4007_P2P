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
    private int PieceStart;
    LinkedHashMap<String, Integer> cInfo;

    /*

     */

    public void startConnection(Peer client, int server, LinkedHashMap<String, Integer> commonInfo) throws IOException {
        cInfo = commonInfo;
        Socket [] socketArr = client.getClientSockets();
        //Maybe should be socket[] and set it for the peer
       clientSocket = socketArr[server];
        out = clientSocket.getOutputStream();
        in = new FileInputStream("java/project_config_file_small/project_config_file_small/" + client.getPeerID()+ "/thefile");

    }


    public void setSentPiece (Peer client, Peer[] peerList , int Piece) {
        int cid = client.getPeerID();
        int Peer0Id = peerList[0].getPeerID();
        byte [][] pieces = client.getFilePieces();
        if(pieces[cid - Peer0Id][Piece] != 0) {
            PieceStart = cInfo.get("PieceSize") * Piece;
        }

    }

    public String sendMessage(String msg) throws IOException{
        System.out.println(msg);
        String resp = String.valueOf(in.read());
        return resp;
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


}

