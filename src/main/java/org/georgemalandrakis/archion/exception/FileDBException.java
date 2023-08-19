package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.model.FileMetadata;

public class FileDBException extends FileException {

    public FileDBException(FileMetadata fileMetadata){
        super(fileMetadata, "Could not save file to database.");
    }

}
