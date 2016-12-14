package de.mpg.mpdl.labcam.Model.LocalModel;

import com.google.gson.annotations.Expose;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

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

    public Note(String noteId, String noteContent, String createTime) {
        this.noteId = noteId;
        this.noteContent = noteContent;
        this.createTime = createTime;
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
}
