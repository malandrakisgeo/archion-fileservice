package org.georgemalandrakis.archion.exception;

import org.georgemalandrakis.archion.core.ArchionConstants;
import org.georgemalandrakis.archion.model.FileMetadata;

public class CloudSavingException extends FileException{

    public CloudSavingException(FileMetadata fileMetadata) {
        super(fileMetadata, ArchionConstants.FAILED_UPLOAD_TO_CLOUD_INFO);
    }
}
