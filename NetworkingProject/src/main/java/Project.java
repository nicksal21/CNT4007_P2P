package main.java;
// Bullock, Peltekis, & Salazar

// Imports

import java.io.File;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

class Project {
    // Read PeerInfo.cfg data into a LinkedHashMap
    public static LinkedHashMap<Integer, String[]> readPeerInfo(String path) {
        LinkedHashMap<Integer, String[]> lhm = new LinkedHashMap<>();
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

    // Read Common.cfg data into a LinkedHashMap
    public static LinkedHashMap<String, Integer> readCommon(String path) {
        LinkedHashMap<String, Integer> lhm = new LinkedHashMap<>();
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
                int term;
                String line = input.nextLine();
                String[] lineArr = line.split(" ");

                // Establish Map Entry
                key = lineArr[1];
                term = Integer.parseInt(lineArr[1]);
                lhm.put(key, term);
            }
        } catch (Exception e) {
            // Error handling
            System.out.println("Error!  Pathway does not exist!");
            e.getStackTrace();
        }
        return lhm;
    }

    public static void main(String[] args) {
        // Setup scanner for user input
        Scanner userInput = new Scanner(System.in);

        // User inputs the path to the PeerInfo.cfg
        String path = userInput.next();

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
        path = userInput.next();

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
        LinkedHashMap<String, Integer> CommonInfo = readCommon(path);

        Set<Integer> keySet = PeerInfo.keySet();
        System.out.println(keySet);

        // Testing sample values
        System.out.println(Arrays.toString(PeerInfo.get(1001)));
        System.out.println(Arrays.toString(PeerInfo.get(1004)));

        // Test Print
        for (int i = 0; i <= 8; i++) {
            int temp = 1001 + i;
            System.out.println(Arrays.toString(PeerInfo.get(temp)));
        }

        /*
         * To-do list
         * TODO: Zeroth, FINISH CLIENT DESIGN
         * TODO: First, find a way to connect all peers to each other
         * TODO: Second, Make a holder for the peer object to hold connects / keep track of the connections
         * TODO: Third, Make an evaluator for information quality
         * TODO: Fourth, Test Cases.....
         * TODO: Fifth,  Comment the SH*T out everything
         * TODO: Sixth, StartRemotePeer???
         */
    }
}

