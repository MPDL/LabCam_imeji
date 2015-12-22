package example.com.mpdlcamera.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by yingli on 12/21/15.
 */

@Table(name = "Configurations")
public class Configuration extends Model {

    @Column(name = "userEmail")
    private String userEmail;

    @Column(name = "backupOption")
    private String backupOption;

    @Column(name = "logoutAfterUpload")
    private boolean logoutAfterUpload;

    @Column(name = "removePhotos")
    private boolean removePhotos;

//  here store Id or path?
    @Column(name = "remoteFolderId")
    private String remoteFolderId;

    @Column(name = "localFolderId")
    private String localFolderId;

    @Column(name = "activateTime")
    private long activateTime;


    public Configuration() {
        super();
    }


    public Configuration(String userEmail, String backupOption, boolean logoutAfterUpload, boolean removePhotos, String remoteFolderId, String localFolderId, long activateTime) {
        this.userEmail = userEmail;
        this.backupOption = backupOption;
        this.logoutAfterUpload = logoutAfterUpload;
        this.removePhotos = removePhotos;
        this.remoteFolderId = remoteFolderId;
        this.localFolderId = localFolderId;
        this.activateTime = activateTime;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getBackupOption() {
        return backupOption;
    }

    public void setBackupOption(String backupOption) {
        this.backupOption = backupOption;
    }

    public boolean isLogoutAfterUpload() {
        return logoutAfterUpload;
    }

    public void setLogoutAfterUpload(boolean logoutAfterUpload) {
        this.logoutAfterUpload = logoutAfterUpload;
    }

    public boolean isRemovePhotos() {
        return removePhotos;
    }

    public void setRemovePhotos(boolean removePhotos) {
        this.removePhotos = removePhotos;
    }

    public String getRemoteFolderId() {
        return remoteFolderId;
    }

    public void setRemoteFolderId(String remoteFolderId) {
        this.remoteFolderId = remoteFolderId;
    }

    public String getLocalFolderId() {
        return localFolderId;
    }

    public void setLocalFolderId(String localFolderId) {
        this.localFolderId = localFolderId;
    }

    public long getActivateTime() {
        return activateTime;
    }

    public void setActivateTime(long activateTime) {
        this.activateTime = activateTime;
    }
}
