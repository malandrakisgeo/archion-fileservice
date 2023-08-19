package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.model.FileMetadata;

public class FileRemovalException extends FileException{
    public FileRemovalException(FileMetadata fileMetadata, String exception) {
        super(fileMetadata, exception);
    }
}
