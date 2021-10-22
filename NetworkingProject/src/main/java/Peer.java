package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedHashMap;

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
    public ServerSocket getServerSocket{ return serverSocket;}

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
    public void interpretMessage(int PeerID, byte messageType)
    {
        switch(messageType) {
            case 0:
                // CHOKE - Set isChoked to true
                isChoked[peerID-1001] = true;
                break;
            case 1:
                // UNCHOKE - Set isChoked to false
                isChoked[peerID-1001] = false;
                break;
            case 2:
                // INTERESTED - Set isInterested to true
                isInterested[peerID-1001] = true;
                break;
            case 3:
                // UNINTERESTED - Set isInterested to false
                isInterested[peerID-1001] = false;
                break;
            case 4:
                // HAVE
                // TODO: IMPLEMENT HAVE
                System.out.println("Have");
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

    // Print the Peer details
    public void printPeerInfo() {
        System.out.println("*******Peer Information*******");
        System.out.println("Peer ID:" + peerID );
        System.out.println("Hostname:"+ hostName );
        System.out.println("The Listening Port:" + listeningPort );
        System.out.println("Has File:" + hasFile);
    }
}