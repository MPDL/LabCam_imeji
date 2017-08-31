package de.mpg.mpdl.labcam.Model;

import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import de.mpg.mpdl.labcam.code.data.model.UserModel;

/**
 * Created by allen on 27/08/15.
 */

@Table(name = "DataItem")
public class DataItem {

    @Expose
    @Column(name = "filename")
    private String filename;

    @Expose
    @Column(name = "createdDate")
    private String createdDate;

    @Expose
    @Column(name = "fileUrl")
    private String fileUrl;

    @Expose
    @Column(name = "createdBy")
    private CreatedBy createdBy;

    @Expose
    @Column(name = "mimetype")
    private String mimetype;

    @Expose
    @Column(name = "collectionId")
    private String collectionId;

    @Column(name = "localPath")
    private String localPath;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    public String getMimetype() {
        return mimetype;
    }

    public void setMimetype(String mimetype) {
        this.mimetype = mimetype;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
