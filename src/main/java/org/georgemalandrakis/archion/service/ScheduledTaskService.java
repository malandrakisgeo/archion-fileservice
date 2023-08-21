package org.georgemalandrakis.archion.service;

import org.georgemalandrakis.archion.dao.FileDAO;
import org.georgemalandrakis.archion.handlers.CloudHandler;
import org.georgemalandrakis.archion.handlers.LocalMachineHandler;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;

import java.util.List;

public class ScheduledTaskService {
    FileDAO fileDAO;
    LocalMachineHandler localMachineHandler;
    CloudHandler cloudHandler;

    public ScheduledTaskService(FileDAO fileDao, LocalMachineHandler localMachineHandler, CloudHandler cloudHandler) {
        this.fileDAO = fileDao;
        this.localMachineHandler = localMachineHandler;
        this.cloudHandler = cloudHandler;
    }

    public void runDelete(String filetype) {
        if (fileDAO != null && localMachineHandler != null && cloudHandler != null) {
            List<FileMetadata> old = fileDAO.fetchOldFiles(filetype);
            massDelete(old);
        }
    }

    public void removeDuplicates() {
        if (fileDAO != null && localMachineHandler != null && cloudHandler != null) {
            List<FileMetadata> duplicates = fileDAO.fetchUserDuplicates();
            massDelete(duplicates);
        }
    }

    public void cleanLocalMachine() {
        if (fileDAO != null && localMachineHandler != null && cloudHandler != null) {
            List<FileMetadata> normalFiles = fileDAO.fetchNotAccessedForThreeDays();
            normalFiles.forEach(file -> {
                try {
                    FileMetadata tempMetadata = localMachineHandler.deleteFileFromLocalMachine(file);
                    if (tempMetadata != null) {
                        fileDAO.update(tempMetadata);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    protected void massDelete(List<FileMetadata> toBeRemoved) {
        toBeRemoved.forEach(file -> {
            try {
                remove(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    protected void remove(FileMetadata file) throws Exception {
        FileMetadata tempFileMetadata = file;

        try {
            if (tempFileMetadata.getPhase() == FileProcedurePhase.LOCAL_MACHINE_STORED || tempFileMetadata.getPhase() == FileProcedurePhase.CLOUD_SERVICE_STORED) {
                tempFileMetadata = localMachineHandler.deleteFileFromLocalMachine(tempFileMetadata);
            }

            if (tempFileMetadata.getPhase() == FileProcedurePhase.LOCAL_MACHINE_REMOVED || tempFileMetadata.getPhase() == FileProcedurePhase.CLOUD_SERVICE_STORED) { //If successfully removed or already removed from the machine in the first place
                tempFileMetadata = cloudHandler.removeFileFromCloudService(tempFileMetadata);
            }
        } catch (Exception e) {
            try {
                fileDAO.update(tempFileMetadata);
            } catch (Exception suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
        fileDAO.deleteFileById(tempFileMetadata.getFileid());

    }
}
