package example.com.mpdlcamera.Utils;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.descriptor.TaglibDescriptor;

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
    public static final String BASE_URL = "https://qa-gluons.mpdl.mpg.de/imeji/rest/";

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
        WAITING, STARTED, STOPPED, INTERRUPTED, FAILED,FINISHED
    }

    //get latest task (sometimes its not right need to distinguish Au Mu)
    public static Task getTask() {
        return new Select()
                .from(Task.class)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    //get latest task (sometimes its not right need to distinguish Au Mu)
    public static Task getLatestFinishedTask(String userId) {
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .orderBy("endDate DESC")
                .executeSingle();
    }

    //TODO: need to pass user as param
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

    /**
     * get User Active Tasks
     * @param userId
     * @return
     */
    public static List<Task> getRecentTasks(String userId){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("state != ?", String.valueOf(state.WAITING))
                .where("state != ?", String.valueOf(state.STOPPED))
                .orderBy("endDate DESC")
                .execute();
    }

    public static List<Task> getUserStoppedTasks(String userId){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("state = ?", String.valueOf(state.STOPPED))
                .orderBy("startDate DESC")
                .execute();
    }

    public static List<Task> getUserWaitingTasks(String userId){
        return new Select()
                .from(Task.class)
                .where("userId = ?", userId)
                .where("state = ?", String.valueOf(state.WAITING))
                .orderBy("startDate DESC")
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
    public static void deleteFinishedAUTasks(){

        // get All au tasks first
        List<Task> finishedTasks = new Select()
                .from(Task.class)
                .where("uploadMode = ?","AU")
                .execute();

        // remove unfinished tasks form list
        for(Task task:finishedTasks){
            if(task.getFinishedItems() == task.getTotalItems()){

                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.setEndDate(dateNow());
                task.save();
            }
        }

        new Delete().from(Task.class).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).execute();

        int num = (new Select()
                .from(Task.class)
                .execute()).size();
        Log.v(LOG_TAG,num +"_finished");
    }

    public static String parseServerUrl(String Url){
        // divide string
        String[] parts = Url.split("/");
        String coreUrl = null;
        String serverUrl = null;
        for (int i = 0;i < parts.length;i++){
            if(parts[i].equalsIgnoreCase("https:")){
                // ignore https:
            } else if(parts[i].equalsIgnoreCase("http:")){
                // ignore empty
            } else if (parts[i].equalsIgnoreCase("")) {
                // also ignore rest
            }else if (parts[i].equalsIgnoreCase("rest")) {
                // also ignore rest
            } else if (parts[i].equalsIgnoreCase("imeji")) {
                // also ignore rest
            } else {
                coreUrl = parts[i];
            }
        }
//        serverUrl = "http://"+coreUrl+"/rest/";
        serverUrl = coreUrl;
        return serverUrl;
    }

    public static long dateNow(){
        // Create an instance of SimpleDateFormat used for formatting
        // the string representation of date (month/day/year)
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

        // Get the date today using Calendar object.
        Date today = Calendar.getInstance().getTime();
        long date = today.getTime();
                // Using DateFormat format method we can create a string
        // representation of a date with the defined format.

        String reportDate = df.format(today);

        // Print what date is today!
//        System.out.println("Report Date: " + reportDate);
        return date;
    }

    public static Date longToDate(long dateL){
        Date date = new Date(dateL);
        return date;
    }

    public static Date stringToDate(String dateStr){
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        try {
             date = df.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    /**
     * date diff
     * @param startDate
     * @param endDate
     * @return
     */
    public static String twoDateDistance(Date startDate,Date endDate){

        if(startDate == null ||endDate == null){
            return null;
        }
        long timeLong = endDate.getTime() - startDate.getTime();
        if (timeLong<60*1000)
            return timeLong/1000 + " seconds ago";
        else if (timeLong<60*60*1000){
            timeLong = timeLong/1000 /60;
            return timeLong + " minutes ago";
        }
        else if (timeLong<60*60*24*1000){
            timeLong = timeLong/60/60/1000;
            return timeLong+" hours ago";
        }
        else if (timeLong<60*60*24*1000*7){
            timeLong = timeLong/1000/ 60 / 60 / 24;
            return timeLong + " days ago";
        }
        else if (timeLong<60*60*24*1000*7*4){
            timeLong = timeLong/1000/ 60 / 60 / 24/7;
            return timeLong + " weeks ago";
        }
        else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT+02:00"));
            return sdf.format(startDate);
        }
    }

    /**
     * twoDateWithin  1  sec
     * @param startDate
     * @param endDate
     * @return
     */
    public static boolean twoDateWithinSecounds(Date startDate,Date endDate){
        if(startDate == null ||endDate == null){
            return false;
        }
        long timeLong = endDate.getTime() - startDate.getTime();
        if (timeLong<1*1000){
            return true;
        }else {
            return false;
        }

    }

    public static String metaDataJson(String imagePath){

        String metaDataJsonStr = null;

        ExifInterface exif = null;

        try {
            exif = new ExifInterface(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // if exif exist, generate metaDataJson
        if(exif!=null){
            String createTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
            if(createTime!=null) {
                Log.e(LOG_TAG,createTime);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
                try {
                    Date date = formatter.parse(createTime);
                    SimpleDateFormat formatterShort = new SimpleDateFormat("yyyy-MM-dd");
                    createTime = formatterShort.format(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                    createTime = "";
                }
            }

            String makeStr = exif.getAttribute(ExifInterface.TAG_MAKE);
            String modelStr = exif.getAttribute(ExifInterface.TAG_MODEL);
            int isoSpeedRatings = 0;
            try{
                isoSpeedRatings = Integer.parseInt(exif.getAttribute(ExifInterface.TAG_ISO));
            }
            catch (Exception e){

            }
            //TODO: find a solution for ExposureMode
            String exposureModeStr = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            String exposureTimeStr = exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
            float[] latLong = new float[2];
            if (exif.getLatLong(latLong)) {
                // latLong[0] holds the Latitude value now.
                // latLong[1] holds the Longitude value now.
            }
            else {
                // Latitude and Longitude were not included in the Exif data.
            }

            try {
                 metaDataJsonStr = new JSONObject()
                         .put("Creation Date", createTime)
                         .put("Make", makeStr)
                         .put("ISO Speed Ratings", isoSpeedRatings)
                         .put("Model", modelStr)
                         .put("Exposure Time", exposureTimeStr)
                         .put("Exposure Mode", exposureModeStr)
                         .put("Geolocation", new JSONObject()
                                 .put("name", "")
                                 .put("longitude", latLong[0])
                                 .put("latitude", latLong[1]))
                         .toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }
        Log.e(LOG_TAG, metaDataJsonStr);
        return metaDataJsonStr;
    }

}
