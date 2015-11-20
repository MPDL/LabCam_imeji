package example.com.mpdlcamera.SQLite;

/**
 * Created by kiran on 30.10.15.
 */
public class FileId {

    private String fileName;
    private String status;

    public FileId(){}

    /*
        constructor for the fileId with the filename and the status
     */
    public FileId(String fileName, String status) {

        this.fileName = fileName;
        this.status = status;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "File [name=" + fileName + ", status=" + status
                + "]";
    }
}
