package de.mpg.mpdl.labcam.Utils;

import android.util.Log;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;

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

}
