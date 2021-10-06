package main.java;

import java.util.LinkedHashMap;

public class Peer {
    String hostName;
    int peerID;
    int listeningPort;
    boolean hasFile;
    int [][] filePieces;

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
    public void setIsChoked (boolean choked) {
        isChoked = choked;
    }

    private void setFilePieces (int fileSize, int pieceSize ) {
        int numPieces = fileSize/pieceSize;
        filePieces = new byte[numPieces][pieceSize];
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public int getPeerID() {
        return peerID;
    }

    /* Print the Peer details */
    public void printPeerInfo() {
        System.out.println("The Hostname:"+ hostName );
        System.out.println("Peer ID:" + peerID );
        System.out.println("The Listening Port:" + listeningPort );
        System.out.println("Has File:" + hasFile);
    }
}