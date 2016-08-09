package de.mpg.mpdl.labcam.Utils;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDescriptorBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDescriptor;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;

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
//    public static final String BASE_URL = "http://test-gluons.mpdl.mpg.de/imeji/rest/";

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

    public static String metaDataJson(String imagePath, Boolean[] typeList ){

        String metaDataJsonStr = null;

        File file = new File(imagePath);

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            metaDataJsonStr =  generateJsonStr(metadata, typeList);

            print(metadata);
        } catch (ImageProcessingException e) {
            // handle exception
        } catch (IOException e) {
            // handle exception
        }


        Log.e(LOG_TAG, metaDataJsonStr);
        return metaDataJsonStr;
    }


    /**
     * generate json string
     * @param metadata
     */
    private static String generateJsonStr(Metadata metadata, Boolean[] typeList){

        String metaDataJsonStr = null;

        // obtain the Exif directory
        ExifSubIFDDirectory exifSubIFDDirectory
                = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        ExifIFD0Directory exifIFD0Directory
                = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        GpsDirectory gpsDirectory
                = metadata.getFirstDirectoryOfType(GpsDirectory.class);

        String makeStr = exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE);
        String modelStr = exifIFD0Directory.getString(ExifSubIFDDirectory.TAG_MODEL);
        int ISOSpeedRating = Integer.parseInt(exifSubIFDDirectory.getString(ExifIFD0Directory.TAG_ISO_EQUIVALENT));

        // create date use TAG_DATE
        String CreationDateStr;
        Date date = exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
        SimpleDateFormat formatterShort = new SimpleDateFormat("yyyy-MM-dd");
        CreationDateStr = formatterShort.format(date);

        String latitudeStr = String.valueOf(gpsDirectory.getGeoLocation().getLatitude());
        String longitudeStr = String.valueOf(gpsDirectory.getGeoLocation().getLongitude());

        String GPSVersionIDStr = gpsDirectory.getString(gpsDirectory.TAG_VERSION_ID);
        String SensingMethodStr = exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_SENSING_METHOD);
        String ApertureValueStr = exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE);
        String ColorSpaceStr = exifSubIFDDirectory.getString(exifSubIFDDirectory.TAG_COLOR_SPACE);
        String ExposureTimeStr = exifSubIFDDirectory.getString(exifSubIFDDirectory.TAG_EXPOSURE_TIME);

        try {
            JSONObject jsonObject = new JSONObject();
            if(typeList[0]){
                jsonObject.put("Make", makeStr);
            }
            if(typeList[1]){
                jsonObject.put("Model", modelStr);
            }
            if(typeList[2]){
                jsonObject.put("ISO Speed Ratings", ISOSpeedRating);
            }
            if(typeList[3]){
                jsonObject.put("Creation Date", CreationDateStr);
            }
            if(typeList[4]){
                jsonObject.put("Geolocation", new JSONObject()
                        .put("name", "")
                        .put("longitude", latitudeStr)
                        .put("latitude", longitudeStr));
            }if(typeList[5]){
                jsonObject.put("GPS Version ID", GPSVersionIDStr);
            }if(typeList[6]){
                jsonObject.put("Sensing Method", SensingMethodStr);
            }if(typeList[7]){
                jsonObject.put("Aperture Value", ApertureValueStr);
            }if(typeList[8]){
                jsonObject.put("Color Space", ColorSpaceStr);
            }

            if(typeList[9]){
                jsonObject.put("Exposure Time", ExposureTimeStr);
            }
            metaDataJsonStr = jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return metaDataJsonStr;
    }

    /**
     * print metadata
     * @param metadata
     */
    private static void print(Metadata metadata)
    {
        System.out.println("-------------------------------------");

        // Iterate over the data and print to System.out

        //
        // A Metadata object contains multiple Directory objects
        //
        for (Directory directory : metadata.getDirectories()) {

            //
            // Each Directory stores values in Tag objects
            //
            for (Tag tag : directory.getTags()) {
                System.out.println(tag);
            }

            //
            // Each Directory may also contain error messages
            //
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    System.err.println("ERROR: " + error);
                }
            }
        }
    }

    public static void getStatusBarHeight(Context context) {
        int statusBarHeight1 = -1;
        //获取status_bar_height资源的ID
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            //根据资源ID获取响应的尺寸值
            statusBarHeight1 = context.getResources().getDimensionPixelSize(resourceId);
        }
        Log.e("WangJ", "状态栏-方法1:" + statusBarHeight1);
    }
}
