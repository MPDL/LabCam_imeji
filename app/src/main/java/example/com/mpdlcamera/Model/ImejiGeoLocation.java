package example.com.mpdlcamera.Model;

import com.google.gson.annotations.Expose;

/**
 * Created by allen on 27/08/15.
 */
public class ImejiGeoLocation {

    @Expose
    private String name;

    @Expose
    private double latitude;

    @Expose
    private double longitude;

    public ImejiGeoLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
