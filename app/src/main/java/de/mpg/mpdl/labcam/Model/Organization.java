package de.mpg.mpdl.labcam.Model;

import com.google.gson.annotations.Expose;

/**
 * Created by allen on 27/08/15.
 */
public class Organization {
    @Expose
    private String name;

    @Expose
    private String description;

    @Expose
    private String city;

    @Expose
    private String country;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
