package de.mpg.mpdl.labcam.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 12/14/15.
 */

@Table(name = "Tasks")
public class Task extends Model{

    @Expose
    @Column(name = "taskName")
    private String taskName;

    @Expose
    @Column(name = "state")
    private String state;

    @Expose
    @Column(name = "errorLevel")
    private String errorLevel;

    @Expose
    @Column(name = "startDate")
    private String startDate;

    @Expose
    @Column(name = "endDate")
    private long endDate;

    @Expose
    @Column(name = "userId")
    private String userId;

    @Expose
    @Column(name = "userName")
    private String userName;

    @Expose
    @Column(name = "apiKey")
    private String apiKey;

    @Expose
    @Column(name = "collectionId")
    private String collectionId;

    @Expose
    @Column(name = "collectionName")
    private String collectionName;

    @Expose
    @Column(name = "finishedItems")
    private int finishedItems;

    @Expose
    @Column(name = "totalItems")
    private int totalItems;

    @Expose
    @Column(name = "uploadMode")
    private String uploadMode;

    @Expose
    @Column(name = "serverName")
    private String serverName;

    @Expose
    @Column(name = "logs")
    private String logs;

    @Expose
    @Column(name = "imagePaths")
    List<String> imagePaths = new ArrayList<String>();

    public Task() {
        super();
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public void setEndDate(long endDate) {
        this.endDate = endDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public int getFinishedItems() {
        return finishedItems;
    }

    public void setFinishedItems(int finishedItems) {
        this.finishedItems = finishedItems;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public String getUploadMode() {
        return uploadMode;
    }

    public void setUploadMode(String uploadMode) {
        this.uploadMode = uploadMode;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

    public List<String> getImagePaths() {
        return imagePaths;
    }

    public void setImagePaths(List<String> imageIds) {
        this.imagePaths = imageIds;
    }

}
