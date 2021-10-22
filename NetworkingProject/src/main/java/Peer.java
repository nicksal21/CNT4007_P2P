package main.java;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.time.*;

public class Peer {
    private String hostName;
    private int peerID;
    private int listeningPort;
    private boolean hasFile;
    private boolean [] isChoked;
    private boolean [] isInterested;
    private byte [][] filePieces;
    private boolean wantToClose;
    private ServerSocket serverSocket;
    private Socket[] clientSockets;


    // This is the constructor of the class Peer
    public Peer(int key, LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo, ServerSocket severSocket, Socket[] clientSockets ) {
        //Sets all peer object variable to the info obtained from reading peerInfo.cfg and commonInfo.cfg
        hostName = peerInfo.get(key)[1];
        listeningPort = Integer.parseInt(peerInfo.get(key)[2]);
        hasFile = Integer.parseInt(peerInfo.get(key)[3])== 1;

        //Sets all arrays of isChoked and isInterested to flase
        for(int i = 0; i < peerInfo.size(); i++) {
            isChoked[i] = false;
            isInterested[i] = false;
        }

        //Sets all other Peer object variables
        wantToClose = false;
        this.serverSocket = severSocket;
        this.clientSockets = clientSockets;


        setFilePieces(commonInfo.get("FilesSize"), commonInfo.get("PieceSize"));
    }


    //*********************************** SET Functions ***********************************//
    // Method to choke or unchoke a peer
    /*
     * TODO:
     *    -Make this function apply to more than one peer
     *     use a list or map of some sort
     */
    public void setIsChoked (int peerID, boolean choked) {
        //PeerID -1001 is needed since Array Starts at 0
        isChoked[peerID-1001] = choked;
    }

    /*
     * Parameter(s):
     * fileSize- the file size as an integer
     * pieceSize - the size of a piece within the file as an integer
     *
     * Function:
     * Sets up the byte array with the correct number of columns using the number of pieces within a file
     */
    public void setFilePieces (int fileSize, int pieceSize ) {
        int numPieces;

        if (fileSize%pieceSize != 0)
            numPieces = fileSize/pieceSize + 1;
        else
            numPieces = fileSize/pieceSize;


        filePieces = new byte[numPieces][];
    }

    public void setClientSockets(Socket [] clientSockets){
        this.clientSockets = clientSockets;
    }


    public void setServerSockets(ServerSocket severSocket){
        this.serverSocket = severSocket;
    }

    //*********************************** GET Functions ***********************************//
    // Returns the array with all who is choked or not
    public boolean[] getChokedPeer(){
        return isChoked;
    }

    // Returns a boolean of if the peer wants to end it's connections
    public boolean getWantToClose(){
        return wantToClose;
    }

    // Returns if the peer has the complete file or not
    public boolean getHasFile(){
        return hasFile;
    }

    public byte[][] getFilePieces() {
        return filePieces;
    }

    // Returns the listing port of the peer
    public int getListeningPort() {
        return listeningPort;
    }

    // Returns the peer's ID number
    public int getPeerID() {
        return peerID;
    }

    // Returns all of the peer's sockets
    public Socket[] getClientSockets(){ return clientSockets;}

    // Returns all of the peer's sockets
    public ServerSocket getServerSocket(){ return serverSocket;}

    // Return Peers host name
    public String getHostName(){ return hostName;}



    //*********************************** Object Specific Functions ***********************************//
    /*
     * Parameters(s):
     * messageType - This will be a byte the dictates the message to be printed
     *
     * Function:
     * Takes in a message type and outputs the corresponding message
     */
    public void interpretMessage(int OtherPeerID, byte messageType)
    {
        switch(messageType) {
            case 0:
                // CHOKE - Set isChoked to true
                isChoked[peerID-1001] = true;
                writeLogMessage(peerID, OtherPeerID, null, 0, 0, 5);
                break;
            case 1:
                // UNCHOKE - Set isChoked to false
                isChoked[peerID-1001] = false;
                writeLogMessage(peerID, OtherPeerID, null, 0, 0, 4);
                break;
            case 2:
                // INTERESTED - Set isInterested to true
                isInterested[peerID-1001] = true;
                writeLogMessage(peerID, OtherPeerID, null, 0, 0, 7);
                break;
            case 3:
                // UNINTERESTED - Set isInterested to false
                isInterested[peerID-1001] = false;
                writeLogMessage(peerID, OtherPeerID, null, 0, 0, 8);
                break;
            case 4:
                // HAVE
                // TODO: IMPLEMENT HAVE
                System.out.println("Have");
                writeLogMessage(peerID, OtherPeerID, null, 0, 0, 6);
                break;
            case 5:
                // BITFIELD
                // TODO: IMPLEMENT BITFIELD
                System.out.println("Bitfield");
                break;
            case 6:
                // REQUEST
                // TODO: IMPLEMENT REQUEST
                System.out.println("Request");
                break;
            case 7:
                // PIECE
                // TODO: IMPLEMENT PIECE
                System.out.println("Piece");
                break;
        }
    }

    /*
    * writeLogMessage:
    * Appends a log message at the time of calling
    *
    * msgType guide:
    *   [0] - Peer1 makes a TCP connection to Peer2
    *   [1] - Peer2 makes a TCP connection to Peer1
    *   [2] - Peer1 makes a change of preferred neighbors
    *   [3] - Peer1 optimistically unchokes a neighbor
    *   [4] - Peer1 is unchoked by Peer2
    *   [5] - Peer1 is choked by a neighbor
    *   [6] - Peer1 receives a 'have' message from Peer2
    *   [7] - Peer1 receives an 'interested' message from Peer2
    *   [8] - Peer1 receives a 'not interested' message from Peer2
    *   [9] - Peer1 finishes downloading a piece from Peer2
    *   [10] - Peer1 has downloaded the complete file
    */
    public void writeLogMessage(int peer1ID, int peer2ID, int[] prefNeighbors, int pieceIndex, int numPieces, int msgType) {
        LocalTime time = LocalTime.now();
        try {
            String path = "/log_peer_" + peer1ID + ".log";
            File f1 = new File(path);
            FileWriter fileWriter = new FileWriter(f1.getName(),true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            String data = "";
            switch (msgType) {
                case 0:
                    data = time + ": Peer " + peer1ID + " makes a connection to Peer " + peer2ID + ".";
                    break;
                case 1:
                    data = time + ": Peer " + peer1ID + " is connected from Peer " + peer2ID + ".";
                    break;
                case 2:
                    data = time + ": Peer " + peer1ID + " has the preferred neighbors " + Arrays.toString(prefNeighbors) + ".";
                    break;
                case 3:
                    data = time + ": Peer " + peer1ID + " has optimistically unchoked neighbor " + peer2ID + ".";
                    break;
                case 4:
                    data = time + ": Peer " + peer1ID + " is unchoked by Peer " + peer2ID + ".";
                    break;
                case 5:
                    data = time + ": Peer " + peer1ID + " is choked by Peer " + peer2ID + ".";
                    break;
                case 6:
                    data = time + ": Peer " + peer1ID + " received the 'have' message from " + peer2ID + " for the piece " + pieceIndex + ".";
                    break;
                case 7:
                    data = time + ": Peer " + peer1ID + " received the 'interested' message from " + peer2ID + ".";
                    break;
                case 8:
                    data = time + ": Peer " + peer1ID + " received the 'not interested' message from " + peer2ID + ".";
                    break;
                case 9:
                    data = time + ": Peer " + peer1ID + " has downloaded the piece " + pieceIndex + " from " + peer2ID + ". Now the number of pieces it has is " + numPieces + ".";
                    break;
                case 10:
                    data = time + ": Peer " + peer1ID + " has downloaded the complete file.";
                    break;
                default:
                    System.err.println("Error! Incorrect message type code!");
                    break;
            }
            bw.write(data);
            bw.close();
            fileWriter.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    // Print the Peer details
    public void printPeerInfo() {
        System.out.println("*******Peer Information*******");
        System.out.println("Peer ID:" + peerID );
        System.out.println("Hostname:"+ hostName );
        System.out.println("The Listening Port:" + listeningPort );
        System.out.println("Has File:" + hasFile);
    }
}