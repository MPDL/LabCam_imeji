package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.annotation.Column;
import com.litesuits.orm.db.annotation.PrimaryKey;
import com.litesuits.orm.db.enums.AssignType;

/**
 * Created by yingli on 3/1/16.
 */
public class Settings {

    @PrimaryKey(AssignType.AUTO_INCREMENT)
    private int id;

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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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
