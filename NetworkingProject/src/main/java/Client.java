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
    LinkedHashMap<String, String> cInfo;


    // Starts client socket and IO file streams
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket("192.168.1.100", port);
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
    public String sendMessage(String msg) throws IOException {
        //System.out.println(msg);
        String resp = "";

        PrintWriter pr = new PrintWriter(clientSocket.getOutputStream());
        pr.println(msg);
        pr.flush();

        in = clientSocket.getInputStream();
        InputStreamReader hanin = new InputStreamReader(in);
        BufferedReader handshake = new BufferedReader(hanin);
        resp = handshake.readLine();

        System.out.println(resp);
        return resp;
    }

    // Close file streams and client sockets
    public void stopConnection() throws IOException {
        Fin.close();
        Fout.close();
        clientSocket.close();
    }

    // Testing
    public void givenClient2_whenServerResponds_thenCorrect() throws IOException {
        Client client2 = new Client();
        client2.startConnection("10.228.5.78", 5555); // Contain the actual connection and handshake

        // Bit field
        String msg1 = client2.sendMessage("hello");
        String msg2 = client2.sendMessage("world");
        String terminate = client2.sendMessage(".");

        /*
         * Tests
         * assertEquals(msg1, "hello");
         * assertEquals(msg2, "world");
         * assertEquals(terminate, "bye");
         */
    }
}