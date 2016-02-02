package example.com.mpdlcamera.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.google.gson.annotations.Expose;

/**
 * Created by yingli on 2/2/16.
 */
public class LocalUser extends Model {

    @Expose
    @Column(name="email")
    private String email;

    @Expose
    @Column(name="collection_id")
    private String collectionId;

    @Expose
    @Column(name="collection_Name")
    private String collectionName;


    public LocalUser() {
        super();
    }

    public LocalUser(String email, String collectionId, String collectionName) {
        this.email = email;
        this.collectionId = collectionId;
        this.collectionName = collectionName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }
}
