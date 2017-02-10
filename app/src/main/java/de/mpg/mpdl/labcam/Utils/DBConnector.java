package de.mpg.mpdl.labcam.Utils;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.LocalModel.Voice;

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
        List<NoteSort> noteSortList= new ArrayList<NoteSort>();

        /** create noteSortList **/
        for (Image image : imageList) {  // every selected image

            int noteSortImageListCount = 0;
            for (NoteSort noteSort : noteSortList) {   // search in noteSort

                if(noteSort.getNoteId()==null  // first time edit, noteId is null
                        || noteSort.getNoteId().equalsIgnoreCase(image.getNoteId())){ // noteId found exist in noteSort
                    List<Image> noteSortImageList = noteSort.getImageList();
                    noteSortImageList.add(image);               // add image to noteSortImageList
                    noteSort.setImageList(noteSortImageList);
                    break;
                }else {
                    noteSortImageListCount += 1;   // not this noteSort
                }
            }

            if(noteSortImageListCount == noteSortList.size()){  // NoteSort not exist
                NoteSort newNoteSort = new NoteSort();             // create NoteSort
                List<Image> noteSortImageList = new ArrayList<>();
                newNoteSort.setNoteId(image.getNoteId());
                noteSortImageList.add(image);                      // add sortImageList
                newNoteSort.setImageList(noteSortImageList);
                noteSortList.add(newNoteSort);
            }
        }

        /** batch operation on notes **/
        Note newNote = new Note();  //PREPARE(CREATE) new NOTE
        newNote.setNoteId(UUID.randomUUID().toString());
        newNote.setNoteContent(noteContent);
        newNote.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        newNote.save();

        boolean deleteNote = true;
        for (NoteSort noteSort : noteSortList) {
            String noteId = (noteSort.getNoteId()!=null)?noteSort.getNoteId():"null";
            Log.d(LOG_TAG, noteId);
            Log.d(LOG_TAG, noteSort.getImageList().size()+"");
            if(noteSort.getNoteId()!=null && getImageByNoteId(noteSort.getNoteId()).size() == noteSort.getImageList().size()){    //UPDATE
                Note updateNote = getNoteById(noteSort.getNoteId());
                updateNote.setNoteContent(noteContent);
                updateNote.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
                updateNote.save();
            }else { // case 1: new NOTE; case 2:
                // BIND NOTE
                for (Image image : noteSort.getImageList()) {
                    image.setNoteId(newNote.getNoteId());
                    image.save();
                    deleteNote = false;
                }
            }
        }
        if(deleteNote){
            newNote.delete();
        }


    }

    public static void batchEditVoice(List<Image> imageList, String voicePath){
        List<VoiceSort> voiceSortList= new ArrayList<VoiceSort>();

        /** create voiceSortList **/
        for (Image image : imageList) {  // every selected image

            int voiceSortImageListCount = 0;
            for (VoiceSort voiceSort : voiceSortList) {   // search in voiceSort
                if(voiceSort.getVoiceId()==null // first time edit, voiceId is null
                        || voiceSort.getVoiceId().equalsIgnoreCase(image.getVoiceId())){ // voiceId found exist in voiceSort
                    List<Image> voiceSortImageList = voiceSort.getImageList();
                    voiceSortImageList.add(image);               // add image to voiceSortImageList
                    voiceSort.setImageList(voiceSortImageList);
                    break;
                }else {
                    voiceSortImageListCount += 1;   // not this voiceSort
                }
            }

            if(voiceSortImageListCount == voiceSortList.size()){  // VoiceSort not exist
                VoiceSort newVoiceSort = new VoiceSort();             // create VoiceSort
                List<Image> voiceSortImageList = new ArrayList<>();
                newVoiceSort.setVoiceId(image.getVoiceId());
                voiceSortImageList.add(image);                      // add sortImageList
                newVoiceSort.setImageList(voiceSortImageList);
                voiceSortList.add(newVoiceSort);
            }
        }

        /** batch operation on voice **/
        Voice newVoice = new Voice();  //PREPARE(CREATE) new VOICE
        newVoice.setVoiceId(UUID.randomUUID().toString());
        newVoice.setVoicePath(voicePath);
        newVoice.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
        newVoice.save();

        boolean deleteVoice = true;
        for (VoiceSort voiceSort : voiceSortList) {
            String voiceId = (voiceSort.getVoiceId()!=null)?voiceSort.getVoiceId():"null";
            Log.d(LOG_TAG, voiceId);
            Log.d(LOG_TAG, voiceSort.getImageList().size()+"");
            if(voiceSort.getVoiceId()!=null && getImageByVoiceId(voiceSort.getVoiceId()).size() == voiceSort.getImageList().size()){    //UPDATE
                Voice updateVoice = getVoiceById(voiceSort.getVoiceId());
                updateVoice.setVoicePath(voicePath);
                updateVoice.setCreateTime(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
                updateVoice.save();
            }else { // case 1: new NOTE; case 2:
                // BIND NOTE
                for (Image image : voiceSort.getImageList()) {
                    image.setNoteId(voiceSort.getVoiceId());
                    image.save();
                    deleteVoice = false;
                }
            }
        }
        if(deleteVoice){
            newVoice.delete();
        }
    }

    private static class NoteSort{
        String noteId;
        List<Image> imageList;
        String imageCount;

        public NoteSort() {
        }

        public NoteSort(String noteId, List<Image> imageList, String imageCount) {
            this.noteId = noteId;
            this.imageList = imageList;
            this.imageCount = imageCount;
        }

        public String getNoteId() {
            return noteId;
        }

        public void setNoteId(String noteId) {
            this.noteId = noteId;
        }

        public String getImageCount() {
            return imageCount;
        }

        public void setImageCount(String imageCount) {
            this.imageCount = imageCount;
        }

        public List<Image> getImageList() {
            return imageList;
        }

        public void setImageList(List<Image> imageList) {
            this.imageList = imageList;
        }
    }


    private static class VoiceSort {
        String voiceId;
        List<Image> imageList;

        public VoiceSort() {
        }

        public VoiceSort(String voiceId, List<Image> imageList) {
            this.voiceId = voiceId;
            this.imageList = imageList;
        }

        public String getVoiceId() {
            return voiceId;
        }

        public void setVoiceId(String voiceId) {
            this.voiceId = voiceId;
        }

        public List<Image> getImageList() {
            return imageList;
        }

        public void setImageList(List<Image> imageList) {
            this.imageList = imageList;
        }
    }
}
