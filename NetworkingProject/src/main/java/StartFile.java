package main.java;// Bullock, Peltekis, & Salazar
// Imports

import java.io.*;
import java.util.*;
import java.net.*;

public class StartFile { // https://www.baeldung.com/a-guide-to-java-sockets

    private List<ServerSocket> serverSockets;
    private ServerSocket current;
    LinkedHashMap<Integer, String[]>pInfo;
    LinkedHashMap<String, Integer> cInfo;

    public void start(LinkedHashMap<Integer, String[]>peerInfo, LinkedHashMap<String, Integer> commonInfo) {
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

        public EchoClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run(int s) {
            try {
                out = new FileOutputStream("java/project_config_file_small/project_config_file_small/" + pInfo.get(s)[0] + "/thefile");
                in = clientSocket.getInputStream();


                byte[] b = new byte[cInfo.get("PieceSize")]; // Number of bits based off the Common.cfg

                in.read(b, 0, b.length);
                out.write(b, 0, b.length);

                in.close();
                out.close();
                clientSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}




