package de.mpg.mpdl.labcam.Model;

import java.util.List;

/**
 * Created by yuvan on 14.09.15.
 */
public class Gallery {

    String galleryName = null;

    boolean status = false;

    String bucketId = null;

    Long coverId = null;

    List<Gallery> items;

    List<String> imagePathList;

    Integer count = 0;

    public String getGalleryPath() {
        return galleryPath;
    }

    public void setGalleryPath(String galleryPath) {
        this.galleryPath = galleryPath;
    }

    String galleryPath = null;


    public Gallery() {

    }


    Gallery(String folder) {
        this.galleryName = folder;
    }

    public Gallery(String galleryName, boolean status) {
        super();
        this.galleryName = galleryName;

        this.status = status;
    }

    public String getGalleryName() {
        return galleryName;
    }


    public void setGalleryName(String galleryName) {
        this.galleryName = galleryName;
    }

    public boolean isSelected() {
        return status;
    }

    public void setSelected(boolean status) {
        this.status = status;
    }

    public List<Gallery> getItems() {
        return items;
    }

    public void setItems(List<Gallery> items) {
        this.items = items;
    }

    public String getBucketId() { return bucketId; }

    public void setBucketId(String bucketId) { this.bucketId = bucketId; }

    public Long getCoverId() { return coverId; }

    public void setCoverId(Long coverId) { this.coverId = coverId; }

    public void incrementCount() {

        this.count++;
    }
    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<String> getImagePathList() {
        return imagePathList;
    }

    public void setImagePathList(List<String> imagePathList) {
        this.imagePathList = imagePathList;
    }
}

