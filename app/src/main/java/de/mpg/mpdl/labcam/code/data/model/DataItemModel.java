package de.mpg.mpdl.labcam.code.data.model;

import de.mpg.mpdl.labcam.Model.MetaData;

/**
 * Created by yingli on 3/23/17.
 */

public class DataItemModel {

    private String filename;

    private String createdDate;

    private String fileUrl;

    private String webResolutionUrlUrl;

    private String thumbnailUrl;

    private UserModel createdBy;

    private MetaData metadata;

    private String collectionId;

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

    public String getWebResolutionUrlUrl() {
        return webResolutionUrlUrl;
    }

    public void setWebResolutionUrlUrl(String webResolutionUrlUrl) {
        this.webResolutionUrlUrl = webResolutionUrlUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public UserModel getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserModel createdBy) {
        this.createdBy = createdBy;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
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
