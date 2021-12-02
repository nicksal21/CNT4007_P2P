package main.java;

// Imports

import java.io.*;
import java.util.*;
import java.net.*;

/*
 * Client is responsible for handling client behavior.
 * This function manually uses the specified socket
 * to send test messages to the Server Socket that is specified.
 */
public class Client extends Thread{

    //Class Variable
    private Socket clientSocket;
    private Peer Pier; // owo
    private FileInputStream Fin;
    private OutputStream Fout;
    private InputStream in;
    private OutputStream out;
    private int PieceStart;
    private int ServerId;
    LinkedHashMap<String, String> cInfo;

    Client(Peer p){

        Pier = p;

    }


    // Starts client socket and IO file streams
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket("10.3.0.169", port);
        //clientSocket = new Socket("10.136.2.24", port);
        out = clientSocket.getOutputStream();
        in = clientSocket.getInputStream();
    }

    // Begins file connection
    public void startClient(int key, String ip, int port,
                            LinkedHashMap<String, String > commonInfo) throws IOException {
        cInfo = commonInfo;
        Fout = clientSocket.getOutputStream();
        /*if(Pier.getHasFile()){

        }*/
        //in = new FileInputStream("java/project_config_file_small/project_config_file_small/" + key + "/thefile");
    }


    // Sets piece to be sent
    public void setSentPiece(Peer client, Peer[] peerList, int Piece) {
        int cid = client.getPeerID();
        int Peer0Id = peerList[0].getPeerID();
        byte[][] pieces = client.getFilePieces();

        if (pieces[cid - Peer0Id][Piece] != 0) {
            PieceStart = Integer.parseInt(cInfo.get("PieceSize")) * Piece;
        }
    }

    // Send a message
    public String handMessage(String msg) throws IOException {
        //System.out.println(msg);
        String resp = "";

        PrintWriter pr = new PrintWriter(clientSocket.getOutputStream());
        pr.println(msg);
        pr.flush();

        in = clientSocket.getInputStream();
        InputStreamReader hanin = new InputStreamReader(in);
        BufferedReader handshake = new BufferedReader(hanin);
        resp = handshake.readLine();
        if(!Objects.equals(resp, "P2PFILESHARINGPROJ0000000000"+Pier.getPeerID()))
            System.out.println(resp);
        ServerId = Integer.parseInt(resp.substring(28,32));
        return resp;
    }

    // Send a message
    public void sendRequest(byte[] req) throws IOException {

        if (req.length < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        OutputStream out = clientSocket.getOutputStream();
        DataOutputStream sndReq = new DataOutputStream(out);
        sndReq.writeInt(req.length);
        sndReq.write(req,0,req.length);

    }

    // Close file streams and client sockets
    public void stopConnection() throws IOException {
        Fin.close();
        Fout.close();
        clientSocket.close();
    }


}