package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.model.FileMetadata;

public class FileRetrievalException extends FileException{

    public FileRetrievalException(FileMetadata fileMetadata, String exception) {
        super(fileMetadata, exception);
    }
}
