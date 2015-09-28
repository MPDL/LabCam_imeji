package example.com.mpdlcamera;

/**
 * Created by yuvan on 14.09.15.
 */
public class FolderModel {

    String folder = null;

    Boolean status = null;

    FolderModel(String folder) {
        this.folder = folder;
    }

    public FolderModel(String folder, Boolean status) {
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

    public Boolean isSelected() {
        return status;
    }

    public void setSelected(Boolean status) {
        this.status = status;
    }
}

