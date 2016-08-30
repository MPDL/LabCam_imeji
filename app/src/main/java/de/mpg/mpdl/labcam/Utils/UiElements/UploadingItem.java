package de.mpg.mpdl.labcam.Utils.UiElements;

/**
 * Created by yu on 30.08.2016.
 */

public class UploadingItem {
    private String itemPath;
    private boolean inUploading  = true;

    public UploadingItem() {
    }

    public UploadingItem(String itemPath, boolean inUploading) {
        this.itemPath = itemPath;
        this.inUploading = inUploading;
    }

    public String getItemPath() {
        return itemPath;
    }

    public void setItemPath(String itemPath) {
        this.itemPath = itemPath;
    }

    public boolean isInUploading() {
        return inUploading;
    }

    public void setInUploading(boolean inUploading) {
        this.inUploading = inUploading;
    }
}
