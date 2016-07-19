package example.com.mpdlcamera.Model.TO;

import com.google.gson.annotations.Expose;

/**
 * Created by yingli on 7/18/16.
 */

public class LiteralConstraintTO {

    @Expose
    private String value;

    public LiteralConstraintTO(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
