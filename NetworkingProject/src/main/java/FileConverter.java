package main.java;

import java.io.*;
import java.util.LinkedHashMap;

public class FileConverter {

    //https://stackoverflow.com/questions/858980/file-to-byte-in-java
    public static byte[][] fileToByte(String fPath, LinkedHashMap<String, String> common) throws IOException {
        FileInputStream fileCon = new FileInputStream(fPath);
        int PieceSize = Integer.parseInt(common.get("PieceSize"));
        int fileSize = Integer.parseInt(common.get("FileSize"));
        byte[] fileCont = fileCon.readAllBytes();
        byte[][] bInF = new byte[(int) Math.ceil((double) fileSize / PieceSize)][PieceSize];
        int j = 0;
        for (int i = 0; i < fileSize; i++) {
            bInF[(int) Math.floor((double) i / PieceSize)][i % PieceSize] = fileCont[i];
            j = i;
        }
        System.out.println(j);
        fileCon.close();
        return bInF;
    }

    // owo
    public static void byteToFile(byte[][] fBytArray, String Dir, String saveLoc, LinkedHashMap<String, String> common) throws IOException {
        int PieceSize = Integer.parseInt(common.get("PieceSize"));
        int fileSize = Integer.parseInt(common.get("FileSize"));
        byte[] fBytes = new byte[fileSize];
        for (int i = 0; i < fileSize; i++)
            //
            fBytes[i] = fBytArray[(int) Math.floor((double) i / PieceSize)][i % PieceSize];


        File Directory = new File(Dir);
        if(!Directory.exists()){
            Directory.mkdir();
        }


        try (FileOutputStream byteToF = new FileOutputStream(saveLoc)) {

            byteToF.write(fBytes);
            byteToF.close();
        } catch (Exception e) {
            System.out.println("Unable to write bytes to File");
        }
    }


}