package org.georgemalandrakis.archion.handlers;

import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.exception.FileRemovalException;
import org.georgemalandrakis.archion.exception.LocalSavingException;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;
import org.georgemalandrakis.archion.other.FileUtil;

import java.io.*;
import java.nio.file.Files;


public class LocalMachineHandler {
    protected String localFileFolder;

    public LocalMachineHandler(ConnectionManager connectionObject) {
        localFileFolder = connectionObject.getLocalMachineFolder();
    }

    public byte[] retrieveFile(String id) throws Exception {
        File initialFile = new File(this.localFileFolder + id);
        InputStream targetStream = new FileInputStream(initialFile);
        return targetStream.readAllBytes();
    }

    public FileMetadata storeFirstTime(FileMetadata fileMetadata, InputStream data) throws LocalSavingException{
        String fileLocationAndName = this.localFileFolder + fileMetadata.getFileid();

        try {
            File file = FileUtil.createFile(fileLocationAndName);
            OutputStream outStream = new FileOutputStream(file);
            //outStream.write(data.readAllBytes());
           // outStream.flush();
            fileUtilForTestability(outStream, data);
            fileMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_STORED);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw new LocalSavingException(fileMetadata);
        }

        return fileMetadata;
    }

    public FileMetadata deleteFileFromLocalMachine(FileMetadata fileMetadata) throws FileRemovalException {

        if (!(fileMetadata.getPhase() == FileProcedurePhase.LOCAL_MACHINE_STORED || fileMetadata.getPhase() == FileProcedurePhase.CLOUD_SERVICE_STORED)) {
            return fileMetadata;
        }

        String fileLocationAndName = this.localFileFolder + fileMetadata.getFileid();
        try {
            fileUtilForTestability(fileLocationAndName);
            fileMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_REMOVED);

        } catch (Exception e) {
            e.printStackTrace();
            throw new FileRemovalException(fileMetadata, e.getMessage());
        }

        return fileMetadata;
    }

    //The purpose of this function is to make UnitTesting easier. TODO: Make it unnecessary
    protected void fileUtilForTestability(String fileLocationAndName) throws Exception{
        File file = FileUtil.createFile(fileLocationAndName);
        Files.delete(file.toPath());
    }

    //The purpose of this function is to make UnitTesting easier
    protected void fileUtilForTestability(OutputStream outStream, InputStream data) throws Exception{
        outStream.write(data.readAllBytes());
        outStream.flush();
    }



}
