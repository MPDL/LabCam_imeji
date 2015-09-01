package example.com.mpdlcamera.Model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

/**
 * Created by allen on 27/08/15.
 */

@Table(name = "DataItem")
public class DataItem extends Model {

    @Expose
    @Column(name = "filename")
    private String filename;

    @Expose
    @Column(name = "createdDate")
    private String createdDate;

    @Expose
    @Column(name = "fileUrl")
    private String fileUrl;

    @Expose
    @Column(name = "webResolutionUrlUrl")
    private String webResolutionUrlUrl;

    @Expose
    @Column(name = "thumbnailUrl")
    private String thumbnailUrl;

    @Expose
    @Column(name = "createdBy")
    private User createdBy;

    //@Expose
    @Column(name = "metaData")
    private MetaData metadata;

    @Expose
    @Column(name = "collectionId")
    private String collectionId;

    @Column(name = "localPath")
    private String localPath;

    public DataItem(){
        super();
    }

    public DataItem(String filename, String createdDate, String fileUrl, String webResolutionUrlUrl,
                    String thumbnailUrl, User createdBy, MetaData metadata,
                    String collectionId) {
        this.filename = filename;
        this.createdDate = createdDate;
        this.fileUrl = fileUrl;
        this.webResolutionUrlUrl = webResolutionUrlUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.createdBy = createdBy;
        this.metadata = metadata;
        this.collectionId = collectionId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getWebResolutionUrlUrl() {
        return webResolutionUrlUrl;
    }

    public void setWebResolutionUrlUrl(String webResolutionUrlUrl) {
        this.webResolutionUrlUrl = webResolutionUrlUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }


    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }


}
