package org.georgemalandrakis.archion.other;

import org.apache.commons.codec.digest.DigestUtils;
import org.georgemalandrakis.archion.core.ArchionRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.util.zip.CRC32;

public class FileUtil {

    public static String getFileExtensionFromFileName(String fileName) {
        if(fileName == null){
            return null;
        }
        String fileExtension = "";

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            fileExtension = fileName.substring(i + 1);
        }

        return fileExtension;
    }


    public static String calculate_SHA1(File file) throws Exception {
        /*MessageDigest digest = MessageDigest.getInstance("SHA-1");
        int n = 0;
        byte[] buffer = new byte[8192]; //Returns different digest from DigestUtils.
        //Perhaps because of data overhead.
        while (n != -1) {
            n = fis.read(buffer);
            if (n > 0) {
                digest.update(buffer, 0, n);
            }
        }*/

        if(file == null){
            return null;
        }
        FileInputStream fis = new FileInputStream(file);
        return DigestUtils.sha1Hex(fis);
    }


    public static String calculateCRC32(File file){
        if(file == null){
            return null;
        }
        CRC32 crc = new CRC32();

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            crc.update(fileInputStream.readAllBytes());
        } catch (Exception e ) {
            e.printStackTrace();
        }

        return crc.toString();
    }

    //Needed mainly for testability, since Mockito cannot mock a local object of a function
    // //TODO: Something about that
    public static File createFile(String filelocation){
        return new File(filelocation);
    }

    //Needed mainly for testability, since Mockito cannot mock a local object of a function
    // //TODO: Something about that
    public static FileInputStream createFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }
}
