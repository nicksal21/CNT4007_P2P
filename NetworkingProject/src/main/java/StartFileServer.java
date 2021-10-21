package main.java;// Bullock, Peltekis, & Salazar

// Imports

import java.io.*;
import java.util.*;
import java.net.*;


/*
 * This class is a "has-a" relationship with a peer;
 * I.e., A peer has a server.
 * The peer will hold
 */
public class StartFileServer { // https://www.baeldung.com/a-guide-to-java-sockets
    private List<ServerSocket> serverSockets;
    private ServerSocket current;
    private LinkedHashMap<Integer, String[]> pInfo;
    private LinkedHashMap<String, Integer> cInfo;

    // Start peer
    public void start(LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo) {
        pInfo = peerInfo;
        cInfo = commonInfo;

        // Keys will allow us to keep track of the size and location of the peers
        List<Integer> keys = new ArrayList<Integer>(peerInfo.keySet());

        // While loop will create each of the Server Sockets for the peers
        int i = keys.get(0);
        while (i < keys.get(keys.size() - 1)) {
            try {
                // Create a server socket or throw an IOException
                serverSockets.add(new ServerSocket(Integer.parseInt(peerInfo.get(i)[0]), Integer.parseInt(peerInfo.get(i)[1])));
                current = serverSockets.get(i);
                new EchoClientHandler(current.accept()).start();
                i++;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    // Close the server
    public void stop() throws IOException {
        current.close();
    }

    // Client Handler
    private class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private FileOutputStream out;
        private InputStream in;

        // Constructor
        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        // Close all streams and sockets on peer exit
        public void exit(Socket clientSocket, InputStream in, FileOutputStream out) {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // Run peer
        public void run(Peer p) {
            try {
                // Input and Output Streams
                InputStream in = clientSocket.getInputStream();
                FileOutputStream out = new FileOutputStream("java/project_config_file_small/project_config_file_small/" + pInfo.get(p.getPeerID())[0] + "/thefile");

                // Number of bits based off the Common.cfg
                byte[] b = new byte[cInfo.get("PieceSize")];

                /*
                 * Check if client peer is unchoked;
                 * I.e., Client Peer is able to transmit or receive data
                 */
                while (!p.getChokedPeer()[p.getPeerID() - 1001]) {
                    // Reads the input stream
                    in.read(b, 0, b.length);
                    // TODO: Use "b" so peer doesn't get stuck reading/writing from same location
                    // Writes the File output stream
                    out.write(b, 0, b.length);
                }

                // Checks to see if the peer wants to close its connections
                if (p.getWantToClose()) {
                    exit(clientSocket, in, out);
                }
            }
            // Handle errors
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



