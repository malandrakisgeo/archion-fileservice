package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.core.ArchionConstants;
import org.georgemalandrakis.archion.model.FileMetadata;

public class FileExistsException extends FileException{
    public FileExistsException(FileMetadata fileMetadata) {
        super(fileMetadata, ArchionConstants.FILE_ALREADY_EXISTS);
    }


}
