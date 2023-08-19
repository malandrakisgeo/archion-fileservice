package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.core.ArchionConstants;
import org.georgemalandrakis.archion.model.FileMetadata;

public class LocalSavingException extends FileException {

    public LocalSavingException(FileMetadata fileMetadata) {
        super(fileMetadata, ArchionConstants.ERROR_SAVING_LOCALLY);
    }
}
