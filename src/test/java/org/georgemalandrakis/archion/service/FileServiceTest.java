package org.georgemalandrakis.archion.service;

import org.georgemalandrakis.archion.core.*;
import org.georgemalandrakis.archion.dao.FileDAO;
import org.georgemalandrakis.archion.exception.AuthException;
import org.georgemalandrakis.archion.exception.FileRemovalException;
import org.georgemalandrakis.archion.handlers.CloudHandler;
import org.georgemalandrakis.archion.handlers.LocalMachineHandler;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;
import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FileServiceTest {

    @Mock
    ConnectionManager connectionManager;

    @Mock
    CloudHandler cloudHandler;

    @Mock
    FileDAO fileDAO;

    @Mock
    LocalMachineHandler localMachineHandler;

    @InjectMocks
    FileService fileService;

    private ArchionRequest archionRequest;


    @BeforeTest
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getLocalMachineFolder()).thenReturn("testMachine");
    }

    @BeforeMethod
    public void method() {
        Mockito.reset(fileDAO, localMachineHandler, cloudHandler);
    }



    @Test
    public void testDates() throws IOException {
        Calendar cal = Calendar.getInstance();

        //String userid, String purpose, String sha1, String filename, File file
        var flm = fileService.createFileMetadata("user", "test", null, null, File.createTempFile("temp", "pmet"));
        assert (flm.getFiletype().equals(ArchionConstants.FILES_TEST_FILETYPE));

        flm = fileService.createFileMetadata("user", "archive", null, null, File.createTempFile("temp", "pmet"));
        assert (flm.getFiletype().equals(ArchionConstants.FILES_ARCHIVE_FILETYPE));
    }

    @Test(expectedExceptions = AuthException.class)
    public void exception_whenNoRight() throws Exception {
        when(fileDAO.retrieve(any())).thenReturn(this.sampleData());
        fileService.getFileById(UUID.randomUUID().toString(), "Test");


    }

    @Test
    public void removeRunsThrough() throws Exception {
        var id = UUID.randomUUID();
        var file = this.sampleData();
        file.setPhase(FileProcedurePhase.CLOUD_SERVICE_STORED);

        doReturn(file).when(this.fileDAO).retrieve(anyString());
        doReturn(file).when(this.localMachineHandler).deleteFileFromLocalMachine(any());

        fileService.remove(id.toString(), this.sampleUser());
        verify(localMachineHandler, times(1)).deleteFileFromLocalMachine(any());
        verify(cloudHandler, times(1)).removeFileFromCloudService(any());

    }

    @Test(expectedExceptions = FileRemovalException.class)
    public void remove_throwsException_butUpdates() throws Exception {
        var id = UUID.randomUUID();
        var file = this.sampleData();
        file.setPhase(FileProcedurePhase.CLOUD_SERVICE_STORED);
        doReturn(file).when(this.fileDAO).retrieve(anyString());
        doThrow(FileRemovalException.class).when(this.localMachineHandler).deleteFileFromLocalMachine(any());

        fileService.remove(id.toString(), this.sampleUser());

        verify(this.fileDAO, times(1)).update(any());
    }

    @Test()
    public void storeFirstTime_test() throws Exception {

        fileService.storeFirstTime(File.createTempFile("temp", ""), this.sampleData());

        verify(this.cloudHandler, times(1)).uploadFile(any(), any());
        verify(this.fileDAO, times(1)).update(any());

    }

    private FileMetadata mockSuccessfulstoreFile(ArchionRequest archionRequest) {
        FileMetadata fileMetadata = archionRequest.getResponseObject().getLatestValidFileMetadata();
        fileMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_STORED);
        return fileMetadata;
    }

    private FileMetadata mockSuccessfulUploadFile(ArchionRequest archionRequest) {
        FileMetadata fileMetadata = archionRequest.getResponseObject().getLatestValidFileMetadata();
        fileMetadata.setPhase(FileProcedurePhase.CLOUD_SERVICE_STORED);
        return fileMetadata;
    }

    private FileMetadata sampleData() {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileid(UUID.randomUUID().toString());
        fileMetadata.setUserid("George");
        fileMetadata.setFilename("testfile");
        return fileMetadata;
    }

    private ArchionUser sampleUser() {
        ArchionUser arch = new ArchionUser();
        arch.setName("George");
        arch.setEmail("test@test.com");
        arch.setId("George");
        return arch;
    }


    private void setSampleRequest() {
        ArchionRequest archionRequest = new ArchionRequest();

        ArchionUser archionUser = new ArchionUser();
        archionUser.setEmail("malandrakisgeo@gmail.com");
        archionUser.setId(UUID.randomUUID().toString());
        archionUser.setName("George Malandrakis");

        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileid(UUID.randomUUID().toString());
        fileMetadata.setUserid(archionUser.getId());
        fileMetadata.setFilename("testfile");
        archionRequest.getResponseObject().setLatestValidFileMetadata(fileMetadata);

        this.archionRequest = archionRequest;
    }

}
