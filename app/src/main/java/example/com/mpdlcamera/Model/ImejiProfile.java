package example.com.mpdlcamera.Model;

import com.google.gson.annotations.Expose;

/**
 * Created by allen on 27/08/15.
 */
public class ImejiProfile {
    @Expose
    private String Profileid;

    @Expose
    private String Method;

    public String getProfileid() {
        return Profileid;
    }

    public void setProfileid(String profileid) {
        Profileid = profileid;
    }

    public String getMethod() {
        return Method;
    }

    public void setMethod(String method) {
        Method = method;
    }
}
