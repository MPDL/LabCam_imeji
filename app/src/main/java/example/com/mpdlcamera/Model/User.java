package example.com.mpdlcamera.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by allen on 27/08/15.
 */

@Table(name = "User")
public class User extends Model{
    @Column(name = "email")
    private String  email;

    @Column(name = "pass")
    private String password;

    //@Column(name = "image")
    private Blob image;

    @Column(name = "familyName")
    @Expose
    private String  familyName;

    @Column(name = "givenName")
    @Expose
    private String givenName;

    @Column(name = "completeName")
    @Expose
    private String completeName;

    @Expose
    private List<Organization> organizations;

    //@Column(name = "collections")
    private ImejiFolder collections;

    //@Column(name = "items")
    private ArrayList<DataItem> items;


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getCompleteName() {
        return completeName;
    }

    public void setCompleteName(String completeName) {
        this.completeName = completeName;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }

    public ImejiFolder getCollections() {
        return collections;
    }

    public void setCollections(ImejiFolder collections) {
        this.collections = collections;
    }

    public ArrayList<DataItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<DataItem> items) {
        this.items = items;
    }
}
