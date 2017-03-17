package de.mpg.mpdl.labcam.code.data.model;

/**
 * Created by yingli on 3/17/17.
 */

public class ImejiFolderModel {

    private String ImejiId;

    private String title;

    private String description;

    private String createdDate;

    private String modifiedDate;

    public String getImejiId() {
        return ImejiId;
    }

    public void setImejiId(String imejiId) {
        ImejiId = imejiId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
