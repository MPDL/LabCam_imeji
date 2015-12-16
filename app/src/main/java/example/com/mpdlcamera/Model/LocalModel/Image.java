package example.com.mpdlcamera.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.Expose;

/**
 * Created by yingli on 12/14/15.
 */

@Table(name = "Image")
public class Image extends Model {
    @Expose
    @Column(name = "ImageId")
    private String ImageId;

    @Expose
    @Column(name = "ImageName")
    private String ImageName;

    @Expose
    @Column(name = "createTime")
    private String createTime;

    @Expose
    @Column(name = "geoLocation")
    private String geoLocation;

    @Expose
    @Column(name = "state")
    private String state;

    @Expose
    @Column(name = "task")
    public Task task;



    public Image() {
        super();
    }

    public Image(String imageId, String imageName, String createTime, String geoLocation, String state, Task task) {
        ImageId = imageId;
        ImageName = imageName;
        this.createTime = createTime;
        this.geoLocation = geoLocation;
        this.state = state;
        this.task = task;
    }

    public String getImageId() {
        return ImageId;
    }

    public void setImageId(String imageId) {
        ImageId = imageId;
    }

    public String getImageName() {
        return ImageName;
    }

    public void setImageName(String imageName) {
        ImageName = imageName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getGeoLocation() {
        return geoLocation;
    }

    public void setGeoLocation(String geoLocation) {
        this.geoLocation = geoLocation;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }
}
