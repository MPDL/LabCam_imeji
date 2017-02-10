package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

/**
 * Created by yingli on 3/1/16.
 */
public class Settings extends Model {

    @Expose
    @Column(name="userId")
    private String userId;

    @Column(name = "isAutoUpload")
    private boolean isAutoUpload;

    public Settings() {
    }

    public Settings(String id, boolean isAutoUpload) {
        this.userId = id;
        this.isAutoUpload = isAutoUpload;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isAutoUpload() {
        return isAutoUpload;
    }

    public void setIsAutoUpload(boolean isAutoUpload) {
        this.isAutoUpload = isAutoUpload;
    }
}
