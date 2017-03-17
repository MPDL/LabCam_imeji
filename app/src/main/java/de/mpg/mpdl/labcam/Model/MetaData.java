package de.mpg.mpdl.labcam.Model;


import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.util.List;

/**
 * Created by allen on 27/08/15.
 */
@Table(name = "MetaDataLocal")

public class MetaData {

    /*
    metadata: {
        tags: "test",
        author: "Allen",
        title: "DataCollector_20150820_110223.jpg@Theresienstr 23, 80333 Muenchen, Germany",
        location: {
            name: "Theresienstr 23, 80333 Muenchen, Germany",
            longitude: 11.5761326,
            latitude: 48.1477742
            },
        deviceID: "1",
        accuracy: 10
    }
     */
    @Expose
    @Column(name = "title")
    private String title;

    @Expose
    @Column(name = "address")
    private String address;

    @Expose
    @Column(name = "latitude")
    private double latitude;

    @Expose
    @Column(name = "longitude")
    private double longitude;

    @Expose
    @Column(name = "accuracy")
    private double accuracy;

    @Column(name = "deviceID")
    private String deviceID;

    @Column(name = "fileType")
    private String type;

    @Expose
    //@Column(name = "tags")
    private List<String> tags;

    @Expose
    @Column(name = "creator")
    private String creator;

    public MetaData() {
        super();
    }


    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }
//
//    public DataItem getWhichItem() {
//        return whichItem;
//    }
//
//    public void setWhichItem(DataItem whichItem) {
//        this.whichItem = whichItem;
//    }


}

