package de.mpg.mpdl.labcam.Utils;

import android.content.Context;
import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.code.data.db.LiteOrmManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by yingli on 9/13/16.
 */

public class DBConnector {

    private static final String LOG_TAG = DBConnector.class.getSimpleName();

    /** TASK **/

    //get user tasks
    public static List<Task> getUserTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .execute();
    }

    // Get User Active Tasks
    public static List<Task> getRecentTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .where("state != ?", String.valueOf(DeviceStatus.state.WAITING))
                .where("state != ?", String.valueOf(DeviceStatus.state.STOPPED))
                .where("state != ?", String.valueOf(DeviceStatus.state.FAILED))
                .orderBy("endDate DESC")
                .execute();
    }

    // Get current AU tasks
    public static Task getAuTask(String userId, String serverName) {
        String mode = "AU";
        return new Select()
                .from(Task.class)
                .where("uploadMode = ?", mode)
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    //get latest task (sometimes its not right need to distinguish Au Mu)
    public static Task getLatestFinishedTask(String userId, String serverName) {
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .orderBy("endDate DESC")
                .where("severName = ?", serverName)
                .executeSingle();
    }

    public static List<Task> getUserStoppedTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .where("state = ?", String.valueOf(DeviceStatus.state.STOPPED))
                .orderBy("startDate DESC")
                .execute();
    }

    public static List<Task> getUserWaitingTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .where("state = ?", String.valueOf(DeviceStatus.state.WAITING))
                .orderBy("startDate DESC")
                .execute();
    }


    public static List<Task> getActiveTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .where("state != ?", String.valueOf(DeviceStatus.state.FINISHED))
//                .where("state != ?", String.valueOf(DeviceStatus.state.FAILED))
                .execute();
    }

    //delete finished tasks
    public static void deleteFinishedAUTasks(String userId, String serverName){

        // get All au tasks first
        List<Task> finishedTasks = new Select()
                .from(Task.class)
                .where("uploadMode = ?","AU")
                .where("userId = ?", userId)
                .where("severName = ?", serverName)
                .execute();

        // remove unfinished tasks form list
        for(Task task:finishedTasks){
            if(task.getFinishedItems() == task.getTotalItems()){

                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.setEndDate(DeviceStatus.dateNow());
                task.save();
            }
        }

        new Delete().from(Task.class)
                .where("uploadMode = ?","AU")
                .where("state = ?", String.valueOf(DeviceStatus.state.FINISHED))
                .where("severName = ?", serverName)
                .execute();

        int num = (new Select()
                .from(Task.class)
                .execute()).size();

        Log.v(LOG_TAG,num +"_finished");
    }

    /** IMAGE **/

    //get latest(createTime) image
    public static Image getImage() {
        return new Select()
                .from(Image.class)
                .orderBy("createTime DESC")
                .executeSingle();
    }

    // get active Image list
    public static List<Image> getActiveImages(String taskId) {
        return new Select()
                .from(Image.class)
                .where("state != ?", String.valueOf(DeviceStatus.state.FAILED))
                .where("state != ?", String.valueOf(DeviceStatus.state.FINISHED))
                .orderBy("RANDOM()")
                .execute();
    }

    public static List<Image> getInactiveImages(String taskId) {
        return new Select()
                .from(Image.class)
                .where("state != ?", String.valueOf(DeviceStatus.state.WAITING))
                .where("state != ?", String.valueOf(DeviceStatus.state.STARTED))
                .orderBy("RANDOM()")
                .execute();
    }

    public static Image getImageByPath(String imagePath) {
        return new Select()
                .from(Image.class)
                .where("imagePath = ?", imagePath)
                .executeSingle();
    }

    public static Image getImageByImgId(String imageId) {
        return new Select()
                .from(Image.class)
                .where("imageId = ?", imageId)
                .executeSingle();
    }

    public static List<Image> getImageByNoteId(String noteId) {
        return new Select()
                .from(Image.class)
                .where("noteId = ?", noteId)
                .execute();
    }

    public static List<Image> getImageByVoiceId(String voiceId) {
        return new Select()
                .from(Image.class)
                .where("voiceId = ?", voiceId)
                .execute();
    }

    /*****  Note  ******/
    public static Note getNoteById(String noteId) {
        return new Select()
                .from(Note.class)
                .where("noteId = ?", noteId)
                .executeSingle();
    }

    /*****  Voice  ******/
    public static Voice getVoiceById(String voiceId) {
        return new Select()
                .from(Voice.class)
                .where("voiceId = ?", voiceId)
                .executeSingle();
    }


    public static void batchEditNote(List<Image> imageList, String noteContent){

        /** create new Note **/
        Note newNote = new Note();  //PREPARE(CREATE) new NOTE
        newNote.setNoteId(UUID.randomUUID().toString());
        newNote.setNoteContent(noteContent);
        newNote.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        for(Image i : imageList)
            newNote.getImageIds().add(i.getImageId());
        newNote.save();

        for (Image image : imageList) {  // every selected image
            if (image.getNoteId() == null || "".equals(image.getNoteId())) {
                // add noteId
                image.setNoteId(newNote.getNoteId());
            } else {
                String oldNoteId = image.getNoteId();
                // update noteId
                image.setNoteId(newNote.getNoteId());
                // remove imageId from old note record
                Note oldNote = getNoteById(oldNoteId);
                oldNote.getImageIds().remove(image.getImageId());
                oldNote.save();
                //TODO modifiedDate ??
                //remove note entry which has empty imageIds
                if (oldNote.getImageIds().size() == 0)
                    oldNote.delete();
            }
            image.save();
        }


    }

    public static void batchEditVoice(List<Image> imageList, String voicePath){

        /** create new Voice **/
        Voice newVoice = new Voice();  //PREPARE(CREATE) new VOICE
        newVoice.setVoiceId(UUID.randomUUID().toString());
        newVoice.setVoicePath(voicePath);
        newVoice.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        for(Image i : imageList)
            newVoice.getImageIds().add(i.getImageId());
        newVoice.save();

        for (Image image : imageList) {  // every selected image
            if (image.getVoiceId() == null || "".equals(image.getVoiceId())) {
                // add voiceId
                image.setVoiceId(newVoice.getVoiceId());
            } else {
                String oldVoiceId = image.getVoiceId();
                // update voiceId
                image.setVoiceId(newVoice.getVoiceId());
                // remove imageId from old voice record
                Voice oldVoice = getVoiceById(oldVoiceId);
                oldVoice.getImageIds().remove(image.getImageId());
                oldVoice.save();
                //TODO modifiedDate ??
                //remove voice entry which has empty imageIds
                if (oldVoice.getImageIds().size() == 0)
                    oldVoice.delete();
            }
            image.save();
        }

    }

    /************************************ refactoring **************************************/

    //get all store
    public static Settings getSettingsByUserId(Context context, String userId){
        List<Settings> settingsList = LiteOrmManager.getInstance(context).queryByEqual("userId", userId, Settings.class);
        if(settingsList!=null && settingsList.size()>0){
            return settingsList.get(0);
        }else
            return null;
    }

}
