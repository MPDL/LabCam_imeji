package de.mpg.mpdl.labcam.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.text.TextPaint;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import de.mpg.mpdl.labcam.R;

/**
 * Created by allen on 09/04/15.
 */
public class DeviceStatus {

    private static final String LOG_TAG = DeviceStatus.class.getSimpleName();
    public static List<String> uploadingItemPaths = new ArrayList<>();
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

    public static String metaDataJson(String imagePath, Boolean[] typeList, Context context ){

        String metaDataJsonStr = null;

        File file = new File(imagePath);


        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            metaDataJsonStr =  generateJsonStr(metadata, typeList, context, imagePath);

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
    private static String generateJsonStr(Metadata metadata, Boolean[] typeList, Context context, String imagePath){

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
        int orientation = 0;

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

        if(exifThumbnailDirectory!=null){
            String orientationStr = exifThumbnailDirectory.getString(exifThumbnailDirectory.TAG_ORIENTATION);
            if(orientationStr.contains("90")){
                orientation = 90;
            }else if(orientationStr.contains("180")){
                orientation = 180;
            }else if(orientationStr.contains("270")){
                orientation = 270;
            }else {
                orientation = 0;
            }

        }



        String ocr = "";
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);

        Matrix matrix = new Matrix();
        matrix.postRotate(orientation);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap,bitmap.getWidth(),bitmap.getHeight(),true);
        Bitmap rotatedBitmap = Bitmap.createBitmap(scaledBitmap , 0, 0, scaledBitmap .getWidth(), scaledBitmap .getHeight(), matrix, true);

        ocr = OCRtextHandler.getText(context, rotatedBitmap);


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
            }if(1==1){
                jsonObject.put("ocr", ocr);
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
}
