package example.com.mpdlcamera.Utils;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.util.List;

import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;

/**
 * Created by allen on 09/04/15.
 */
public class DeviceStatus {

    private static final String LOG_TAG = DeviceStatus.class.getSimpleName();
    public static final String username = "";
    public static final String password = "";
    public static final String collectionID = "0L5yLxP_AphUMtIi";
    public static final String queryKeyword = "MPDLCam";
 //   public static final String BASE_URL= "";
    public static final String BASE_URL = "https://spot.mpdl.mpg.de/rest/";

    // Checks whether the device currently has a network connection
    public static boolean isNetworkEnabled(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager)  activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null) {
            return networkInfo.isConnected();
        } else {
            return false;
        }
    }

    // Check whether the GPS sensor is activated
    public static boolean isGPSEnabled(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isNetworkLocationEnabled(Activity activity){
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean isPassiveLocationEnabled(Activity activity){
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }

    public static boolean checkExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        else {
            return false;
        }
    }

    public static void showToast(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View rootLayout, String message) {
        if(rootLayout != null){
            Snackbar.make(rootLayout, message, Snackbar.LENGTH_SHORT)
                    .setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    }).show();
        }
    }

    public enum backupOption {
        wifi, wifiCellular
    }

    public enum state{
        WAITING, STARTED, STOPPED, INTERRUPTED, FINISHED
    }

    //get latest task (sometimes its not right need to distinguish Au Mu)
    public static Task getTask() {
        return new Select()
                .from(Task.class)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    public static Task getAuTask() {
        String mode = "AU";
        return new Select()
                .from(Task.class)
                .where("uploadMode = ?", mode)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    public static List<Task> getTasks(){
        return null;
    }
    //get user tasks
    public static List<Task> getUserTasks(String userId){
        return new Select()
                .from(Task.class)
                .where("userId = ?",userId)
                .execute();
    }

    //get Image list of a Task
    public static List<Image> getImagesByTaskId(String taskId){
        return new Select()
                .from(Image.class)
                .where("taskId = ?", taskId)
                .orderBy("createTime ASC")
                .execute();
    }


    //delete tasks

    //delete finished tasks
    public static void deleteFinishedTasks(){

        // get All au tasks first
        List<Task> finishedTasks = new Select()
                .from(Task.class)
                .execute();
        Log.v(LOG_TAG, finishedTasks.size() + "_all");

        // remove unfinished tasks form list
        for(Task task:finishedTasks){
            if(task.getFinishedItems() == task.getTotalItems()){
                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.save();
            }
        }

        new Delete().from(Task.class).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).execute();

        int num = (new Select()
                .from(Task.class)
                .execute()).size();
        Log.v(LOG_TAG,num +"_finished");
    }

}
