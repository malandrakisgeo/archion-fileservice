package org.georgemalandrakis.archion.handlers;


import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.georgemalandrakis.archion.core.ConnectionManager;
import org.georgemalandrakis.archion.exception.CloudSavingException;
import org.georgemalandrakis.archion.exception.FileException;
import org.georgemalandrakis.archion.exception.FileRemovalException;
import org.georgemalandrakis.archion.model.FileMetadata;
import org.georgemalandrakis.archion.model.FileProcedurePhase;

import java.io.*;

public class CloudHandler {

    private final String bucketName = "myFiles";

    private final AmazonS3 sClient;


    public CloudHandler(ConnectionManager connectionObject) {
        String amazonaccesskey = connectionObject.getAmazonAccesskey();
        String amazonsecretkey = connectionObject.getAmazonSecretkey();
        AWSCredentials credentials = new BasicAWSCredentials(amazonaccesskey, amazonsecretkey);
        Regions clientRegion = Regions.EU_NORTH_1;
        sClient = AmazonS3ClientBuilder.standard().withRegion(clientRegion).withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
    }


    public byte[] downloadFile(String fileid) throws Exception {
        //S3Object fullObject = sClient.getObject(new GetObjectRequest(bucketName, fileid));
        //return IOUtils.toByteArray(fullObject.getObjectContent());
        return null;
    }


    public FileMetadata uploadFile(FileMetadata fileMetadata, InputStream inputStream) throws FileException {
        //ObjectMetadata metadata = new ObjectMetadata();  //TODO: fill with data

        if (inputStream != null) {
            try {
                //sClient.putObject(bucketName, filename, inputStream, metadata); //TODO: Uncomment
                fileMetadata.setPhase(FileProcedurePhase.CLOUD_SERVICE_STORED);
                return fileMetadata;
            } catch (Exception e) {
                e.printStackTrace();
                throw new CloudSavingException(fileMetadata);
            }
        }

        return fileMetadata;
    }


    public FileMetadata removeFileFromCloudService(FileMetadata fileMetadata) throws FileRemovalException {
        if (fileMetadata != null) {
            if (!(fileMetadata.getPhase() == FileProcedurePhase.LOCAL_MACHINE_REMOVED || fileMetadata.getPhase() == FileProcedurePhase.CLOUD_SERVICE_STORED)) {
                return fileMetadata;
            }

            try {
                //sClient.deleteObject(bucketName,fileMetadata.getFileid());
                fileMetadata.setPhase(FileProcedurePhase.CLOUD_SERVICE_REMOVED);
            } catch (Exception e) {
                e.printStackTrace();
                throw new FileRemovalException(fileMetadata, e.getMessage());
            }
        }
        return fileMetadata;
    }


}
