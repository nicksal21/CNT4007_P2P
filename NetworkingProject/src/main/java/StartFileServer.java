package main.java;// Bullock, Peltekis, & Salazar

// Imports

import java.io.*;
import java.util.*;
import java.net.*;


/*
This class is has-a relationship with a peer ie a peer has severs.
Peers act as both client as servers. Peer hosts will recieve information
from each of the clients through the InputStream and write to the OutputFile.
 */
public class StartFileServer { // https://www.baeldung.com/a-guide-to-java-sockets
    private ServerSocket current;
    private LinkedHashMap<Integer, String[]> pInfo;
    private LinkedHashMap<String, Integer> cInfo;

    /*
    Start uses the information provided by peer object and commonInfo to connect the socekets
    to each of the severs. In order to allow the server socket to handle multiple connection
    you need to thread all the TCP connections.
    https://www.youtube.com/watch?v=ZIzoesrHHQo&ab_channel=DavidDobervich
    The Youtube link above discussess how to make this connections
    */

    public void start(Peer[] Peers, LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo) {
        pInfo = peerInfo;
        cInfo = commonInfo;
        while (true)
            for(int i = 0, i<= Peers.length; i++){
                current = Peers[i].getServerSocket();
                new EchoClientHandler(current.accept()).start();
    }

    }

    // Close the server
    public void stop() throws IOException {
        current.close();
    }

    /*
    The EchoClientHandler will set the client socket that will currently be running.
    The EchoClientHander contains the run peer method and
     */
    private class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private FileOutputStream out;
        private InputStream in;
        private byte [] b;
        private int pieceStart = 0;

        // Constructor
        public EchoClientHandler(Socket socket, Peer target) throws IOException{
            this.clientSocket = socket;
            out = new FileOutputStream("java/project_config_file_small/project_config_file_small/" + target.getPeerID()+ "/thefile");
            in = clientSocket.getInputStream();
        }

        // Close all streams and sockets on peer exit
        public void exit() {

            try {
                in.close();
                out.close();
                this.clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        /*
    This function will set the offset for the file InputStream.
    This function will check if the Peer has the piece that the
    host wants. If not it will send the piece it does have
     */

        public void setPiece(int Piece, LinkedHashMap<String, Integer> commonInfo) {

            // Number of bytes based off the Common.cfg
            pieceStart = commonInfo.get("PieceSize") * Piece;

            return;
        }

        // Run peer
        public void run(Peer p, int bytWanted) {
            try {
                // Input and Output Streams
                InputStream in = clientSocket.getInputStream();
                FileOutputStream out = new FileOutputStream("java/project_config_file_small/project_config_file_small/" + p.getPeerID() + "/thefile");


                /*
                 * Check if client peer is unchoked;
                 * I.e., Client Peer is able to transmit or receive data
                 */
                while (!p.getChokedPeer()[p.getPeerID() - 1001]) {
                    // Reads the input stream
                    in.read(b, 0, b.length);
                    // TODO: Use "b" so peer doesn't get stuck reading/writing from same location
                    // Writes the File output stream
                    out.write(b, pieceStart, b.length);
                }

                // Checks to see if the peer wants to close its connections
                if (p.getWantToClose()) {
                    exit();
                }
            }
            // Handle errors
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

