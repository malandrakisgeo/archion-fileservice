package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.model.FileMetadata;

import java.io.File;

public class FileException extends Exception{
    protected FileMetadata latestMetadata;

    public FileMetadata getLatestMetadata() {
        return latestMetadata;
    }

    public void setLatestMetadata(FileMetadata latestMetadata) {
        this.latestMetadata = latestMetadata;
    }

    public FileException(FileMetadata fileMetadata, String exception){
        super(exception + ": " + fileMetadata.toJSON());
        this.latestMetadata = fileMetadata;
    }

}
