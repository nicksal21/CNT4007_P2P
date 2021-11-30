package main.java;
// Bullock, Peltekis, & Salazar P2P Project

// Imports

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/*
 * Main Project File:
 * Collects PeerInfo.cfg and Common.cfg and begins connections
 */
class Project extends Thread {
    public Server[] servers;
    public Client[][] clients;

    static LinkedHashMap<Integer, String[]> PeerInfo;
    static LinkedHashMap<String, String> CommonInfo;

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

    /*
     * In order for the main to run properly User needs to enter the locations
     * of the PeerInfo.cfg and CommonInfo.cfg in that order. With this information
     * the main will use the information provided to run the read functions mentioned
     * previously. The Linked Hash Maps will then been utilized to build the Peer Object
     * and TCP Network.
     */

    /*I figured out how to thread through this youtube video
    https://www.youtube.com/watch?v=eQk5AWcTS8w&ab_channel=JakobJenkov
    and this Stack overflow question
    https://stackoverflow.com/questions/877096/how-can-i-pass-a-parameter-to-a-java-thread
     */
    public static class PeerServer implements Runnable {
        Server Peer;
        int key;
        int port;
        PeerServer(Server peer, int k, int p){
            key = k;
            port = p;
            Peer = peer;
        }
        @Override
        public void run(){

            System.out.println("Server " + key + " " + port + " is running");

            try {
                Peer.startServer(key, port+key, PeerInfo, CommonInfo);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    public static void main(String[] args) throws IOException {
        ArrayList<Peer> peersOnline = new ArrayList<>();
        //Setup scanner for user input
        //Scanner userInput = new Scanner(System.in);

        // User inputs the path to the PeerInfo.cfg
        //System.out.println("PeerInfo filepath");
        //String path = userInput.next();
        String path = new File("NetworkingProject/src/main/java/project_config_file_small/project_config_file_small/PeerInfo.cfg").getCanonicalPath();
        /*
         * PeerInfo.cfg Map Legend
         * Key - Peer ID <String>
         * Value - "Term" array that includes
         *  [0] - Hostname
         *  [1] - Port number
         *  [2] - Peer has the file?
         */
        LinkedHashMap<Integer, String[]> PeerInfo = readPeerInfo(path);

        // User inputs the path to the Common.cfg
        //System.out.println("Common filepath");
        //path = userInput.next();
        path = new File("NetworkingProject/src/main/java/project_config_file_small/project_config_file_small/Common.cfg").getCanonicalPath();
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

        LinkedHashMap<String, String> CommonInfo = readCommon(path);


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

        Server[] servers = new Server[keySet.length];
        Thread[] threads = new Thread[keySet.length];
        Client[][] clients = new Client[keySet.length][keySet.length];

        //Starting severs in different threads
        for (int k = 0; k < keySet.length; k++) {
            int key = 1001 + k;
            int port = Integer.parseInt(PeerInfo.get(1001 + k)[1]);
            String hostname = PeerInfo.get(1001 + k)[0];

            //ServerSocket serverSocket =
            servers[k] = new Server();
            //Making threads for servers
            threads[k] = new Thread(new PeerServer(servers[k],key,port));
            threads[k].start();
        }

        for (int i = 0; i < keySet.length; i++) {
            for (int j = 0; j < keySet.length - 1; j++) {
                int portN = Integer.parseInt(PeerInfo.get(1001 + i)[1]);
                String hostname = PeerInfo.get(1001 + i)[0];
                //Client client = new Client(PeerInfo.get(1001+i)[0], Integer.parseInt(PeerInfo.get(1001+i)[1]));
                Client client = new Client();
                client.start();
                client.startConnection(hostname, portN + keySet[i]);

                //Handshake
                String handshakeMessage = "P2PFILESHARINGPROJ0000000000";
                handshakeMessage += keySet[i];
                client.sendMessage(handshakeMessage);
                clients[i][j] = client;
            }
            System.out.println("End of Clients for " + keySet[i] );

        }

        /*
         * This for loop is responsible for creating peer objects which
         * will contain all the necessary information that our Peer-2-Peer
         * network will be utilizing
         */
        for (int i = 0; i < keySet.length; i++) {
            peersOnline.add(new Peer(1001 + i, PeerInfo, CommonInfo, servers[i], clients[i]));
        }

        /*
         * To-do list
         * TODO: Zeroth, Work on implement logging for different functions
         * TODO: First, timers et al.
         * TODO: Second, work on messages
         * TODO: Third, housekeeping et al.
         * TODO: Fourth, paths or smthg
         * TODO: Fifth, Comment everything!
         * TODO: Sixth, think of good preguntas???
         *
         * Ouvuvuevuevue Enyetuenwevue Ugbemugbem Osas
         */
    }
}
