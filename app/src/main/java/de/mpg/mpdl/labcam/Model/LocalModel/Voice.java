package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

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
    @Column(name = "voiceId")
    private String voiceId;

    @Expose
    @Column(name = "imageIds")
    List imageIds = new ArrayList();

    public Voice() {
        super();
    }

    public Voice(String voicePath, String voiceName, String createTime, String voiceId, List imageIds) {
        this.voicePath = voicePath;
        this.voiceName = voiceName;
        this.createTime = createTime;
        this.voiceId = voiceId;
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

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }
}
