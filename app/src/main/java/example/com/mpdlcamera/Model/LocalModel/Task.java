package example.com.mpdlcamera.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

/**
 * Created by yingli on 12/14/15.
 */

@Table(name = "Tasks")
public class Task extends Model{

    @Expose
    @Column(name = "taskId")
    private String taskId;

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
    @Column(name = "logs")
    private String logs;


    public Task() {
        super();
    }

    public Task(String taskId, String taskName, String state, String errorLevel, String startDate, long endDate, String userId, String userName, String apiKey, String collectionId, String collectionName, int finishedItems, int totalItems, String uploadMode, String logs) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.state = state;
        this.errorLevel = errorLevel;
        this.startDate = startDate;
        this.endDate = endDate;
        this.userId = userId;
        this.userName = userName;
        this.apiKey = apiKey;
        this.collectionId = collectionId;
        this.collectionName = collectionName;
        this.finishedItems = finishedItems;
        this.totalItems = totalItems;
        this.uploadMode = uploadMode;
        this.logs = logs;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }

}
