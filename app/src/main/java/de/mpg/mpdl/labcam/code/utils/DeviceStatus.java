package de.mpg.mpdl.labcam.code.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import com.drew.metadata.exif.GpsDirectory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import de.mpg.mpdl.labcam.LabCam;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;

/**
 * Created by allen on 09/04/15.
 */
public class DeviceStatus {

    private static final String LOG_TAG = DeviceStatus.class.getSimpleName();
    public static List<String> uploadingItemPaths = new ArrayList<>();
    public static final String username = "";
    public static final String password = "";
 //   public static final String BASE_URL= "";
    public static final String BASE_URL = "http://qa-imeji.mpdl.mpg.de/imeji/rest/";

//    public static final String BASE_URL = "https://test-gluons.mpdl.mpg.de/imeji/rest/";

    private static String[] labCamTemplateProfileLabels = {"Make",
            "Model",
            "ISO Speed Ratings",
            "Creation Date",
            "Geolocation",
            "GPS Version ID",
            "Sensing Method",
            "Aperture Value",
            "Color Space",
            "Exposure Time",
            "Note",
            "OCR"};
    private static String[] labCamTemplateProfileTypes ={"text",
            "text",
            "number",
            "date", "geolocation", "text", "text", "text", "text", "text", "text", "text"};


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

    public static List<String> getUploadingItemPaths() {
        return uploadingItemPaths;
    }

    public static void setUploadingItemPaths(List<String> uploadingItemPaths) {
        DeviceStatus.uploadingItemPaths = uploadingItemPaths;
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
        WAITING, STARTED, STOPPED, FAILED,FINISHED
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

    public static String metaDataJson(String collectionId, String imagePath, Boolean[] typeList, boolean addLicense, String ocrText, String userId, String serverName){

        String metaDataJsonStr = null;

        File file = new File(imagePath);


        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            metaDataJsonStr =  generateJsonStr(collectionId, metadata, typeList,addLicense, imagePath, ocrText, userId, serverName);

            print(metadata);
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NegativeArraySizeException e){
            e.printStackTrace();
        }
        return metaDataJsonStr;
    }

    private static String generateJsonStr(String collectionId, Metadata metadata, Boolean[] typeList, boolean addLicense, String imagePath, String ocrText, String userId, String serverName){

        String metaDataJsonStr = null;

        // obtain the Exif directory
        ExifSubIFDDirectory exifSubIFDDirectory
                = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);

        ExifIFD0Directory exifIFD0Directory
                = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

        GpsDirectory gpsDirectory
                = metadata.getFirstDirectoryOfType(GpsDirectory.class);

        ExifThumbnailDirectory exifThumbnailDirectory
                = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);



        String makeStr = "";
        String modelStr = "";
        int ISOSpeedRating = 0;
        String CreationDateStr = "";
        String latitudeStr = "";
        String longitudeStr = "";
        String GPSVersionIDStr = "";
        String ExposureTimeStr = "";
        String SensingMethodStr = "";
        String ApertureValueStr = "";
        String ColorSpaceStr = "";
        String note ="";
        int orientation = 0;
        String ocr = "";

        if(exifIFD0Directory!=null){
            makeStr = (exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE) != null) ? exifIFD0Directory.getString(ExifIFD0Directory.TAG_MAKE) : "";
            modelStr = (exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL)!= null) ? exifIFD0Directory.getString(ExifIFD0Directory.TAG_MODEL) : "";
            // create date use TAG_DATE
            Date date = exifIFD0Directory.getDate(ExifIFD0Directory.TAG_DATETIME);
            SimpleDateFormat formatterShort = new SimpleDateFormat("yyyy-MM-dd");
            if(date != null)
                CreationDateStr = formatterShort.format(date);
        }

        if(exifSubIFDDirectory!=null){
            ISOSpeedRating =  (exifSubIFDDirectory.getString(ExifIFD0Directory.TAG_ISO_EQUIVALENT) !=null) ? Integer.parseInt(exifSubIFDDirectory.getString(ExifIFD0Directory.TAG_ISO_EQUIVALENT)) :-1;
            SensingMethodStr = (exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_SENSING_METHOD) != null ) ? exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_SENSING_METHOD) : "";
            ApertureValueStr = (exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE) != null ) ? exifSubIFDDirectory.getString(ExifSubIFDDirectory.TAG_APERTURE) : "";
            ColorSpaceStr = (exifSubIFDDirectory.getString(exifSubIFDDirectory.TAG_COLOR_SPACE)  != null) ? exifSubIFDDirectory.getString(exifSubIFDDirectory.TAG_COLOR_SPACE) : "";
            ExposureTimeStr = (exifSubIFDDirectory.getString(exifSubIFDDirectory.TAG_EXPOSURE_TIME) != null) ? exifSubIFDDirectory.getString(exifSubIFDDirectory.TAG_EXPOSURE_TIME) : "";
        }

        if(gpsDirectory!=null && gpsDirectory.getGeoLocation()!=null ) {
            latitudeStr = String.valueOf(gpsDirectory.getGeoLocation().getLatitude());
            longitudeStr = String.valueOf(gpsDirectory.getGeoLocation().getLongitude());
            GPSVersionIDStr = gpsDirectory.getString(gpsDirectory.TAG_VERSION_ID);
        }

        //note
        Image image = DBConnector.getImageByPath(imagePath, userId, serverName);

        // if image object not exist, there is no note.
        if(image!=null && image.getNoteId() != null && DBConnector.getNoteById(image.getNoteId(), userId, serverName) != null)
            note = DBConnector.getNoteById(image.getNoteId(), userId, serverName).getNoteContent();
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("collectionId", collectionId);
            JSONArray mdArray = new JSONArray();
            if(typeList[0]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[0]);
                mdObj.put(labCamTemplateProfileTypes[0], "make");
                mdArray.put(mdObj);
            }
            if(typeList[1]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[1]);
                mdObj.put(labCamTemplateProfileTypes[1], modelStr);
                mdArray.put(mdObj);
            }
            if(typeList[2]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[2]);
                mdObj.put(labCamTemplateProfileTypes[2], ISOSpeedRating);
                mdArray.put(mdObj);
            }
            if(typeList[3]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[3]);
                mdObj.put(labCamTemplateProfileTypes[3], CreationDateStr);
                mdArray.put(mdObj);
            }
            if(typeList[4]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[4]);
                mdObj.put("longitude", latitudeStr);
                mdObj.put("latitude", longitudeStr);
                mdArray.put(mdObj);

            }if(typeList[5]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[5]);
                mdObj.put(labCamTemplateProfileTypes[5], GPSVersionIDStr);
                mdArray.put(mdObj);
            }if(typeList[6]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[6]);
                mdObj.put(labCamTemplateProfileTypes[6], SensingMethodStr);
                mdArray.put(mdObj);
            }if(typeList[7]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[7]);
                mdObj.put(labCamTemplateProfileTypes[7], ApertureValueStr);
                mdArray.put(mdObj);
            }if(typeList[8]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[8]);
                mdObj.put(labCamTemplateProfileTypes[8], ColorSpaceStr);
                mdArray.put(mdObj);
            }if(typeList[9]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[9]);
                mdObj.put(labCamTemplateProfileTypes[9], ExposureTimeStr);
                mdArray.put(mdObj);
            }if(typeList[10]) {
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[10]);
                mdObj.put(labCamTemplateProfileTypes[10], note);
                mdArray.put(mdObj);
            }if(typeList[11]){
                JSONObject mdObj = new JSONObject();
                mdObj.put("index",labCamTemplateProfileLabels[11]);
                mdObj.put(labCamTemplateProfileTypes[11], ocrText);
                mdArray.put(mdObj);
            }
            jsonObject.put("metadata", mdArray);
            jsonObject = addLicenseJson(addLicense, jsonObject);
            metaDataJsonStr = jsonObject.toString();
            Log.e(LOG_TAG, metaDataJsonStr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return metaDataJsonStr;
    }

    private static JSONObject addLicenseJson(boolean addLicense, JSONObject jsonObject){
        if(addLicense){
            try {
                JSONObject licenseObj = new JSONObject()
                        .put("name", "Copyright " +
                                PreferenceUtil.getString(LabCam.getContext(), Constants.SHARED_PREFERENCES,Constants.GIVEN_NAME, "")+
                                PreferenceUtil.getString(LabCam.getContext(), Constants.SHARED_PREFERENCES,Constants.FAMILY_NAME, ""));

                jsonObject.put("licenses", new JSONArray().put(licenseObj));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonObject;
    }

    // print metadata for function 'metaDataJson'
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

     public static String SaveImage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+ n +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.e(LOG_TAG, "rotatedBitmap saved");
            return root + "/saved_images" + fname;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /*********************************** REFACTORING **************************************/


    /**
     * 检测当的网络（WLAN、3G/2G）状态
     * @param context Context
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    return true;
                }
            }
        }
        return false;
    }

}
