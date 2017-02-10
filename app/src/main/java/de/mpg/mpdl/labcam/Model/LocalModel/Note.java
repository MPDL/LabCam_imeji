package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import java.util.List;
import java.util.ArrayList;


/**
 * Created by yingli on 11/28/16.
 */
@Table(name = "Notes")
public class Note extends Model{

    @Expose
    @Column(name = "noteId")
    String noteId;

    @Expose
    @Column(name = "noteContent")
    String noteContent;

    @Expose
    @Column(name = "createTime")
    String createTime;

    @Expose
    @Column(name = "imageIds")
    List<String> imageIds;

    public Note(String noteId, String noteContent, String createTime, ArrayList<String> imageIds){
        this.noteId = noteId;
        this.noteContent = noteContent;
        this.createTime = createTime;
        this.imageIds = imageIds;
    }

    public Note() {
        super();
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getNoteContent() {
        return noteContent;
    }

    public void setNoteContent(String noteContent) {
        this.noteContent = noteContent;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public List<String> getImageIds() {
        return imageIds;
    }

    public void setImageIds(List<String> imageIds) {
        this.imageIds = imageIds;
    }



}
