package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.LinkedHashMap;

public class Peer {
    String hostName;
    int peerID;
    int listeningPort;
    boolean hasFile;
    boolean isChoked;
    boolean isInterested;
    byte [][] filePieces;

    // This is the constructor of the class Employee
    public Peer(int key, LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String, Integer> commonInfo ) {
        hostName = peerInfo.get(key)[1];
        listeningPort = Integer.parseInt(peerInfo.get(key)[2]);

        hasFile = Integer.parseInt(peerInfo.get(key)[3])== 1;

        isChoked = false;

        setFilePieces(commonInfo.get("FilesSize"), commonInfo.get("PieceSize"));
    }

    // Method to choke or unchoke a peer
    /*
    TODO:
        -Make this function apply to more than one peer
        use a list or map of some sort
     */
    public void setIsChoked (int peerID, boolean choked) {
        //PeerID -1001 is needed since Array Starts at 0
        isChoked[peerID-1001] = choked;
    }

    private void setFilePieces (int fileSize, int pieceSize ) {
        int numPieces;

        if (fileSize%pieceSize != 0)
            numPieces = fileSize/pieceSize + 1;
        else
            numPieces = fileSize/pieceSize;


        filePieces = new byte[numPieces][];
    }

    // Returns the listing port of the peer
    public int getListeningPort() {
        return listeningPort;
    }

    // Returns the peer's ID number
    public int getPeerID() {
        return peerID;
    }

    /*
    Parameters(s):
    messageType - This will be a byte the dictates the message to be printed

    Function:
    Takes in a message type and outputs the corresponding message
     */
    public void interpretMessage(int PeerID, byte messageType)
    {
        switch(messageType) {
            case 0: // CHOKE
                isChoked[peerID-1001] = true;
                break;
            case 1: // UNCHOKE
                isChoked[peerID-1001] = false;
                break;
            case 2: // INTERESTED
                isInterested[peerID-1001] = true;
                break;
            case 3: // UNINTERESTED
                isInterested[peerID-1001] = false;
                break;
            case 4: // HAVE
                System.out.println("Have");
                break;
            case 5: // BITFIELD
                System.out.println("Bitfield");
                break;
            case 6: // REQUEST
                System.out.println("Request");
                break;
            case 7: // PIECE
                System.out.println("Piece");
                break;
        }
    }

    /* Print the Peer details */
    public void printPeerInfo() {
        System.out.println("*******Peer Information*******");
        System.out.println("Peer ID:" + peerID );
        System.out.println("Hostname:"+ hostName );
        System.out.println("The Listening Port:" + listeningPort );
        System.out.println("Has File:" + hasFile);
    }
}