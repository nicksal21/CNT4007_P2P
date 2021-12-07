package main.java;

// Imports

import java.io.*;
import java.net.Socket;

/*
 * Client is responsible for handling client behavior.
 * This function manually uses the specified socket
 * to send test messages to the Server Socket that is specified.
 */
public class Client extends Thread {

    //Class Variable
    private Socket clientSocket;
    // owo
    private InputStream in;
    private OutputStream out;

    Client(Peer p) {

    }


    // Starts client socket and IO file streams
    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket("10.2.0.224", port);
        // clientSocket = new Socket("10.140.109.23", port);
        // clientSocket = new Socket("10.136.178.29", port);
        out = clientSocket.getOutputStream();
        in = clientSocket.getInputStream();
    }


    // Send a message
    public void sendRequest(byte[] req) throws IOException {

        if (req.length < 0)
            throw new IllegalArgumentException("Negative length not allowed");
        OutputStream out = clientSocket.getOutputStream();
        DataOutputStream sndReq = new DataOutputStream(out);
        sndReq.writeInt(req.length);
        sndReq.write(req, 0, req.length);

    }

    // Close file streams and client sockets
    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }


}