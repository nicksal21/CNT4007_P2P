package main.java;
// Bullock, Peltekis, & Salazar
// Imports
import java.io.File;
import java.util.Arrays;
import java.util.Scanner;
import java.util.LinkedHashMap;
import java.util.Set;

class Project {
    // Read in PeerInfo.cfg
    public static LinkedHashMap readPeerInfo(String path) {
        LinkedHashMap<Integer, String[]> lhm = new LinkedHashMap<>();
        try {
            // Create input stream; read first byte
            File file = new File(path);
            Scanner input = new Scanner(file);

            //Create the Map
            while(input.hasNextLine()) {
                int key;
                String[] term = new String[3];
                String line = input.nextLine();
                String[] lineArr = line.split(" ");
                key = Integer.parseInt(lineArr[0]);
                term[0] = lineArr[1];
                term[1] = lineArr[2];
                term[2] = lineArr[3];
                lhm.put(key, term);
            }
        }
        catch(Exception e) {
            // Scream!
            System.out.println("REEEEEEEE!!!  IT NO EXIST!!!  FEED ME PROPER PATH!!!");
            e.getStackTrace();
        }

        return lhm;
    }

    public static void main(String[]args){
        //Setup scanner for user input
        Scanner userInput = new Scanner(System.in);

        //User inputs the path to the common.cfg
        String path = userInput.next();

        //Read PeerInfo files
        LinkedHashMap<Integer, String[]> PeerInfo  = readPeerInfo(path);
        /*           PeerInfo Legend
        Key = the peer ID
        Map holds term array which is
            0. computer's name
            1. port number
            2. If it has the complete file
         */

        Set<Integer> keySet = PeerInfo.keySet();
        System.out.println(keySet);
        System.out.println(PeerInfo.get(1001));
        System.out.println(PeerInfo.get(1004));


        for(int i = 0; i <= 8; i++) {
            int temp = 1001 + i;
            System.out.println(Arrays.toString(PeerInfo.get(temp)));
        }
    }
}

