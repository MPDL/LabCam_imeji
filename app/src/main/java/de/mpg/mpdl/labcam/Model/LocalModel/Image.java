package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by yingli on 12/14/15.
 */

@Table(name = "Images")
public class Image extends Model {
    @Expose
    @Column(name = "imageId")
    private String imageId;

    @Expose
    @Column(name = "taskId")
    public String taskId;

    @Expose
    @Column(name = "imageName")
    private String imageName;

    @Expose
    @Column(name = "state")
    private String state;

    @Expose
    @Column(name = "errorLevel")
    private String errorLevel;

    @Expose
    @Column(name = "imagePath")
    private String imagePath;

    @Column(name = "size")
    private String size;

    @Expose
    @Column(name = "createTime")
    private String createTime;

    @Expose
    @Column(name = "latitude")
    private String latitude;

    @Expose
    @Column(name = "longitude")
    private String longitude;

    @Expose
    @Column(name = "logs")
    private String log;

    @Expose
    @Column(name = "noteId")
    private String noteId;

    @Expose
    @Column(name = "voiceId")
    private String voiceId;

    @Expose
    @Column(name = "userId")
    private String userId;

    @Expose
    @Column(name = "severName")
    private String severName;

    public Image() {
        super();
    }

    public Image(String imageId, String taskId, String imageName, String state, String errorLevel, String imagePath, String size, String createTime, String latitude, String longitude, String log, String noteId, String voiceId) {
        this.imageId = imageId;
        this.taskId = taskId;
        this.imageName = imageName;
        this.state = state;
        this.errorLevel = errorLevel;
        this.imagePath = imagePath;
        this.size = size;
        this.createTime = createTime;
        this.latitude = latitude;
        this.longitude = longitude;
        this.log = log;
        this.noteId = noteId;
        this.voiceId = voiceId;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getErrorLeverl() {
        return errorLevel;
    }

    public void setErrorLeverl(String errorLeverl) {
        this.errorLevel = errorLeverl;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getErrorLevel() {
        return errorLevel;
    }

    public void setErrorLevel(String errorLevel) {
        this.errorLevel = errorLevel;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSeverName() {
        return severName;
    }

    public void setSeverName(String severName) {
        this.severName = severName;
    }
}
