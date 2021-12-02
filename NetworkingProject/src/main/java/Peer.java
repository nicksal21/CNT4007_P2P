package main.java;

// Imports

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.BitSet;

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
    private boolean[] hasPieces;
    private byte[][] filePieces;
    private boolean wantToClose;
    private Server server;
    private Client[] clients;
    private FileConverter fc;
    private LinkedHashMap<Integer, Byte[]> peerBitfields;
    int PieceSize;
    int fileSize;


    // This is the constructor of the class Peer
    public Peer(int key, LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String,
            String> commonInfo, Server server, Client[] clients) throws IOException {
        //Sets all peer object variable to the info obtained from reading peerInfo.cfg and commonInfo.cfg
        hostName = peerInfo.get(key)[0];
        listeningPort = Integer.parseInt(peerInfo.get(key)[1]);
        hasFile = Integer.parseInt(peerInfo.get(key)[2]) == 1;
        peerID = key;

        //Sets all arrays of isChoked and isInterested to false
        isChoked = new boolean[peerInfo.size()];
        isInterested = new boolean[peerInfo.size()];

        for (int i = 0; i < peerInfo.size(); i++) {
            isChoked[i] = false;
            isInterested[i] = false;
        }

        //Sets all other Peer object variables
        wantToClose = false;
        this.server = server;
        this.clients = clients;


        setFilePieces(commonInfo, hasFile, key);
    }


    //*********************************** SET Functions ***********************************//
    // Method to choke or unchoke a peer
    /*
     * TODO:
     *    -Make this function apply to more than one peer
     *     use a list or map of some sort
     */
    public synchronized void setIsChoked(int peerID, boolean choked) {
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
    public synchronized void setFilePieces(LinkedHashMap<String,
            String> commonInfo, boolean hasFile, int key) throws IOException {

        PieceSize = Integer.parseInt(commonInfo.get("PieceSize"));
        fileSize = Integer.parseInt(commonInfo.get("FileSize"));
        hasPieces = new boolean[(int) Math.ceil((double) fileSize / PieceSize)];

        Arrays.fill(hasPieces, false);

        if (hasFile) {
            //filePieces = fc.fileToByte("src/main/java/project_config_file_small/project_config_file_small/"+key+"/thefile", commonInfo);
            filePieces = fc.fileToByte(new File("NetworkingProject\\src\\main\\java\\project_config_file_small\\project_config_file_small\\" + key + "\\thefile").getCanonicalPath(), commonInfo);
            for (int i = 0; i < (int) Math.ceil((double) fileSize / PieceSize); i++) {
                hasPieces[i] = true;
            }

        } else
            filePieces = new byte[(int) Math.ceil((double) fileSize / PieceSize)][PieceSize];
    }

    //This function is responcible for setting the individual pieces when recieved
    public synchronized void setPiece(int pieceNum, byte[] piece) throws IOException {

        hasPieces[pieceNum] = true;
        filePieces[pieceNum] = piece;

    }


    // Sets Client Sockets
    public synchronized void setClientSockets(Client[] clients) {
        this.clients = clients;
    }

    // Sets Server Sockets
    public synchronized void setServerSockets(Server server) {
        this.server = server;
    }

    //*********************************** GET Functions ***********************************//
    // Returns the array with all who is choked or not
    public synchronized boolean[] getChokedPeer() {
        return isChoked;
    }

    // Returns a boolean for if the peer wants to end its connections
    public synchronized boolean getWantToClose() {
        return wantToClose;
    }

    // Returns if the peer has the complete file or not
    public synchronized boolean getHasFile() {
        return hasFile;
    }

    // Get pieces of file
    public synchronized byte[][] getFilePieces() {
        return filePieces;
    }

    public synchronized byte[] getBitFieldMessage() {
        int b = (int) Math.ceil(Math.log((double) fileSize / PieceSize) / Math.log(2));
        BitSet bitSet = new BitSet((int) Math.pow(2,b));
        for (int i = 0; i < hasPieces.length; i++)
            if (hasPieces[i]) {
                //bitfield[(int) Math.floor((double) i / 8)] |= 1 << (7 - i % 8);
                bitSet.set((int) Math.pow(2,b)-i);
            }
        byte[] bitfield = bitSet.toByteArray();

        byte msgT = (byte) 5;
        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
        lengthBuffer.putInt(1 + bitfield.length);
        byte[] msgL = lengthBuffer.array();

        byte[] bfmsg = new byte[5 + bitfield.length];
        for (int i = 0; i < bfmsg.length; i++) {
            if (i < 4) {
                bfmsg[i] = msgL[i];
            } else if (i == 4) {
                bfmsg[i] = msgT;
            } else {
                bfmsg[i] = bitfield[i - 5];
            }
        }
        return bfmsg;

    }

    // Returns the listing port of the peer
    public synchronized int getListeningPort() {
        return listeningPort;
    }

    // Returns the peer's ID number
    public synchronized int getPeerID() {
        return peerID;
    }

    // Returns all the peer's client sockets
    public synchronized Client[] getClients() {
        return clients;
    }

    // Returns all the peer's server sockets
    public synchronized Server getServer() {
        return server;
    }

    // Return Peers host name
    public synchronized String getHostName() {
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
    public synchronized void interpretMessage(int OtherPeer, byte[] message) {
        byte messageType = message[4];
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
                getBitFieldMessage();
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

    public synchronized byte[] generateMessage() {
        return null;
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
    public synchronized void writeLogMessage(int Peer2, int[] prefNeighbors, int pieceIndex, int numPieces, int msgType) {
        try {
            int peer2ID = Peer2;
            String path = "/log_peer_" + peer2ID + ".log";
            File f1 = new File(path);
            FileWriter fileWriter = new FileWriter(f1.getName(), true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            String data = "";
            LocalTime time = LocalTime.now();
            switch (msgType) {
                case 0:
                    data = time + ": Peer " + peerID + " makes a connection to Peer " + peer2ID + ".\n";
                    break;
                case 1:
                    data = time + ": Peer " + peerID + " is connected from Peer " + peer2ID + ".\n";
                    break;
                case 2:
                    data = time + ": Peer " + peerID + " has the preferred neighbors " + Arrays.toString(prefNeighbors) + ".\n";
                    break;
                case 3:
                    data = time + ": Peer " + peerID + " has optimistically unchoked neighbor " + peer2ID + ".\n";
                    break;
                case 4:
                    data = time + ": Peer " + peerID + " is unchoked by Peer " + peer2ID + ".\n";
                    break;
                case 5:
                    data = time + ": Peer " + peerID + " is choked by Peer " + peer2ID + ".\n";
                    break;
                case 6:
                    data = time + ": Peer " + peerID + " received the 'have' message from " + peer2ID + " for the piece " + pieceIndex + ".\n";
                    break;
                case 7:
                    data = time + ": Peer " + peerID + " received the 'interested' message from " + peer2ID + ".\n";
                    break;
                case 8:
                    data = time + ": Peer " + peerID + " received the 'not interested' message from " + peer2ID + ".\n";
                    break;
                case 9:
                    data = time + ": Peer " + peerID + " has downloaded the piece " + pieceIndex + " from " + peer2ID + ". Now the number of pieces it has is " + numPieces + ".\n";
                    break;
                case 10:
                    data = time + ": Peer " + peerID + " has downloaded the complete file.\n";
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
    public synchronized void printPeerInfo() {
        System.out.println("*******Peer Information*******");
        System.out.println("Peer ID:" + peerID);
        System.out.println("Hostname:" + hostName);
        System.out.println("The Listening Port:" + listeningPort);
        System.out.println("Has File:" + hasFile);
    }
}