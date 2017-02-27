package de.mpg.mpdl.labcam.code.rxbus.event;

/**
 * Created by yingli on 2/21/17.
 */

public class NoteRefreshEvent {
    String imgPath;

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public NoteRefreshEvent(String imgPath) {
        this.imgPath = imgPath;
    }
}
