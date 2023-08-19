package org.georgemalandrakis.archion.service;

import org.georgemalandrakis.archion.dao.FileDAO;
import org.georgemalandrakis.archion.exception.FileRemovalException;
import org.georgemalandrakis.archion.handlers.CloudHandler;
import org.georgemalandrakis.archion.handlers.LocalMachineHandler;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScheduledTaskServiceTest {


    @Mock
    CloudHandler cloudHandler;

    @Mock
    FileDAO fileDAO;

    @Mock
    LocalMachineHandler localMachineHandler;

    @InjectMocks
    ScheduledTaskService scheduledTaskService;


    @BeforeTest
    public void setUp(){
        MockitoAnnotations.openMocks(this);
    }


    @Test(expectedExceptions = FileRemovalException.class)
    public void throws_FileDeletionException_if_deleteUnsuccessful() throws Exception{
        when(localMachineHandler.deleteFileFromLocalMachine(any())).thenThrow(FileRemovalException.class);
        scheduledTaskService.remove(this.normalFiles(FileProcedurePhase.LOCAL_MACHINE_STORED).get(0));
        verify(fileDAO, times(1)).update(any());

    }

    @Test
    public void localMachineHandler_willNotRun_ifFileNotInLocalMachine() throws Exception{
        when(cloudHandler.removeFileFromCloudService(any())).thenReturn(this.normalFiles(FileProcedurePhase.CLOUD_SERVICE_REMOVED).get(0));

        scheduledTaskService.remove(this.normalFiles(FileProcedurePhase.LOCAL_MACHINE_REMOVED).get(0));
        Mockito.verify(localMachineHandler, times(0)).deleteFileFromLocalMachine(any());

    }


    List<FileMetadata> normalFiles(FileProcedurePhase fileProcedurePhase){
        FileMetadata file1 = new FileMetadata();
        FileMetadata file2 = new FileMetadata();

        file1.setFileid(UUID.randomUUID().toString());
        file1.setFilename("filename");
        file1.setPhase(fileProcedurePhase);

        file2.setFileid(UUID.randomUUID().toString());
        file2.setFilename("filename2");
        file2.setPhase(fileProcedurePhase);

        List<FileMetadata> fileMetadataList = new ArrayList<>();
        fileMetadataList.add(file1);
        fileMetadataList.add(file2);

        return fileMetadataList;
    }

}
