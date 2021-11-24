package main.java;

// Imports

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;

/*
 * Peer Object
 * Contains all the relevant information that is needed to
 * manage the Peer-2-Peer network. This object contains each Peer's
 * Server and Client sockets, the Peer ID, Hostname, and the pieces of
 * theFile that the peer contains.
 * The peer will also be responsible for handling and executing all the messages
 */

public class Peer {

    //Class Variables
    private String hostName;
    private int peerID;
    private int listeningPort;
    private boolean hasFile;
    private boolean[] isChoked;
    private boolean[] isInterested;
    private byte[][] filePieces;
    private boolean wantToClose;
    private Server server;
    private Client[] clients;
    private LinkedHashMap<Integer, Byte[]> peerBitfields;


    // This is the constructor of the class Peer
    public Peer(int key, LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String,
            Integer> commonInfo, Server server, Client[] clients) {
        //Sets all peer object variable to the info obtained from reading peerInfo.cfg and commonInfo.cfg
        hostName = peerInfo.get(key)[1];
        listeningPort = Integer.parseInt(peerInfo.get(key)[2]);
        hasFile = Integer.parseInt(peerInfo.get(key)[3]) == 1;

        //Sets all arrays of isChoked and isInterested to false
        for (int i = 0; i < peerInfo.size(); i++) {
            isChoked[i] = false;
            isInterested[i] = false;
        }

        //Sets all other Peer object variables
        wantToClose = false;
        this.server = server;
        this.clients = clients;


        setFilePieces(commonInfo.get("FilesSize"), commonInfo.get("PieceSize"));
    }


    //*********************************** SET Functions ***********************************//
    // Method to choke or unchoke a peer
    /*
     * TODO:
     *    -Make this function apply to more than one peer
     *     use a list or map of some sort
     */
    public void setIsChoked(int peerID, boolean choked) {
        //PeerID -1001 is needed since Array Starts at 0
        isChoked[peerID - 1001] = choked;
    }

    /*
     * Parameter(s):
     * fileSize- the file size as an integer
     * pieceSize - the size of a piece within the file as an integer
     *
     * Function:
     * Sets up the byte array with the correct number of columns using the number of pieces within a file
     */
    public void setFilePieces(int fileSize, int pieceSize) {
        int numPieces;

        if (fileSize % pieceSize != 0)
            numPieces = fileSize / pieceSize + 1;
        else
            numPieces = fileSize / pieceSize;


        filePieces = new byte[numPieces][];
    }

    // Sets Client Sockets
    public void setClientSockets(Client[] clients) {
        this.clients = clients;
    }

    // Sets Server Sockets
    public void setServerSockets(Server server) {
        this.server = server;
    }

    //*********************************** GET Functions ***********************************//
    // Returns the array with all who is choked or not
    public boolean[] getChokedPeer() {
        return isChoked;
    }

    // Returns a boolean for if the peer wants to end its connections
    public boolean getWantToClose() {
        return wantToClose;
    }

    // Returns if the peer has the complete file or not
    public boolean getHasFile() {
        return hasFile;
    }

    // Get pieces of file
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

    // Returns all the peer's client sockets
    public Client[] getClients() {
        return clients;
    }

    // Returns all the peer's server sockets
    public Server getServer() {
        return server;
    }

    // Return Peers host name
    public String getHostName() {
        return hostName;
    }


    //*********************************** Object Specific Functions ***********************************//
    /*
     * Parameters(s):
     * messageType - This will be a byte the dictates the message to be printed
     *
     * Function:
     * Takes in a message type and outputs the corresponding message
     */
    public void interpretMessage(Peer OtherPeer, byte messageType) {
        switch (messageType) {
            case 0:
                // CHOKE - Set isChoked to true
                isChoked[peerID - 1001] = true;
                writeLogMessage(OtherPeer, null, 0, 0, 5);
                break;
            case 1:
                // UNCHOKE - Set isChoked to false
                isChoked[peerID - 1001] = false;
                writeLogMessage(OtherPeer, null, 0, 0, 4);
                break;
            case 2:
                // INTERESTED - Set isInterested to true
                isInterested[peerID - 1001] = true;
                writeLogMessage(OtherPeer, null, 0, 0, 7);
                break;
            case 3:
                // UNINTERESTED - Set isInterested to false
                isInterested[peerID - 1001] = false;
                writeLogMessage(OtherPeer, null, 0, 0, 8);
                break;
            case 4:
                // HAVE
                // TODO: IMPLEMENT HAVE
                System.out.println("Have");
                writeLogMessage(OtherPeer, null, 0, 0, 6);
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
    public void writeLogMessage(Peer Peer2, int[] prefNeighbors, int pieceIndex, int numPieces, int msgType) {
        try {
            int peer2ID = Peer2.peerID;
            String path = "/log_peer_" + peer2ID + ".log";
            File f1 = new File(path);
            FileWriter fileWriter = new FileWriter(f1.getName(), true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            String data = "";
            LocalTime time = LocalTime.now();
            switch (msgType) {
                case 0:
                    data = time + ": Peer " + peerID + " makes a connection to Peer " + peer2ID + ".";
                    break;
                case 1:
                    data = time + ": Peer " + peerID + " is connected from Peer " + peer2ID + ".";
                    break;
                case 2:
                    data = time + ": Peer " + peerID + " has the preferred neighbors " + Arrays.toString(prefNeighbors) + ".";
                    break;
                case 3:
                    data = time + ": Peer " + peerID + " has optimistically unchoked neighbor " + peer2ID + ".";
                    break;
                case 4:
                    data = time + ": Peer " + peerID + " is unchoked by Peer " + peer2ID + ".";
                    break;
                case 5:
                    data = time + ": Peer " + peerID + " is choked by Peer " + peer2ID + ".";
                    break;
                case 6:
                    data = time + ": Peer " + peerID + " received the 'have' message from " + peer2ID + " for the piece " + pieceIndex + ".";
                    break;
                case 7:
                    data = time + ": Peer " + peerID + " received the 'interested' message from " + peer2ID + ".";
                    break;
                case 8:
                    data = time + ": Peer " + peerID + " received the 'not interested' message from " + peer2ID + ".";
                    break;
                case 9:
                    data = time + ": Peer " + peerID + " has downloaded the piece " + pieceIndex + " from " + peer2ID + ". Now the number of pieces it has is " + numPieces + ".";
                    break;
                case 10:
                    data = time + ": Peer " + peerID + " has downloaded the complete file.";
                    break;
                default:
                    System.err.println("Error! Incorrect message type code!");
                    break;
            }
            bw.write(data);
            bw.close();
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Print the Peer details
    public void printPeerInfo() {
        System.out.println("*******Peer Information*******");
        System.out.println("Peer ID:" + peerID);
        System.out.println("Hostname:" + hostName);
        System.out.println("The Listening Port:" + listeningPort);
        System.out.println("Has File:" + hasFile);
    }
}