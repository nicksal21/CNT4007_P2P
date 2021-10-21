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

    private List<ServerSocket> serverSockets;
    private ServerSocket current;
    private LinkedHashMap<Integer, String[]>pInfo;
    private LinkedHashMap<String, Integer> cInfo;

    /*
    Start uses the information provided by peer object and commonInfo to create
    the server sockets.
    */

    public void start(LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo) {
        pInfo = peerInfo;
        cInfo = commonInfo;
       //Keys will allow us to keep track of the size and location of the peers
        List<Integer> keys = new ArrayList<Integer>(peerInfo.keySet());

        // While loop will create each of the Server Sockets for the peers
        int i = keys.get(0);
        while (i < keys.get(keys.size() - 1)) {
            try { // Try to create a server socket; if not, throw an IOException
                // Create a
                serverSockets.add(new ServerSocket(Integer.parseInt(peerInfo.get(i)[0]), Integer.parseInt(peerInfo.get(i)[1])));
                current = serverSockets.get(i);
                //EchoClientHander is a function below
                new EchoClientHandler(current.accept(), peerInfo.get(i)[0]).start();
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

    /*
    The EchoClientHandler will set the client socket that will currently be running.
    The EchoClientHander contains the run peer method and
     */
    private class EchoClientHandler extends Thread {
        private Socket clientSocket;
        private FileOutputStream out;
        private InputStream in;

        public EchoClientHandler(Socket socket, String pname) throws IOException {
            this.clientSocket = socket;
            out = new FileOutputStream("java/project_config_file_small/project_config_file_small/" + pname+ "/thefile");
            in = clientSocket.getInputStream();
        }

        public void exit(Socket clientSocket, InputStream in, FileOutputStream out) {

            try {
                in.close();
                out.close();
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void run(Peer p) {
            try {

                // Number of bits based off the Common.cfg
                byte[] b = new byte[cInfo.get("PieceSize")];

                /*
                Check if client peer is unchoked;
                I.e., Client Peer is able to snd/rcv data
                 */
                while(!p.getChokedPeer()[p.getPeerID() - 1001]) {
                    //Reads the input stream
                    in.read(b, 0, b.length);
                    //Writes the File output stream
                    out.write(b, 0, b.length);
                }

                // Checks to see if the peer wants to close its connections
                if(p.getWantToClose()) {
                    exit(clientSocket,in, out);
                }
            }
            /*
            Need try catch to in order to handle the file stream
             */
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}



