package main.java;

// Bullock, Peltekis, & Salazar

// Imports

import java.io.*;
import java.util.*;
import java.net.*;


/*
 * This class is has-a relationship with a peer ie a peer has severs.
 * Peers act as both client as servers. Peer hosts will receive information
 * from each of the clients through the InputStream and write to the OutputFile.
 */
public class Server extends Thread { // https://www.baeldung.com/a-guide-to-java-sockets

    //Class Variable
    private ServerSocket current;
    private LinkedHashMap<Integer, String[]> pInfo;
    private LinkedHashMap<String, String> cInfo;
    public Peer sPeer;

    /*
     * Start uses the information provided by peer object and commonInfo to connect the socekets
     * to each of the severs. In order to allow the server socket to handle multiple connection
     * you need to thread all the TCP connections.
     * https://www.youtube.com/watch?v=ZIzoesrHHQo&ab_channel=DavidDobervich
     * The YouTube link above discusses how to make this connections
     */

    public void startServer(int key, int port, LinkedHashMap<Integer, String[]> peerInfo,
                            LinkedHashMap<String, String> commonInfo, Peer serverP) throws IOException {

        pInfo = peerInfo;
        cInfo = commonInfo;
        ServerSocket server = new ServerSocket(port);
        sPeer = serverP;

        while (true) {
            current = server;
            new EchoClientHandler(current.accept(), key, sPeer).start();
        }

    }

    // Close the server
    public void stopServer() throws IOException {
        current.close();
    }

    /*
     * The EchoClientHandler will set the client socket that will currently be running.
     * The EchoClientHandler contains the run peer method and
     */
    private class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private FileOutputStream out;
        private InputStream in;
        public Peer ServerPeer;
        private int pieceStart = 0;
        private int Clientkey;
        private int ServerKey;
        boolean handshake;

        // Constructor
        public EchoClientHandler(Socket socket, int k, Peer serverP) throws IOException {
            this.clientSocket = socket;
            ServerKey = k;
            ServerPeer = serverP;
        }

        // Close all streams and sockets on peer exit
        public void exit() {

            try {
                in.close();
                out.close();
                this.clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        /*
         * This function will set the offset for the file InputStream.
         * This function will check if the Peer has the piece that the
         * host wants. If not it will send the piece it does have
         */

        public void setPiece(int Piece, LinkedHashMap<String, Integer> commonInfo) {
            // Number of bytes based off the Common.cfg
            pieceStart = commonInfo.get("PieceSize") * Piece;

        }

        // Run peer
        public void run(Peer p, int bytWanted) {
            try {
                // Input and Output Streams
                InputStream in = clientSocket.getInputStream();
                // FileOutputStream out = new FileOutputStream("java/project_config_file_small/project_config_file_small/" + p.getPeerID() + "/thefile");

                /*
                 * Check if client peer is unchoked;
                 * I.e., Client Peer is able to transmit or receive data
                 */
                while (!p.getChokedPeer()[p.getPeerID() - 1001]) {
                    // Reads the input stream
                    //in.read(b, 0, b.length);
                    // TODO: Use "b" so peer doesn't get stuck reading/writing from same location
                    // Writes the File output stream
                    //out.write(b, pieceStart, b.length);
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

        @Override
        public void run() {

            try {
                OutputStream outMsg = clientSocket.getOutputStream();
                in = clientSocket.getInputStream();

                InputStreamReader inMsg = new InputStreamReader(in);
                BufferedReader MsgRead = new BufferedReader(inMsg);
                String handshakeMsg = "";
                boolean hand = false;

                while (!hand) {
                    //boolean checkCond = !Objects.equals(handshakeMsg, "P2PFILESHARINGPROJ0000000000"+key);
                    handshakeMsg = MsgRead.readLine();
                    if (!handshakeMsg.isEmpty())
                        hand = Objects.equals(handshakeMsg.substring(0, 28), "P2PFILESHARINGPROJ0000000000");
                }

                Clientkey = Integer.parseInt(handshakeMsg.substring(28, 32));
                handshakeMsg = handshakeMsg.substring(0, 28) + ServerKey;
                PrintWriter pr = new PrintWriter(outMsg);
                pr.println(handshakeMsg);
                pr.flush();
                DataInputStream sentReq = new DataInputStream(in);
                int cReqLength = sentReq.readInt();
                byte[] MsgReq = new byte[cReqLength];
                sentReq.readFully(MsgReq);

                while (true) {

                    ServerPeer.interpretMessage(Clientkey, MsgReq);
                    cReqLength = sentReq.readInt();
                    MsgReq = new byte[cReqLength];
                    sentReq.readFully(MsgReq);

                    


                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }
}

