package de.mpg.mpdl.labcam.Model.MessageModel;

import com.activeandroid.annotation.Column;
import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

import de.mpg.mpdl.labcam.Model.Organization;

/**
 * Created by yingli on 1/20/16.
 */
public class Person {

    private int position;

    @Expose
    @Column(name="userId")
    private String id;

    @Expose
    @Column(name="familyName")
    private String familyName;

    @Expose
    @Column(name="givenName")
    private String givenName;

    @Expose
    @Column(name="completeName")
    private String completeName;

    @Expose
    @Column(name="alternativeName")
    private String alternativeName;

    private String role;

    private List<Organization> organizations = new ArrayList<Organization>();

    public Person(int position, String id, String familyName, String givenName, String completeName, String alternativeName, String role, List<Organization> organizations) {
        this.position = position;
        this.id = id;
        this.familyName = familyName;
        this.givenName = givenName;
        this.completeName = completeName;
        this.alternativeName = alternativeName;
        this.role = role;
        this.organizations = organizations;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Organization> getOrganizations() {
        return organizations;
    }

    public void setOrganizations(List<Organization> organizations) {
        this.organizations = organizations;
    }
}
