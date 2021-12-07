package main.java;

// Imports

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.LocalTime;
import java.util.*;

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
    private boolean[][] hasPieces;
    private byte[][] filePieces;
    private int[] ReqPfromNeighbors;
    private boolean wantToClose;
    private Server server;
    private Client[] clients;
    private FileConverter fc;
    private LinkedHashMap<Integer, String[]> AllPeers;
    private LinkedHashMap<String, String> commonInfo;
    private ArrayList<Integer> IndexOfPiecesMissing;
    private int PieceSize;
    private int fileSize;
    int unchokeInterval;
    int OptimisticUnchokeInterval;

    int[] PreferredNeighbors;
    int[] filesTranseredToPN;
    int OptPeer;
    int fileTransferedToOPN;


    // This is the constructor of the class Peer
    public Peer(int key, LinkedHashMap<Integer, String[]> peerInfo, LinkedHashMap<String,
            String> cInfo, Server server, Client[] clients) throws IOException {
        //Sets all peer object variable to the info obtained from reading peerInfo.cfg and commonInfo.cfg
        hostName = peerInfo.get(key)[0];
        listeningPort = Integer.parseInt(peerInfo.get(key)[1]);
        hasFile = Integer.parseInt(peerInfo.get(key)[2]) == 1;
        peerID = key;
        AllPeers = peerInfo;
        //Sets all arrays of isChoked and isInterested to false
        isChoked = new boolean[peerInfo.size()];
        isInterested = new boolean[peerInfo.size()];
        commonInfo = cInfo;

        for (int i = 0; i < peerInfo.size(); i++) {
            isChoked[i] = false;
            isInterested[i] = false;
        }

        //Sets all other Peer object variables
        wantToClose = false;
        this.server = server;
        this.clients = clients;

        ReqPfromNeighbors = new int[Integer.parseInt(commonInfo.get("NumberOfPreferredNeighbors"))];
        PreferredNeighbors = new int[Integer.parseInt(commonInfo.get("NumberOfPreferredNeighbors"))];
        Arrays.fill(PreferredNeighbors, -1);
        Arrays.fill(ReqPfromNeighbors, -1);
        filesTranseredToPN = new int[PreferredNeighbors.length];
        Arrays.fill(filesTranseredToPN, 0);
        OptPeer = 0;
        fileTransferedToOPN = 0;

        OptimisticUnchokeInterval = Integer.parseInt(commonInfo.get("OptimisticUnchokingInterval"));
        unchokeInterval = Integer.parseInt(commonInfo.get("UnchokingInterval"));

        setFilePieces(commonInfo, hasFile, key);
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
        hasPieces = new boolean[AllPeers.size()][(int) Math.ceil((double) fileSize / PieceSize)];
        IndexOfPiecesMissing = new ArrayList<>((int) Math.ceil((double) fileSize / PieceSize));

        for (int i = 0; i < AllPeers.size(); i++) {
            Arrays.fill(hasPieces[i], false);
        }

        if (hasFile) {
            //filePieces = fc.fileToByte("src/main/java/project_config_file_small/project_config_file_small/"+key+"/thefile", commonInfo);
            filePieces = fc.fileToByte(new File("NetworkingProject\\src\\main\\java\\project_config_file_small\\project_config_file_small\\" + key + "\\thefile").getCanonicalPath(), commonInfo);
            for (int i = 0; i < (int) Math.ceil((double) fileSize / PieceSize); i++) {
                hasPieces[getPeerID() - 1001][i] = true;
            }

        } else {
            filePieces = new byte[(int) Math.ceil((double) fileSize / PieceSize)][PieceSize];

            for (int i = 0; i < filePieces.length; i++)
                IndexOfPiecesMissing.add(i, i);
        }
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

    public synchronized ArrayList<Integer> getIndexOfPiecesMissing() {
        return IndexOfPiecesMissing;
    }

    // Returns if the peer has the complete file or not
    public synchronized boolean getHasFile() {
        return hasFile;
    }

    public synchronized boolean[][] getHasPieces() {
        return hasPieces;
    }

    // Get pieces of file
    public synchronized byte[][] getFilePieces() {
        return filePieces;
    }

    //Formats the Choke
    public synchronized byte[] ChokeMsg() {
        return new byte[]{0, 0, 0, 1, 0};
    }

    //Formats the UnChoke
    public synchronized byte[] UnChokeMsg() {
        return new byte[]{0, 0, 0, 1, 1};
    }

    //Formats the Interested
    public synchronized byte[] InterestedMsg() {
        return new byte[]{0, 0, 0, 1, 2};
    }

    //Formats the UnInterested
    public synchronized byte[] UnInterestedMsg() {
        return new byte[]{0, 0, 0, 1, 3};
    }

    //Formats the Have Message
    public synchronized byte[] haveMsg(int hasIndex) {

        byte[] mL = ByteBuffer.allocate(4).putInt(5).array();
        byte mT = (byte) 4;
        byte[] payload = ByteBuffer.allocate(4).putInt(hasIndex).array();
        byte[] hasM = new byte[9];
        for (int i = 0; i < 9; i++) {
            if (i < 4)
                hasM[i] = mL[i];
            else if (i == 4)
                hasM[i] = mT;
            else
                hasM[i] = payload[i - 5];
        }
        return hasM;


    }

    // Calculates the bitfield and formats the message
    // 4 Bytes Length + 1 Byte Type + Variable Bytes for payload
    public synchronized byte[] getBitFieldMessage() {
        int b = (int) Math.ceil(Math.log((double) fileSize / PieceSize) / Math.log(2));
        // BitSet bitSet = new BitSet((int) Math.pow(2, b));
        BitSet bitSet = new BitSet((int) Math.ceil((double) fileSize / PieceSize));

        for (int i = 0; i < hasPieces[peerID - 1001].length; i++) {
            if (hasPieces[peerID - 1001][i]) {
                //bitfield[(int) Math.floor((double) i / 8)] |= 1 << (7 - i % 8);
                bitSet.set(i, true);
            } else
                bitSet.set(i, false);
        }

        //byte[] bitf = bitSet.toByteArray();
        byte[] bitfield = bitSet.toByteArray();


        if (bitfield.length == 0) {
            bitfield = new byte[(int) Math.ceil((double) (fileSize / PieceSize) / 8)];
        }

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

    //Creates the request message to be sent
    public synchronized byte[] requestMessage(int pieceReq) {
        byte[] mL = ByteBuffer.allocate(4).putInt(5).array();
        byte mT = (byte) 6;
        byte[] payload = ByteBuffer.allocate(4).putInt(pieceReq).array();
        byte[] reqM = new byte[9];
        for (int i = 0; i < 9; i++) {
            if (i < 4)
                reqM[i] = mL[i];
            else if (i == 4)
                reqM[i] = mT;
            else
                reqM[i] = payload[i - 5];
        }
        return reqM;
    }

    //Similar to request Message, but incorporates the entire piece into the bitfield
    public synchronized byte[] pieceMessage(int pieceReq) {
        byte mT = (byte) 7;
        byte[] pieceData = filePieces[pieceReq];
        byte[] Indexload = ByteBuffer.allocate(4).putInt(pieceReq).array();
        byte[] mL = ByteBuffer.allocate(4).putInt(5 + pieceData.length).array();
        byte[] reqM = new byte[9 + pieceData.length];
        for (int i = 0; i < reqM.length; i++) {
            if (i < 4)
                reqM[i] = mL[i];
            else if (i == 4)
                reqM[i] = mT;
            else if (i < 9)
                reqM[i] = Indexload[i - 5];
            else
                reqM[i] = pieceData[i - 9];
        }
        return reqM;
    }

    // Returns the listing port of the peer
    public synchronized int getListeningPort() {
        return listeningPort;
    }

    // Returns the peer's ID number
    public synchronized int getPeerID() {
        return peerID;
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
                if(!isChoked[OtherPeer-1001]) {
                    isChoked[OtherPeer - 1001] = true;
                    writeLogMessage(OtherPeer, PreferredNeighbors, 0, 0, 5);
                }
                break;
            case 1:
                // UNCHOKE - Set isChoked to false

                //If the peer correlates to a preferred neighbor
                boolean preFNeighbor = false;
                for(int i = 0; i< PreferredNeighbors.length; i++){
                    if(OtherPeer == PreferredNeighbors[i]){
                        preFNeighbor = true;
                        break;
                    }
                }
                if(preFNeighbor || OtherPeer == OptPeer) {
                    if (isChoked[OtherPeer-1001]) {
                        isChoked[OtherPeer - 1001] = false;
                        writeLogMessage(OtherPeer, PreferredNeighbors, 0, 0, 4);
                    }
                }


                break;
            case 2:
                // INTERESTED - Set isInterested to true
                if(!isInterested[OtherPeer-1001]) {
                    isInterested[OtherPeer - 1001] = true;
                    writeLogMessage(OtherPeer, PreferredNeighbors, 0, 0, 7);
                }
                break;
            case 3:
                // UNINTERESTED - Set isInterested to false
                if(isInterested[OtherPeer-1001]) {
                    isInterested[OtherPeer - 1001] = false;
                    writeLogMessage(OtherPeer, PreferredNeighbors, 0, 0, 8);
                }
                break;
            case 4:
                // HAVE
                // TODO: IMPLEMENT HAVE
                byte[] OPhave = new byte[4];
                for (int i = 5; i < message.length; i++)
                    OPhave[i - 5] = message[i];
                int OPH = ByteBuffer.wrap(OPhave).getInt();
                hasPieces[OtherPeer - 1001][OPH] = true;

                boolean ThisPeerIsInterested = false;
                for (int i = 0; i < hasPieces[peerID - 1001].length; i++) {
                    if (hasPieces[peerID - 1001][i] != hasPieces[OtherPeer - 1001][i] && hasPieces[OtherPeer - 1001][i]) {
                        ThisPeerIsInterested = true;
                        break;
                    }
                }

                try {
                    if (ThisPeerIsInterested) {
                        if (OtherPeer < getPeerID())
                            clients[OtherPeer - 1001].sendRequest(InterestedMsg());
                        else
                            clients[OtherPeer - 1002].sendRequest(InterestedMsg());
                    } else {
                        if (OtherPeer < getPeerID())
                            clients[OtherPeer - 1001].sendRequest(UnInterestedMsg());
                        else
                            clients[OtherPeer - 1002].sendRequest(UnInterestedMsg());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //System.out.println("Have");
                writeLogMessage(OtherPeer, PreferredNeighbors, OPH, 0, 6);


                break;
            case 5:
                // BITFIELD
                //As soon as a BitFieldRequest is sent
                if(message.length > 5) {
                    byte[] bFieldResp = getBitFieldMessage();

                    boolean interestedINPeer = false;
                    //BitSet RecievedM = BitSet.valueOf(message);
                    //BitSet BitFiled = BitSet.valueOf(bFieldResp)
                    for (int i = 0; i <= (message.length - 1) * 8; i++) {
                        if (isSet(bFieldResp, i) != isSet(message, i))
                            interestedINPeer = true;

                    }

                    //The bitfield starts at byte 5 aka bit 40
                    for (int i = 40; i < 40 + hasPieces[OtherPeer - 1001].length; i++) {
                        if (isSet(message, i)) {
                            hasPieces[OtherPeer - 1001][i - 40] = true;
                        }
                    }
                    isInterested[OtherPeer - 1001] = interestedINPeer;

                    try {
                        if (interestedINPeer) {
                            if (OtherPeer < getPeerID())
                                clients[OtherPeer - 1001].sendRequest(InterestedMsg());
                            else
                                clients[OtherPeer - 1002].sendRequest(InterestedMsg());
                        } else {
                            if (OtherPeer < getPeerID())
                                clients[OtherPeer - 1001].sendRequest(UnInterestedMsg());
                            else
                                clients[OtherPeer - 1002].sendRequest(UnInterestedMsg());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                    System.out.println("Bitfield");
                }
                break;
            case 6:
                // REQUEST
                // After initial BITFIELD you need to request the pieces that are needed

                // Check for any preferred neighbors


                if (!isChoked[OtherPeer - 1001]) {

                    boolean isPreferred = false;
                    for(int i = 0; i < PreferredNeighbors.length; i++){
                        if(PreferredNeighbors[i] == OtherPeer){
                            filesTranseredToPN[i]++;
                            isPreferred = true;
                            break;
                        }
                    }

                    if(isPreferred || OtherPeer == OptPeer) {
                        if(OtherPeer == OptPeer)
                            fileTransferedToOPN++;
                        byte[] OPReq = new byte[4];
                        for (int i = 5; i < message.length; i++)
                            OPReq[i - 5] = message[i];

                        int OPReqed = ByteBuffer.wrap(OPReq).getInt();

                        try {
                            if (OtherPeer < getPeerID())
                                clients[OtherPeer - 1001].sendRequest(pieceMessage(OPReqed));
                            else
                                clients[OtherPeer - 1002].sendRequest(pieceMessage(OPReqed));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
                break;
            case 7:
                // PIECE
                // TODO: IMPLEMENT PIECE
                if(!hasFile) {
                    byte[] pieceIndex = new byte[4];
                    byte[] pieceReceived = new byte[PieceSize];
                    for (int i = 5; i < message.length; i++) {
                        if (i < 9)
                            pieceIndex[i - 5] = message[i];
                        else
                            pieceReceived[i - 9] = message[i];
                    }
                    int pIndex = ByteBuffer.wrap(pieceIndex).getInt();

                    boolean badPayload = true;
                    for (int p = 0; p < pieceReceived.length; p++){
                        if(pieceReceived[p] != 0){
                            badPayload = false;
                            break;
                        }
                    }

                    if (!badPayload && !hasPieces[peerID - 1001][pIndex]) {
                        filePieces[pIndex] = pieceReceived;
                        hasPieces[peerID - 1001][pIndex] = true;


                        for (int i = 0; i < ReqPfromNeighbors.length; i++)
                            if (ReqPfromNeighbors[i] == pIndex) {
                                ReqPfromNeighbors[i] = -1;
                            }

                        for (int i = 0; i < IndexOfPiecesMissing.size(); i++) {
                            if (IndexOfPiecesMissing.get(i) == pIndex) {
                                IndexOfPiecesMissing.remove(i);
                                IndexOfPiecesMissing.trimToSize();
                            }
                        }
                        int numberOfPieces = 0;
                        for(int i = 0; i < hasPieces[peerID - 1001].length; i++) {
                            if(hasPieces[peerID-1001][i]) {
                                numberOfPieces++;
                            }
                        }
                        writeLogMessage(OtherPeer, PreferredNeighbors, pIndex, numberOfPieces, 9);

                        if (!hasFile) {
                            boolean check = true;
                            for (int i = 0; i < hasPieces[peerID - 1001].length; i++) {

                                if (!hasPieces[peerID - 1001][i]) {
                                    check = false;
                                    break;
                                }

                            }
                            hasFile = check;
                            if (hasFile) {
                                writeLogMessage(0, null, 0, 0, 10);
                            }
                        }

                        // Once 'receive' a piece send HAVE to all the other Peers
                        try {
                            for (int i = 0; i < clients.length; i++)
                                clients[i].sendRequest(haveMsg(pIndex));
                        } catch (IOException e) {
                            System.err.println("IOEX in Piece recieved");
                        }

                /* uwu */
                    }
                }
                break;
        }
    }

    //Determines the preferred neighbors during the unchoked interval
    public synchronized void determinePreferredNeighbors() {

        //If requested has values inside store because messages could be lost
        for(int i = 0; i < ReqPfromNeighbors.length; i++){
            if(ReqPfromNeighbors[i] != -1) {
                IndexOfPiecesMissing.add(ReqPfromNeighbors[i]);
                ReqPfromNeighbors[i] = -1;
            }
        }

        // -1 signifies a lack of neighbor
        boolean PreferredEmpty = false;
        for (int i = 0; i < PreferredNeighbors.length; i++) {
            if (PreferredNeighbors[i] == -1) {
                PreferredEmpty = true;
                break;
            }
        }
        // If there are neighbors (i.e., this is not at the server start), continue
        if (!PreferredEmpty) {

            int[] downLoadRateResults = new int[PreferredNeighbors.length];
            for (int i = 0; i < PreferredNeighbors.length; i++) {
                downLoadRateResults[i] = filesTranseredToPN[i] / unchokeInterval;
                filesTranseredToPN[i] = 0;
            }
            //Determines if Optimistic is better than one of than one preferred neighbors
            int downLoadRateForOp = fileTransferedToOPN / unchokeInterval;
            fileTransferedToOPN = 0;

            //check if OP is one of the preferred neighbors
            boolean OPisNotaPreferred = false;
            for (int preferredNeighbor : PreferredNeighbors) {
                if (OptPeer == preferredNeighbor) {
                    OPisNotaPreferred = true;
                    break;
                }
            }
            //If OP is not a preferred Neighbor check to see if it is better than other preferred neighbors
            if (!OPisNotaPreferred) {
                for (int i = 0; i < PreferredNeighbors.length; i++) {
                    if (downLoadRateForOp > downLoadRateResults[i]) {
                        PreferredNeighbors[i] = OptPeer;
                        break;
                    }
                }
            }
        }
        // This is the start of the network, select random neighbors!
        else {
            // Not going to look at interested to start since there will be peers with nothing
            ArrayList<Integer> interested = new ArrayList<Integer>(isInterested.length) {
            };
            for (int i = 0; i < isInterested.length; i++) {
                interested.add(i, 1001 + i);
            }

            // Randomly remove from the list, thus randomly selecting peers.
            for (int n = 0; n < PreferredNeighbors.length; n++) {
                int randPeer = (int) (Math.random() * interested.size());
                PreferredNeighbors[n] = interested.get(randPeer);
                interested.remove(randPeer);
            }
            writeLogMessage(0, PreferredNeighbors, 0, 0, 2);
        }
    }

    //Determines the optimistic neighbors during the unchoked interval
    public synchronized void determineOpNeighbors() {

        //Checking any peers are interested in this peer
        boolean thereArePeersInterested = false;
        for (int i = 0; i < isInterested.length; i++) {
            for (int j = 0; j < PreferredNeighbors.length; j++){
                if (isInterested[i] && PreferredNeighbors[j] != 1001+j) {
                    thereArePeersInterested = true;
                }
                else{
                    thereArePeersInterested = false;
                    break;
                }
            }
        }
        if(thereArePeersInterested) {
            // What is interested and not a preferred neighbor already?
            ArrayList<Integer> interested = new ArrayList<>() {
            };
            for (int i = 0; i < isInterested.length; i++) {
                if (isInterested[i]) {
                    boolean isAlreadyPreferred = false;
                    for (int preferredNeighbor : PreferredNeighbors) {
                        if (preferredNeighbor == 1001 + i) {
                            isAlreadyPreferred = true;
                            break;
                        }
                    }
                    if (!isAlreadyPreferred)
                        interested.add(i + 1001);
                }
            }

            int randP = (int) (Math.random() * interested.size());
            if(interested.size() > 0)
                OptPeer = interested.get(randP);
            fileTransferedToOPN = 0;
        }
        else {
            ArrayList<Integer> chokedNeighbors = new ArrayList<>();
            for(int i = 0; i < isChoked.length; i++){
                for (int j = 0; j < PreferredNeighbors.length; j++){
                    if(isChoked[i] && PreferredNeighbors[j] == 1001+i){
                        break;
                    }

                }
                chokedNeighbors.add(i, i+1001);
            }
            int randP = (int) (Math.random() * chokedNeighbors.size());
            OptPeer = chokedNeighbors.get(randP);
        }
        writeLogMessage(OptPeer, null, 0, 0, 3);
    }

    // Check for whether a peer has a desired file piece.
    public synchronized int FindPieceToRequest(int OtherPeer) {

        //boolean dNHTFile = !hasFile;
        //for (int i = 0; i < hasPieces[peerID-1001].length; i++)
        //if (!hasPieces[peerID - 1001][i])
        //dNHTFile = true;
        boolean reqIsfull = true;
        for(int i = 0; i < ReqPfromNeighbors.length; i++){
            if(ReqPfromNeighbors[i] == -1){
                reqIsfull = false;
                break;
            }
        }
        if(reqIsfull){
            for(int i = 0; i < ReqPfromNeighbors.length; i++){
                IndexOfPiecesMissing.add(ReqPfromNeighbors[i]);
                ReqPfromNeighbors[i] = -1;
            }


        }

        if(isChoked[OtherPeer-1001]) {

            // Making sure that we are not requesting what is already requested
            boolean hasPieceNReq = false;
            if (!hasFile) { // This peer doesn't have the file

                for (int i = 0; i < IndexOfPiecesMissing.size(); i++) {//Looking through the OtherPeer for pieces we need

                    for (int j = 0; j < ReqPfromNeighbors.length; j++) {// Looking through what has been requested

                        if (hasPieces[OtherPeer - 1001][IndexOfPiecesMissing.get(i)] && ReqPfromNeighbors[j] != IndexOfPiecesMissing.get(i)) {// Has a piece we need and not requested yet
                            hasPieceNReq = true;
                        } else {
                            hasPieceNReq = false;
                            break;
                        }
                    }
                }

            }

            if (!hasFile && hasPieceNReq) {
                int randP = (int) (Math.random() * IndexOfPiecesMissing.size());
                boolean ReqAlready = false;

                // While the Other peers has a piece not already requested
                do {
                    for (int i = 0; i < ReqPfromNeighbors.length; i++) {
                        if (ReqPfromNeighbors[i] != IndexOfPiecesMissing.get(randP) && hasPieces[OtherPeer - 1001][IndexOfPiecesMissing.get(randP)]) {
                            ReqAlready = false;
                        } else {
                            ReqAlready = true;
                            randP = (int) (Math.random() * IndexOfPiecesMissing.size());
                            break;
                        }
                    }
                } while (ReqAlready);
                for (int i = 0; i < ReqPfromNeighbors.length; i++) {
                    if (ReqPfromNeighbors[i] == -1) {
                        ReqPfromNeighbors[i] = IndexOfPiecesMissing.get(randP);
                        break;
                    }
                }


                return IndexOfPiecesMissing.get(randP);
            }
            else {
                for(int i = 0; i<ReqPfromNeighbors.length; i++){
                    if(ReqPfromNeighbors[i] != -1){
                       return ReqPfromNeighbors[i];
                    }
                }
            }

        }
        return -1;
    }


    // Sets bits, good for comparison
    public synchronized boolean isSet(byte[] bitArr, int bit) {
        int i = (int) Math.floor((double) bit / 8);
        int bitPos = bit % 8;
        if(i >= bitArr.length){
            return false;
        }
        return (bitArr[i] >> bitPos & 1) == 1;
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
            String path = "/log_peer_" + peerID + ".log";
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

    public synchronized void savePiecesAsFile(){

        fc = new FileConverter();
        try {
            fc.byteToFile(filePieces,new File("NetworkingProject\\src\\main\\java\\project_config_file_small\\project_config_file_small\\" + peerID).getCanonicalPath(), new File("NetworkingProject\\src\\main\\java\\project_config_file_small\\project_config_file_small\\" + peerID + "\\thefile").getCanonicalPath(), commonInfo);
        }catch (IOException e){
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