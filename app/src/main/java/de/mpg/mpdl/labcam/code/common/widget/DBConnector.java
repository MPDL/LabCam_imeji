package de.mpg.mpdl.labcam.code.common.widget;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;

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
                .where("serverName = ?", serverName)
                .orderBy("startDate DESC")
                .execute();
    }

    // Get User Active Tasks
    public static List<Task> getRecentTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
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
                .where("serverName = ?", serverName)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    // Get last Non-AU task
    public static Task getLastTask(String userId, String serverName) {
        String mode = "AU";
        return new Select()
                .from(Task.class)
                .where("uploadMode != ?", mode)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    // get Task by TaskId
    public static Task getTaskById(String taskId, String userId, String serverName) {
        return new Select()
                .from(Task.class)
                .where("Id = ?", taskId)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .executeSingle();
    }

    //get latest task (sometimes its not right need to distinguish Au Mu)
    public static Task getLatestFinishedTask(String userId, String serverName) {
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .orderBy("endDate DESC")
                .where("serverName = ?", serverName)
                .executeSingle();
    }

    public static List<Task> getUserStoppedTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .where("state = ?", String.valueOf(DeviceStatus.state.STOPPED))
                .orderBy("startDate DESC")
                .execute();
    }

    public static List<Task> getUserWaitingTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .where("state = ?", String.valueOf(DeviceStatus.state.WAITING))
                .orderBy("startDate DESC")
                .execute();
    }


    public static List<Task> getActiveTasks(String userId, String serverName){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .where("state != ?", String.valueOf(DeviceStatus.state.FINISHED))
//                .where("state != ?", String.valueOf(DeviceStatus.state.FAILED))
                .execute();
    }

    public static List<Task> getActiveManualTasks(String userId, String serverName){
        return new Select().
                from(Task.class)
                .where("uploadMode = ?", "MU")
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .where("state != ?", String.valueOf(DeviceStatus.state.FINISHED))
                .execute();
    }

    //delete finished tasks
    public static void deleteFinishedAUTasks(String userId, String serverName){

        // get All au tasks first
        List<Task> finishedTasks = new Select()
                .from(Task.class)
                .where("uploadMode = ?","AU")
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
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
                .where("serverName = ?", serverName)
                .execute();

        int num = (new Select()
                .from(Task.class)
                .execute()).size();

        Log.v(LOG_TAG,num +"_finished");
    }

    public static void deleteAuTask(){
        new Delete().from(Task.class).where("uploadMode = ?", "AU").execute();
    }

    public static void deleteTaskById(Long taskId){
        new Delete().from(Task.class).where("Id = ?", taskId).execute();
    }
    /** IMAGE **/

    //get latest(createTime) image
    public static Image getImage() {
        return new Select()
                .from(Image.class)
                .orderBy("createTime DESC")
                .executeSingle();
    }

    public static boolean isNeedUpload(String imgPath, String userId, String serverName){
        for (Task task : getActiveTasks(userId, serverName)) {
            if(task.getImagePaths().contains(imgPath)){
                return true;
            }
        }
        return false;
    }

    public static Image getImageByPath(String imagePath, String userId, String serverName) {
        return new Select()
                .from(Image.class)
                .where("imagePath = ?", imagePath)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .executeSingle();
    }

    public static Image getImageByImgId(Long imageId) {
        return new Select()
                .from(Image.class)
                .where("Id = ?", imageId)
                .executeSingle();
    }

    public static List<Image> getImageByNoteId(Long noteId) {
        return new Select()
                .from(Image.class)
                .where("noteId = ?", noteId)
                .execute();
    }

    public static List<Image> getImageByVoiceId(Long voiceId) {
        return new Select()
                .from(Image.class)
                .where("voiceId = ?", voiceId)
                .execute();
    }

    /*****  Note  ******/
    public static Note getNoteById(Long id, String userId, String serverName) {
        return new Select()
                .from(Note.class)
                .where("Id = ?", id)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .executeSingle();
    }

    /*****  Voice  ******/
    public static Voice getVoiceById(Long id, String userId, String serverName) {
        return new Select()
                .from(Voice.class)
                .where("Id = ?", id)
                .where("userId = ?", userId)
                .where("serverName = ?", serverName)
                .executeSingle();
    }


    public static void batchEditNote(List<Image> imageList, String noteContent, String userId, String serverName) {

        /** create new Note **/
        Note newNote = new Note();  //PREPARE(CREATE) new NOTE
        newNote.setNoteContent(noteContent);
        newNote.setUserId(userId);
        newNote.setServerName(serverName);

        newNote.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        for (Image i : imageList)
            newNote.getImageIds().add(i.getId().toString());
        newNote.save();

        for (Image image : imageList) {  // every selected image
            if ((image.getNoteId() == null || "".equals(image.getNoteId()))) {
                // add noteId
                image.setNoteId(newNote.getId());
            } else if (DBConnector.getNoteById(image.getNoteId(), userId, serverName) != null) {
                Long oldNoteId = image.getNoteId();
                // update noteId
                image.setNoteId(newNote.getId());
                // remove imageId from old note record
                Note oldNote = getNoteById(oldNoteId, userId, serverName);
                if (oldNote != null) {
                    oldNote.getImageIds().remove(image.getId().toString());
                    oldNote.save();
                    //remove note entry which has empty imageIds
                    if (oldNote.getImageIds().size() == 0) {
                        oldNote.delete();
                    }

                }
            }
            image.save();
        }
    }

    public static void batchEditVoice(List<Image> imageList, String voicePath, String userId, String serverName){

        /** create new Voice **/
        Voice newVoice = new Voice();  //PREPARE(CREATE) new VOICE
        newVoice.setVoicePath(voicePath);
        newVoice.setUserId(userId);
        newVoice.setServerName(serverName);
        newVoice.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        for(Image i : imageList)
            newVoice.getImageIds().add(i.getId().toString());
        newVoice.save();

        for (Image image : imageList) {  // every selected image
            if (image.getVoiceId() == null || "".equals(image.getVoiceId())) {
                // add voiceId
                image.setVoiceId(newVoice.getId());
            } else if(DBConnector.getVoiceById(image.getVoiceId(), userId, serverName)!=null) {
                Long oldVoiceId = image.getVoiceId();
                // update voiceId
                image.setVoiceId(newVoice.getId());
                // remove imageId from old voice record
                Voice oldVoice = getVoiceById(oldVoiceId, userId,serverName);
                oldVoice.getImageIds().remove(image.getId().toString());
                oldVoice.save();
                //remove voice entry which has empty imageIds
                if (oldVoice.getImageIds().size() == 0)
                    oldVoice.delete();
            }
            image.save();
        }

    }

    /************************************ refactoring **************************************/

    public static Settings getSettingsByUserId(String userId){
        return new Select().from(Settings.class)
                .where("userId = ?", userId)
                .executeSingle();   // get old settings

    }

    public static List<ImejiFolder> getUserFolders(){
        return new Select()
                .from(ImejiFolder.class)
                .execute();
    }

    public static void deleteAllImejiFolders(){
        new Delete().from(ImejiFolder.class).execute();
    }
}
