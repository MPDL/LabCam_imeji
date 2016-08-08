package de.mpg.mpdl.labcam.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.List;

import de.mpg.mpdl.labcam.Model.MessageModel.Person;

/**
 * Created by allen on 27/08/15.
 */
@Table(name = "Folder")
public class ImejiFolder extends Model {

    @Expose
    //@Column(name = "ImejiId")
    public String id;

    //very very confusing here, ImejiId = folder.id
    @Column(name = "ImejiId")
    private String ImejiId;

    @Expose
    @Column(name = "title")
    private String title;

    @Expose
    @Column(name = "description")
    private String description;


    @Expose
    @Column(name = "modifiedDate")
    private String modifiedDate;

    @Expose
    //@Column(name = "contributors")
    private List<Person> contributors;

    @Expose
    //@Column(name = "profile")
    private ImejiProfile profile;


//    @Column(name = "items")
    private List<DataItem> items;

    @Column(name = "coverItemUrl")
    private String coverItemUrl;


    public String getImejiId() {
        return ImejiId;
    }

    public void setImejiId(String imejiId) {
        ImejiId = imejiId;
    }

    public List<DataItem> getItems() {
        return items;
    }

    public void setItems(List<DataItem> items) {
        this.items = items;
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

    public String getCoverItemUrl() {
        return coverItemUrl;
    }

    public void setCoverItemUrl(String coverItemUrl) {
        this.coverItemUrl = coverItemUrl;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
