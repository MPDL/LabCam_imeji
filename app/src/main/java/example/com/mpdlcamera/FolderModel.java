package example.com.mpdlcamera;

/**
 * Created by yuvan on 14.09.15.
 */
public class FolderModel {

    String folder = null;

    boolean status = false;

    FolderModel(String folder) {
        this.folder = folder;
    }

    public FolderModel(String folder, boolean status) {
        super();
        this.folder = folder;

        this.status = status;
    }

    public String getFolder() {
        return folder;
    }


    public void setFolder(String folder) {
        this.folder = folder;
    }

    public boolean isSelected() {
        return status;
    }

    public void setSelected(boolean status) {
        this.status = status;
    }
}

