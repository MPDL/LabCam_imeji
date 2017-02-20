package de.mpg.mpdl.labcam.code.data.model;

/**
 * Created by yingli on 8/14/16.
 */

public class ApplicationInfoModel {

    private long applicationInfoId;

    private String latestIOSVersion;

    private boolean iOSUpdateMandatory;

    private String iOSUpdateNote;

    private String latestAndroidVersion;

    private boolean androidUpdateMandatory;

    private String androidUpdateNote;

    private long lastModified;

    private String entityStatus;

    public long getApplicationInfoId() {
        return applicationInfoId;
    }

    public void setApplicationInfoId(long applicationInfoId) {
        this.applicationInfoId = applicationInfoId;
    }

    public String getLatestIOSVersion() {
        return latestIOSVersion;
    }

    public void setLatestIOSVersion(String latestIOSVersion) {
        this.latestIOSVersion = latestIOSVersion;
    }

    public boolean isiOSUpdateMandatory() {
        return iOSUpdateMandatory;
    }

    public void setiOSUpdateMandatory(boolean iOSUpdateMandatory) {
        this.iOSUpdateMandatory = iOSUpdateMandatory;
    }

    public String getiOSUpdateNote() {
        return iOSUpdateNote;
    }

    public void setiOSUpdateNote(String iOSUpdateNote) {
        this.iOSUpdateNote = iOSUpdateNote;
    }

    public String getLatestAndroidVersion() {
        return latestAndroidVersion;
    }

    public void setLatestAndroidVersion(String latestAndroidVersion) {
        this.latestAndroidVersion = latestAndroidVersion;
    }

    public boolean isAndroidUpdateMandatory() {
        return androidUpdateMandatory;
    }

    public void setAndroidUpdateMandatory(boolean androidUpdateMandatory) {
        this.androidUpdateMandatory = androidUpdateMandatory;
    }

    public String getAndroidUpdateNote() {
        return androidUpdateNote;
    }

    public void setAndroidUpdateNote(String androidUpdateNote) {
        this.androidUpdateNote = androidUpdateNote;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(String entityStatus) {
        this.entityStatus = entityStatus;
    }
}
