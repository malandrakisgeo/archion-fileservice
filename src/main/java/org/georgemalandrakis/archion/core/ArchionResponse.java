package org.georgemalandrakis.archion.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.georgemalandrakis.archion.model.FileMetadata;

import java.util.ArrayList;
import java.util.List;

public class ArchionResponse {
    @JsonProperty("Data")
    private Object data;

    @JsonProperty("Notification")
    private List<ArchionNotification> notification;

    @JsonProperty("latestValidFileMetadata")
    private FileMetadata latestValidMetadata; //Null on deletion or error!

    @JsonProperty("Error")
    private String errorMessage; //Null on deletion or error!

    @JsonIgnore
    private boolean hasError;

    public ArchionResponse() {
        this.hasError = false;
        this.notification = new ArrayList<>();
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<ArchionNotification> getNotification() {
        return this.notification;
    }

    public void setNotification(List<ArchionNotification> notification) {
        this.notification = notification;
    }

    public Boolean hasNotification() {
        return (this.notification.size() > 0 ? true : false);
    }


    public Boolean hasType(ArchionNotification.NotificationType type) { //TODO: See if we really need it or if hasError is just fine.
        if (type == ArchionNotification.NotificationType.error && hasError) { //skip the for-loop
            return true;
        }
        for (ArchionNotification archionNotification : this.getNotification()) {
            if (archionNotification.getType() == type) {
                return true;
            }
        }
        return false;
    }

    public void addError(String name, String message) {
        this.hasError = true;
        this.latestValidMetadata = null;
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.error, name, message);
        this.getNotification().add(archionNotification);
    }

    public void addError(String name, String message, Integer index) {
        this.hasError = true;
        this.latestValidMetadata = null;
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.error, name, message, index);
        this.getNotification().add(archionNotification);
    }

    public void addDebug(String name, String message) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.debug, name, message);
        this.getNotification().add(archionNotification);
    }

    public void addDebug(String name, String message, Integer index) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.debug, name, message, index);
        this.getNotification().add(archionNotification);
    }

    public void addSuccess(String name, String message) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.success, name, message);
        this.getNotification().add(archionNotification);
    }

    public void addSuccess(String name, String message, Integer index) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.success, name, message, index);
        this.getNotification().add(archionNotification);
    }

    public void addWarning(String name, String message) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.warning, name, message);
        this.getNotification().add(archionNotification);
    }

    public void addWarning(String name, String message, Integer index) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.warning, name, message, index);
        this.getNotification().add(archionNotification);
    }

    public void addInformation(String name, String message) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.info, name, message);
        this.getNotification().add(archionNotification);
    }

    public void addInformation(String name, String message, Integer index) {
        ArchionNotification archionNotification = new ArchionNotification(ArchionNotification.NotificationType.info, name, message, index);
        this.getNotification().add(archionNotification);
    }

    public boolean hasError() {
        return hasError;
    }

    public FileMetadata getLatestValidFileMetadata() {
        return latestValidMetadata;
    }

    public void setLatestValidFileMetadata(FileMetadata fileMetadata) {
        this.latestValidMetadata = fileMetadata;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
