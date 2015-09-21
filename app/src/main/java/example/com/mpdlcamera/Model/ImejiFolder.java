package example.com.mpdlcamera.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by allen on 27/08/15.
 */
@Table(name = "Folder")
public class ImejiFolder extends Model {

    @Expose
    //@Column(name = "ImejiId")
    public String id;

    @Expose
    @Column(name = "title")
    private String title;

    @Expose
    @Column(name = "description")
    private String description;


    @Expose
    //@Column(name = "contributors")
    private List<User> contributors;

    @Expose
    //@Column(name = "profile")
    private ImejiProfile profile;


    @Column(name = "items")
    private List<DataItem> items;

    @Column(name = "coverItemUrl")
    private String coverItemUrl;

//    @Override
//    public String getId() {
//        return id;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }

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

    public List<User> getContributors() {
        return contributors;
    }

    public void setContributors(List<User> contributors) {
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
}
