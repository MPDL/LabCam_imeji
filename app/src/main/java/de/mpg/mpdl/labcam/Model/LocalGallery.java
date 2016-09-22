package de.mpg.mpdl.labcam.Model;

/**
 * Created by yuvan on 14.09.15.
 */
public class LocalGallery {

    String galleryName = null;

    boolean status = false;

    LocalGallery(String gallery) {
        this.galleryName = gallery;
    }

    public LocalGallery(String gallery, boolean status) {
        super();
        this.galleryName = gallery;

        this.status = status;
    }

    public String getGallery() {
        return galleryName;
    }


    public void setGallery(String gallery) {
        this.galleryName = gallery;
    }

    public boolean isSelected() {
        return status;
    }

    public void setSelected(boolean status) {
        this.status = status;
    }
}

