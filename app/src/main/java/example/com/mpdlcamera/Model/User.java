package example.com.mpdlcamera.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.MessageModel.Person;

/**
 * Created by allen on 27/08/15.
 */

@Table(name = "User")
public class User extends Model{

    @Expose
    @Column(name="email")
    private String email;

    @Expose
    @Column(name="quota")
    private long quota;

    @Expose
    @Column(name="apiKey")
    private String apiKey;

    @Expose
    @Column(name="person")
    private Person person;

    public User() {
        super();
    }

    public User(String email, long quota, String apiKey, Person person) {
        this.email = email;
        this.quota = quota;
        this.apiKey = apiKey;
        this.person = person;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getQuota() {
        return quota;
    }

    public void setQuota(long quota) {
        this.quota = quota;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
