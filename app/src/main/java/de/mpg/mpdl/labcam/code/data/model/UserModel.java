package de.mpg.mpdl.labcam.code.data.model;

import de.mpg.mpdl.labcam.Model.MessageModel.Person;

/**
 * Created by yingli on 3/17/17.
 */

public class UserModel {
    private String email;

    private long quota;

    private String apiKey;

    private Person person;

    public UserModel() {
        super();
    }

    public UserModel(String email, long quota, String apiKey, Person person) {
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
