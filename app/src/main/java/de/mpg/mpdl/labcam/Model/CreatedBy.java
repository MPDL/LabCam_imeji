package de.mpg.mpdl.labcam.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.google.gson.annotations.Expose;

/**
 * Created by yingli on 9/26/16.
 */

public class CreatedBy extends Model {

    @Expose
    @Column(name = "fullname")
    private String fullname;

    @Expose
    @Column(name = "userId")
    private String userId;

    public CreatedBy(String fullname, String userId) {
        this.fullname = fullname;
        this.userId = userId;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
