package de.mpg.mpdl.labcam.Model;

import com.google.gson.annotations.Expose;

/**
 * Created by allen on 27/08/15.
 */
public class ImejiProfile {
    @Expose
    private String id;

    @Expose
    private String Method;



    public ImejiProfile(String id, String method) {
        this.id = id;
        Method = method;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMethod() {
        return Method;
    }

    public void setMethod(String method) {
        Method = method;
    }
}
