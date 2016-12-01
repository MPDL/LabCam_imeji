package de.mpg.mpdl.labcam.Model.LocalModel;
import android.os.Parcel;
import android.os.Parcelable;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 11/29/16.
 */

public class ImageGroup implements Parcelable {

    private String batchId;

    private List<String> imageList;

    public ImageGroup(Parcel in) {
        this.batchId = in.readString();
        this.imageList = new ArrayList<String>();
        in.readStringList(imageList);
    }

    public ImageGroup(String batchId, List<String> imageList) {
        this.batchId = batchId;
        this.imageList = imageList;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(batchId);
        dest.writeStringList(imageList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ImageGroup> CREATOR = new Creator<ImageGroup>() {
        @Override
        public ImageGroup createFromParcel(Parcel in) {
            return new ImageGroup(in);
        }

        @Override
        public ImageGroup[] newArray(int size) {
            return new ImageGroup[size];
        }
    };

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public List<String> getImageList() {
        return imageList;
    }

    public void setImageList(List<String> imageList) {
        this.imageList = imageList;
    }
}