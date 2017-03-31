package de.mpg.mpdl.labcam.code.data.model;

import de.mpg.mpdl.labcam.Model.ImejiProfile;
import de.mpg.mpdl.labcam.Model.MessageModel.Person;

import java.util.List;

/**
 * Created by yingli on 3/17/17.
 */

public class ImejiFolderModel {

    public String id;

    private String title;

    private String description;

    private String createdDate;

    private String modifiedDate;

    private CreatedByModel createdBy;

    private List<Person> contributors;

    private ImejiProfile profile;

    private String coverItemUrl;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CreatedByModel getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedByModel createdBy) {
        this.createdBy = createdBy;
    }

    public List<Person> getContributors() {
        return contributors;
    }

    public void setContributors(List<Person> contributors) {
        this.contributors = contributors;
    }

    public ImejiProfile getProfile() {
        return profile;
    }

    public void setProfile(ImejiProfile profile) {
        this.profile = profile;
    }
//
//    public String getCoverItemUrl() {
//        return coverItemUrl;
//    }
//
//    public void setCoverItemUrl(String coverItemUrl) {
//        this.coverItemUrl = coverItemUrl;
//    }

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

    public String getCoverItemUrl() {
        return coverItemUrl;
    }

    public void setCoverItemUrl(String coverItemUrl) {
        this.coverItemUrl = coverItemUrl;
    }
}
