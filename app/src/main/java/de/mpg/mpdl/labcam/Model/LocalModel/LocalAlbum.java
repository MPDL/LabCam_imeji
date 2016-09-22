package de.mpg.mpdl.labcam.Model.LocalModel;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by yingli on 1/13/16.
 */
@Table(name = "LocalAlbums")
public class LocalAlbum extends Model {

    @Column(name = "AlbumName")
    private String albumName;

    @Column(name = "AlbumDirectory")
    private String albumDirectory;

    public LocalAlbum() {
        super();
    }

    public LocalAlbum(String albumName, String albumDirectory) {
        this.albumName = albumName;
        this.albumDirectory = albumDirectory;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumDirectory() {
        return albumDirectory;
    }

    public void setAlbumDirectory(String albumDirectory) {
        this.albumDirectory = albumDirectory;
    }
}
