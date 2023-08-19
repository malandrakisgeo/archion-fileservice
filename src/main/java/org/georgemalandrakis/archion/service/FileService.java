package org.georgemalandrakis.archion.service;

import org.georgemalandrakis.archion.core.ArchionConstants;
import org.georgemalandrakis.archion.core.ArchionUser;
import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.dao.FileDAO;
import org.georgemalandrakis.archion.exception.*;
import org.georgemalandrakis.archion.handlers.CloudHandler;
import org.georgemalandrakis.archion.handlers.LocalMachineHandler;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;
import org.georgemalandrakis.archion.other.FileUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static org.georgemalandrakis.archion.other.FileUtil.getFileExtensionFromFileName;

public class FileService {

    private final FileDAO fileDao;
    private final CloudHandler cloudHandler;
    private final LocalMachineHandler localMachineHandler;

    public FileService(ConnectionManager connectionObject, FileDAO fileDao, CloudHandler cloudHandler, LocalMachineHandler machineHandler) {
        this.fileDao = fileDao;
        this.cloudHandler = cloudHandler;
        this.localMachineHandler = machineHandler;
    }

    public byte[] getFileById(String fileId, String userId) throws Exception {
        FileMetadata metadata = fileDao.retrieve(fileId);
        if (metadata == null) {
            return null;
        }
        if (metadata.getUserid().equals(userId)) {
            return getFile(metadata);
        } else {
            throw new AuthException();
        }

    }


    public FileMetadata createNewFile(String userid, String purpose, String filename, File file) throws Exception {
        String sha1 = FileUtil.calculate_SHA1(file);

        if (!purpose.contentEquals("test") && fileDao.file_exists(userid, sha1)) { //duplicates allowed when testing.
            throw new FileExistsException(new FileMetadata());
        }
        FileMetadata fileMetadata = createFileMetadata(userid, purpose, sha1, filename, file);

        fileDao.create(fileMetadata);//We store the metadata in the DB first.
        storeFirstTime(file, fileMetadata);//Stores the file in the local machine & the cloud thereafter.

        return fileMetadata;
    }

    public List<FileMetadata> retrieveList(String userId) {
        return fileDao.retrieveList(userId);
    }


    public FileMetadata getUpdatedMetadata(String fileId, ArchionUser user) throws Exception {
        var metadata = fileDao.retrieve(fileId);
        if (metadata == null) {
            throw new FileNotFoundException();
        }
        if (!(user.getRoles().contains("ADMIN") || metadata.getUserid().equals(user.getId()))) {
            throw new AuthException();
        }
        return metadata;
    }


    public void remove(String id, ArchionUser user) throws Exception {
        FileMetadata tempFileMetadata = getUpdatedMetadata(id, user);

        try {
            localMachineHandler.deleteFileFromLocalMachine(tempFileMetadata);
            cloudHandler.removeFileFromCloudService(tempFileMetadata);
        } catch (Exception e) {
            try {
                fileDao.update(tempFileMetadata);
            } catch (Exception suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }

        fileDao.deleteFileById(tempFileMetadata.getFileid());
    }


    protected FileMetadata storeFirstTime(File file, FileMetadata fileMetadata) throws Exception {

        try (FileInputStream fileinputStream = FileUtil.createFileInputStream(file)) {
            localMachineHandler.storeFirstTime(fileMetadata, fileinputStream); //will throw exception if unsuccessful.
            cloudHandler.uploadFile(fileMetadata, fileinputStream);

        } catch (FileException e) {
            throw e;
        } finally {
            fileMetadata.setLastmodified(new Timestamp(System.currentTimeMillis()));//The metadata was modified in the storeFile() function.
            fileMetadata.setSha1Hash(null); //Otherwise the user will not be able to re-create the file (FileExistsException)
            fileDao.update(fileMetadata);
        }
        return fileMetadata;
    }


    protected FileMetadata createFileMetadata(String userid, String purpose, String sha1, String filename, File file) throws IOException {
        FileMetadata fileMetadata = new FileMetadata();
        // Calendar cal = Calendar.getInstance();

        fileMetadata.setFilename(filename);
        fileMetadata.setFileid(UUID.randomUUID().toString());
        fileMetadata.setSizeinkbs(String.valueOf(Files.size(file.toPath()) / 1024));
        fileMetadata.setUserid(userid);
        fileMetadata.setSha1Hash(sha1); //Becomes null if something goes wrong when saving locally, otherwise the user will not be able to re-create the file.

        Timestamp creationandFirstAccess = new Timestamp(System.currentTimeMillis());
        fileMetadata.setCreated(creationandFirstAccess);
        fileMetadata.setLastAccessed(creationandFirstAccess);
        fileMetadata.setLastmodified(creationandFirstAccess);
        fileMetadata.setFileextension(getFileExtensionFromFileName(fileMetadata.getFilename()));
        fileMetadata.setPhase(FileProcedurePhase.DB_METADATA_STORED);

        switch (purpose) {
            case "temporary": //Temporary files are forcibly deleted after a month by default
                fileMetadata.setFilename(file.getName() + ".temp");
                //cal.add(Calendar.MONTH, 1);
                fileMetadata.setFiletype(ArchionConstants.FILES_TEMP_FILETYPE);
                break;

            case "archive": //Files marked as "archive" can only be manually deleted.
                fileMetadata.setFiletype(ArchionConstants.FILES_ARCHIVE_FILETYPE);
                break;

            case "test": //Test files are deleted after 5 minutes
                //cal.add(Calendar.MINUTE, 5);
                fileMetadata.setFiletype(ArchionConstants.FILES_TEST_FILETYPE);
                break;

            case "save": //To be deleted in a year, if they are not used again in the meanwhile.
            default:
                // cal.add(Calendar.YEAR, 1);
                fileMetadata.setFiletype(ArchionConstants.FILES_DEFAULT_FILETYPE);
                break;
        }

        return fileMetadata;

    }

    public byte[] getFile(FileMetadata fileMetadata) throws FileRetrievalException { //FileMetadata assumed up to date
        byte[] file = null;
        fileMetadata.setLastAccessed(new Timestamp(System.currentTimeMillis()));

        try {
            if (fileMetadata.getPhase().equals(FileProcedurePhase.LOCAL_MACHINE_STORED) || fileMetadata.getPhase().equals(FileProcedurePhase.CLOUD_SERVICE_STORED)) {
                file = localMachineHandler.retrieveFile(fileMetadata.getFileid()); //Retrieve from local machine if recently accessed.
            } else if (fileMetadata.getPhase().equals(FileProcedurePhase.LOCAL_MACHINE_REMOVED)) {
                file = cloudHandler.downloadFile(fileMetadata.getFileid());
                resaveInLocalMachine(fileMetadata, file); //User need not be notified if something goes wrong here.
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new FileRetrievalException(fileMetadata, e.getMessage());
        } finally {
            fileDao.updateLastAccessed(fileMetadata.getFileid());
            //NOTE: The last_accessed is updated whether the file was successfully retrieved or not. We only care that someone requested it.
        }
        return file;

    }

    protected FileMetadata resaveInLocalMachine(FileMetadata fileMetadata, byte[] filebytes) {
        //TODO: Implement. If something goes wrong, we just return the FileMetadata as it is. The user does not need to know.
        fileMetadata.setPhase(FileProcedurePhase.CLOUD_SERVICE_STORED); //CLOUD SERVICE STORED = STORED IN BOTH THE CLOUD SERVICE AND THE LOCAL MACHINE.
        return fileMetadata;
    }

}
