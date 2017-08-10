package de.mpg.mpdl.labcam.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 11/28/16.
 */
@Table(name = "Voices")
public class Voice extends Model {

    @Expose
    @Column(name = "voicePath")
    private String voicePath;

    @Expose
    @Column(name = "voiceName")
    private String voiceName;

    @Expose
    @Column(name = "createTime")
    private String createTime;

    @Expose
    @Column(name = "serverName")
    private String serverName;

    @Expose
    @Column(name = "userId")
    private String userId;

    @Expose
    @Column(name = "imageIds")
    List<String> imageIds = new ArrayList<String>();

    public Voice() {
        super();
    }

    public Voice(String voicePath, String voiceName, String createTime, String serverName, String userId, List imageIds) {
        this.voicePath = voicePath;
        this.voiceName = voiceName;
        this.createTime = createTime;
        this.serverName = serverName;
        this.userId = userId;
        this.imageIds = imageIds;
    }

    public String getVoicePath() {
        return voicePath;
    }

    public void setVoicePath(String voicePath) {
        this.voicePath = voicePath;
    }

    public String getVoiceName() {
        return voiceName;
    }

    public void setVoiceName(String voiceName) {
        this.voiceName = voiceName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }

}
