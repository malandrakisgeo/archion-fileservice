package org.georgemalandrakis.archion.handlers;

import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.exception.LocalSavingException;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;
import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

//@RunWith(PowerMockRunner.class)
public class LocalMachineHandlerTest {

    @Mock
    ConnectionManager connectionManager;


    @InjectMocks
    @Spy
    LocalMachineHandler testObj;

    FileMetadata sampleMetadata;


    @BeforeTest
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(connectionManager.getLocalMachineFolder()).thenReturn("testMachine");
    }

    @BeforeMethod
    public void method() {
        Mockito.reset(connectionManager);
        this.setSampleMetadata();
    }

    @Test
    public void noChangeIfUnsuitable() throws Exception {


        sampleMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_STORED);
        doNothing().when(testObj).fileUtilForTestability(anyString());
        FileMetadata returned = testObj.deleteFileFromLocalMachine(sampleMetadata);
        returned = testObj.deleteFileFromLocalMachine(sampleMetadata);
        assert (!returned.getPhase().equals(FileProcedurePhase.LOCAL_MACHINE_STORED));


        sampleMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_REMOVED);
        returned = testObj.deleteFileFromLocalMachine(sampleMetadata);
        assert (returned.getPhase().equals(FileProcedurePhase.LOCAL_MACHINE_REMOVED));


        sampleMetadata.setPhase(FileProcedurePhase.CLOUD_SERVICE_REMOVED);
        FileMetadata returned2 = testObj.deleteFileFromLocalMachine(sampleMetadata);

        assert (returned2.equals(sampleMetadata));
    }

    @Test()
    public void LocalSaving() throws Exception {
        doNothing().when(testObj).fileUtilForTestability(any(), any());

        sampleMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_STORED);
        testObj.storeFirstTime(sampleMetadata, null);

    }

    @Test(expectedExceptions = LocalSavingException.class)
    public void LocalSavingException() throws Exception {
        doThrow(new IOException()).when(testObj).fileUtilForTestability(any(), any());

        sampleMetadata.setPhase(FileProcedurePhase.LOCAL_MACHINE_STORED);
        testObj.storeFirstTime(sampleMetadata, null);

    }


    private void setSampleMetadata() {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileid(UUID.randomUUID().toString());
        fileMetadata.setUserid(UUID.randomUUID().toString());
        fileMetadata.setFilename("testfile");

        this.sampleMetadata = fileMetadata;
    }


}
