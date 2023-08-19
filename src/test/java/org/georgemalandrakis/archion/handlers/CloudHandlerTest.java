package org.georgemalandrakis.archion.handlers;

import org.checkerframework.checker.units.qual.C;
import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;
import org.mockito.*;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.File;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class CloudHandlerTest {

    @Mock
    ConnectionManager connectionManager;

    @Spy
    CloudHandler testObj;

    FileMetadata sampleMetadata;

    @BeforeTest
    public void setUp() {
        connectionManager = Mockito.mock(ConnectionManager.class);
        when(connectionManager.getAmazonSecretkey()).thenReturn("key");
        when(connectionManager.getAmazonAccesskey()).thenReturn("key");
        testObj = new CloudHandler(connectionManager);
    }

    @BeforeMethod
    public void method() {
        this.setSampleMetadata();
    }

    @Test
    public void removeFileFromCloudService_runs() throws Exception{
        sampleMetadata.setPhase(FileProcedurePhase.CLOUD_SERVICE_STORED);
        testObj.removeFileFromCloudService(sampleMetadata);
        assert (sampleMetadata.getPhase().equals(FileProcedurePhase.CLOUD_SERVICE_REMOVED));

    }

    private void setSampleMetadata() {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFileid(UUID.randomUUID().toString());
        fileMetadata.setUserid(UUID.randomUUID().toString());
        fileMetadata.setFilename("testfile");

        this.sampleMetadata = fileMetadata;
    }


}
