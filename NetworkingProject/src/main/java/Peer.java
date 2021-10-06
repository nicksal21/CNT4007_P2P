public class Peer {
    String hostName;
    int peerID;
    int listeningPort;
    boolean hasFile;
    boolean isChoked;

    // This is the constructor of the class Employee
    public Peer(int key, LinkedHashMap<Integer, String[]> peerInfo ) {
        hostName = peerInfo[key][1];
        listeningPort = peerInfo[key][2];

        if(peerInfo[key][3]==1)
            hasFile = true;
        else
            hasFile = false;

        isChoked = false;
    }

    // Method to choke or unchoke a peer
    /*
    TODO:
        -Make this function apply to more than one peer
        use a list or map of some sort
     */
    public void isChoked (boolean choked) {
        isChoked = choked;
    }

    /* Assign the designation to the variable designation.*/
    public int getListeningPort() {
        return listeningPort;
    }

    /* Assign the salary to the variable	salary.*/
    public int getPeerID() {
        return peerID;
    }

    /* Print the Employee details */
    public void printPeerInfo() {
        System.out.println("The Hostname:"+ hostName );
        System.out.println("Peer ID:" + peerID );
        System.out.println("The Listening Port:" + listeningPort );
        System.out.println("Has File:" + hasFile);
    }
}