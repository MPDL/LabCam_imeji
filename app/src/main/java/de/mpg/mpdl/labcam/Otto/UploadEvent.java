package de.mpg.mpdl.labcam.Otto;

/**
 * Created by kiran on 16.09.15.
 */
public class UploadEvent {
    public final Integer httpStatus;

    public UploadEvent(Integer httpStatus) {
        this.httpStatus = httpStatus;
    }
}
