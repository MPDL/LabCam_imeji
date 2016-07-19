package example.com.mpdlcamera.Model.TO;

import com.google.gson.annotations.Expose;

/**
 * Created by yingli on 7/19/16.
 */

public class LocalizedStringTO {

    @Expose
    private String value;

    @Expose
    private String lang;

    public LocalizedStringTO(String value, String lang) {
        this.value = value;
        this.lang = lang;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }
}
