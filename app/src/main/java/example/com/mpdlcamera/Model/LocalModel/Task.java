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
    @Column(name = "startTime")
    private String startTime;

    @Expose
    @Column(name = "finishTime")
    private String finishTime;

    @Expose
    @Column(name = "uploadMode")
    private String uploadMode;

    @Expose
    @Column(name = "fromFolder")
    private String fromFolder;

    @Expose
    @Column(name = "toFolder")
    private String toFolder;

    @Expose
    @Column(name = "userName")
    private String userName;


    public Task(String userName, String taskId, String startTime, String finishTime, String uploadMode, String fromFolder, String toFolder) {
        this.userName = userName;
        this.taskId = taskId;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.uploadMode = uploadMode;
        this.fromFolder = fromFolder;
        this.toFolder = toFolder;
    }

    public Task() {
        super();
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }

    public String getUploadMode() {
        return uploadMode;
    }

    public void setUploadMode(String uploadMode) {
        this.uploadMode = uploadMode;
    }

    public String getFromFolder() {
        return fromFolder;
    }

    public void setFromFolder(String fromFolder) {
        this.fromFolder = fromFolder;
    }

    public String getToFolder() {
        return toFolder;
    }

    public void setToFolder(String toFolder) {
        this.toFolder = toFolder;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
