package main.java;
// Bullock, Peltekis, & Salazar P2P Project

// Imports

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.charset.StandardCharsets;
import java.util.*;

/*
 * Main Project File:
 * Collects PeerInfo.cfg and Common.cfg and begins connections
 */
class SSHProject extends Thread {
    static LinkedHashMap<Integer, String[]> PeerInfo;
    static LinkedHashMap<String, String> CommonInfo;
    public Server[] servers;
    public Client[][] clients;
    Set<Integer> keySetArray;
    Integer[] keySet;

    /*
     * The readPeerInfo will take in the file path to the Peer information document.
     * This function will scan the file and place the information into a linked hash map
     * which will be utilized to build our peer object.
     */
    public static LinkedHashMap<Integer, String[]> readPeerInfo(String path) {
        //Function Variables
        LinkedHashMap<Integer, String[]> lhm = new LinkedHashMap<>();


        //Try catch is necessary to handle IO errors
        try {
            // Create input stream; read first byte
            File file = new File(path);
            Scanner input = new Scanner(file);

            /*
             * PeerInfo.cfg Map Legend
             * Key: Peer ID <int>
             * Value: "Term" array that includes
             *  [0] - Hostname
             *  [1] - Port number
             *  [2] - Peer has the file?
             */
            while (input.hasNextLine()) {
                // Preprocessing
                String[] term = new String[3];
                String line = input.nextLine();
                String[] lineArr = line.split(" ");
                // Establish Map Entry
                int key = Integer.parseInt(lineArr[0]);
                term[0] = lineArr[1];
                term[1] = lineArr[2];
                term[2] = lineArr[3];
                lhm.put(key, term);
            }

        } catch (Exception e) {
            // Error Handling
            System.out.println("Error!  Pathway does not exist!");
            e.getStackTrace();
        }

        return lhm;
    }

    /*
     * ReadCommon is identical to our readPeerInfo() function. This function will
     * scan the common.cfg file and save the information into a linked hash map
     * which will be utilized to configure our TCP network settings.
     */

    public static LinkedHashMap<String, String> readCommon(String path) {
        LinkedHashMap<String, String> lhm = new LinkedHashMap<>();
        try {
            // Create input stream; read first byte
            File file = new File(path);
            Scanner input = new Scanner(file);

            /*
             * Common.cfg Map Legend
             * Keys: <String>
             *  - Number of Preferred Neighbors
             *  - Unchoking Interval
             *  - Optimistic Unchoking Interval
             *  - File Name
             *  - File Size
             *  - Piece Size
             * Value: [Respective <int> value]
             */
            while (input.hasNextLine()) {
                // Preprocess
                String key;
                String term;
                String line = input.nextLine();
                String[] lineArr = line.split(" ");

                // Establish Map Entry
                key = lineArr[0];
                term = lineArr[1];
                lhm.put(key, term);
            }
        } catch (Exception e) {
            // Scream!
            System.out.println("REEEEEEEE!!!  IT NO EXIST!!!  FEED ME PROPER PATH!!!");
            e.getStackTrace();
        }

        return lhm;
    }


    /*I figured out how to thread through this youtube video
    https://www.youtube.com/watch?v=eQk5AWcTS8w&ab_channel=JakobJenkov
    and this Stack overflow question
    https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread
     */
    public static class PeerServer implements Runnable {
        Server Peer;
        int key;
        int port;
        Peer sPeer;

        PeerServer(Server peer, int k, int p, Peer serverP) {
            key = k;
            port = p;
            Peer = peer;
            sPeer = serverP;
        }

        @Override
        public void run() {
            System.out.println("Server " + key + " " + port + " is running");
            try {
                Peer.startServer(key, port + key, sPeer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static class PeerClientBehavior implements Runnable {
        Client[] cPeer;
        int key;
        Peer ClientPeer;
        boolean Finished;

        PeerClientBehavior(Client[] peer, int k, Peer ClientP) {
            key = k;
            cPeer = peer;
            ClientPeer = ClientP;
        }

        @Override
        public void run() {

            System.out.println("Client " + key + " is running");
            byte[] message;
            message = ClientPeer.getBitFieldMessage();
            if (ClientPeer.getHasFile()) {
                for (int i = 0; i < cPeer.length; i++) {
                    try {
                        cPeer[i].sendRequest(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            for (int i = 0; i < cPeer.length; i++) {
                try {
                    cPeer[i].sendRequest(ClientPeer.ChokeMsg());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // This section was for testing an unchoked file transfers
            /*int pieceReq;
            ArrayList<Integer> missing;
            do {
            for (int i = 0; i< cPeer.length; i++) {
                missing = ClientPeer.getIndexOfPiecesMissing();
                try {
                if(ClientPeer.getPeerID() - 1001 > i) {
                    pieceReq = ClientPeer.FindPieceToRequest(1001 + i);
                    if(pieceReq != -1)
                        //missing.get(pieceReq)
                        cPeer[i].sendRequest(ClientPeer.requestMessage(pieceReq));
                }
                else {
                    pieceReq = ClientPeer.FindPieceToRequest(1002 + i);
                    if(pieceReq!= -1)
                        cPeer[i].sendRequest(ClientPeer.requestMessage(pieceReq));
                }


                } catch(IOException e) {
                    e.printStackTrace();
                }


            }
        }while (!ClientPeer.getHasFile());
        */

        }

    }

    /*
     * In order for the main to run properly User needs to enter the locations
     * of the PeerInfo.cfg and CommonInfo.cfg in that order. With this information
     * the main will use the information provided to run the read functions mentioned
     * previously. The Linked Hash Maps will then been utilized to build the Peer Object
     * and TCP Network.
     */

    public static void main(String[] args) throws IOException {
        ArrayList<Peer> peersOnline = new ArrayList<>();
        //Setup scanner for user input
        System.out.println("0 for small, 1 for large");
        Scanner userInput = new Scanner(System.in);
        int in = userInput.nextInt();
        String path = "";
        String workFile = "";
        LinkedHashMap<Integer, String[]> PeerInfo;
        LinkedHashMap<String, String> CommonInfo;
        if(in == 0) {
            path = new File("NetworkingProject/src/main/java/project_config_file_small/project_config_file_small/PeerInfo.cfg").getCanonicalPath();
            PeerInfo = readPeerInfo(path);
            path = new File("NetworkingProject/src/main/java/project_config_file_small/project_config_file_small/Common.cfg").getCanonicalPath();
            CommonInfo = readCommon(path);
            workFile = new File("NetworkingProject/src/main/java/project_config_file_small/project_config_file_small/").getCanonicalPath();
        } else {
            path = new File("NetworkingProject/src/main/java/project_config_file_large/project_config_file_large/PeerInfo.cfg").getCanonicalPath();
            PeerInfo = readPeerInfo(path);
            path = new File("NetworkingProject/src/main/java/project_config_file_large/project_config_file_large/Common.cfg").getCanonicalPath();
            CommonInfo = readCommon(path);
            workFile = new File("NetworkingProject/src/main/java/project_config_file_large/project_config_file_large/").getCanonicalPath();
        }

        /*
         * PeerInfo.cfg Map Legend
         * Key - Peer ID <String>
         * Value - "Term" array that includes
         *  [0] - Hostname
         *  [1] - Port number
         *  [2] - Peer has the file?
         */

        /*
         * Common.cfg Map Legend
         * Keys: <String>
         *  - Number of Preferred Neighbors
         *  - Unchoking Interval
         *  - Optimistic Unchoking Interval
         *  - File Name
         *  - File Size
         *  - Piece Size
         * Value: [Respective <int> value]
         */

        Set<Integer> keySetArray = PeerInfo.keySet();
        Integer[] keySet = keySetArray.toArray(new Integer[keySetArray.size()]);

        // Testing sample values
        System.out.println(Arrays.toString(PeerInfo.get(1001)));
        System.out.println(Arrays.toString(PeerInfo.get(1004)));

        // Test Print
        for (int i = 0; i <= 8; i++) {
            int temp = 1001 + i;
            System.out.println(Arrays.toString(PeerInfo.get(temp)));
        }

        /*
         * This section here is responsible for creating both the server
         * and client sockets. For now the client sockets will be left unconnected
         * In the peer object we will create a private function that will connections
         * to each of the hosts.
         */


        //Need Java timers in order to keep everything in time and efficient
        //https://docs.oracle.com/javase/7/docs/api/java/util/Timer.html
        //In order to understand how they work

        Server server = new Server();
        Thread thread = new Thread();
        Client[] clients = new Client[keySet.length - 1];

        /*
         * This for loop is responsible for creating peer objects which
         * will contain all the necessary information that our Peer-2-Peer
         * network will be utilizing
         */
        // TODO: If IP address causes issue, try using ".trim()"
        String compIpAddress = Inet4Address.getLocalHost().getHostAddress();
        int peerId = 0;
        for (int i = 0; i < PeerInfo.size(); i++) {
            if (PeerInfo.get(0)[0].equals(compIpAddress)) {
                peerId = keySet[i];
            }
        }

        peersOnline.add(new Peer(peerId, PeerInfo, CommonInfo, server, clients, workFile));


        //Starting severs in different threads
        int port = Integer.parseInt(PeerInfo.get(peerId - 1001)[1]);
        String hostname = PeerInfo.get(peerId - 1001)[0];

        //ServerSocket serverSocket =
        server = new Server();
        //Making threads for servers
        thread = new Thread(new PeerServer(server, peerId, port, peersOnline.get(peerId - 1001)));
        thread.start();


        int foundDup = 0;
        for (int j = 0; j < keySet.length; j++) {
            if (keySet[j] != peerId) {
                int portN = Integer.parseInt(PeerInfo.get(1001 + j)[1]);
                hostname = PeerInfo.get(1001 + j)[0];
                //Client client = new Client(PeerInfo.get(1001+i)[0], Integer.parseInt(PeerInfo.get(1001+i)[1]));
                Client client = new Client(peersOnline.get(j));
                client.start();
                client.startConnection(hostname, portN + keySet[j]);

                peersOnline.get(j).writeLogMessage(peersOnline.get(peerId - 1001).getPeerID(), null, 0, 0, 0);

                //Handshake need to make it byte[]
                String handshakeMessage = "P2PFILESHARINGPROJ0000000000";
                handshakeMessage += peerId;
                byte[] handShake = handshakeMessage.getBytes(StandardCharsets.UTF_8);
                //client.handMessage(handshakeMessage);
                client.sendRequest(handShake);
                clients[j - foundDup] = client;
            } else
                foundDup = 1;

        }
        System.out.println("End of Clients for " + peerId);


        peersOnline.get(peerId - 1001).setClientSockets(clients);
        peersOnline.get(1001).setServerSockets(server);




        /*
            Set a loop that uses clients to send requests to peers
            Server handles request and uses their client to do something
         */

        //ServerSocket serverSocket =
        //Making threads for servers
        Thread cThreads = new Thread(new PeerClientBehavior(clients, peerId, peersOnline.get(peerId - 1001)));
        cThreads.start();


        //TIMER
        final boolean[] timerRunning = {false, false};
        Timer UnchokeInterval = new Timer();
        int finalPeerId = peerId;
        TimerTask Chk = new TimerTask() {
            @Override
            public void run() {
                try {
                    timerRunning[0] = false;
                    for (int c = 0; c < clients.length; c++) {
                        clients[c].sendRequest(peersOnline.get(finalPeerId - 1001).ChokeMsg());
                    }
                    peersOnline.get(finalPeerId - 1001).determinePreferredNeighbors();

                    for (int c = 0; c < clients.length; c++) {
                        clients[c].sendRequest(peersOnline.get(finalPeerId - 1001).UnChokeMsg());
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        Timer OPUnchokeInterval = new Timer();
        int finalPeerId1 = peerId;
        TimerTask Op = new TimerTask() {
            @Override
            public void run() {
                timerRunning[1] = false;
                peersOnline.get(finalPeerId1 - 1001).determineOpNeighbors();

                    for (int c = 0; c < clients.length; c++) {
                        try {
                            clients[c].sendRequest(peersOnline.get(finalPeerId1 -1001).UnChokeMsg());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }



            }
        };


        //This checks if all the peers have the file, if so they will end the program
        boolean AllFinished;
        int totalNumDone = 0;
        int numDone = 0;
        boolean[][] peerProg = peersOnline.get(0).getHasPieces();

        UnchokeInterval.scheduleAtFixedRate(Chk, peersOnline.get(peerId-1001).unchokeInterval * 1000, peersOnline.get(peerId-1001).unchokeInterval * 1000);
        OPUnchokeInterval.scheduleAtFixedRate(Op, peersOnline.get(peerId-1001).OptimisticUnchokeInterval * 1000, peersOnline.get(peerId-1001).OptimisticUnchokeInterval * 1000);

        do {
            peerProg = peersOnline.get(peerId-1001).getHasPieces();
            AllFinished = true;
            numDone = 0;


            for (int i = 0; i < peerProg.length; i++) {
                for (int j = 0; j < peerProg[i].length; j++) {
                    if (!peerProg[i][j]) {
                        AllFinished = false;
                    } else {
                        numDone++;
                    }
                }
            }
            if (totalNumDone != numDone) {
                totalNumDone = numDone;
                System.out.println(totalNumDone);
            }

        } while (!AllFinished);

        System.out.println("All Peers have File");


        peersOnline.get(peerId-1001).savePiecesAsFile();

        System.out.println("AllFilesArePrinted");
        System.exit(0);

        /*
         * To-do list
         *
         */
    }
}
