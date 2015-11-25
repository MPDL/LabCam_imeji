package example.com.mpdlcamera.Upload;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.concurrent.ExecutorService;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;

/**
 * Created by kiran on 29.09.15.
 * The Content Observer method which observes the recent file added to the file system.
 */

public class NewFileObserver extends ContentObserver {

    SharedPreferences settings;

    MySQLiteHelper db;

    SharedPreferences nPrefs;

    private Context context;
    private Activity act;
    private SharedPreferences mPrefs;
    private String collectionID;

    FileUploader fileUploader = new FileUploader();


    private ExecutorService queue;

    /*
        constructor method for the file observer
     */
    public NewFileObserver(Handler handler, MainActivity application) {
        super(handler);
        this.context = application.getBaseContext();
        this.act = (Activity) application;

    }


    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public NewFileObserver(Handler handler, ExecutorService queue) {
        super(handler);
        this.queue = queue;
    }

    /*
        method which runs when there is any change in the file system
     */
    @Override
    public void onChange(boolean selfChange) {

        Log.v("observer","started");

        settings = PreferenceManager.getDefaultSharedPreferences(context);

        String prefOption = settings.getString("status", "");

        nPrefs = context.getSharedPreferences("myPref", 0);
        collectionID = nPrefs.getString("collectionID", "");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        String networkStatus = null;
        if(networkInfo != null) {
            networkStatus = networkInfo.getTypeName();
        }

        LatestImage imageLatest = new LatestImage(context);
        int imageId = imageLatest.getId();

        if (imageId == -1) {
            return;
        }

        DataItem item = imageLatest.getLatestItem();

        if (item == null) {
            return;
        } else {

            //Upload the files only when the settings is not "manual" so check for the other two options
            //  point:
            do {
                ConnectivityManager connectivityManager1 = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo1 = connectivityManager1.getActiveNetworkInfo();
                if(networkInfo1 != null) {
                    networkStatus = networkInfo1.getTypeName();
                }
                if (networkStatus != null) {
                    if (prefOption.equalsIgnoreCase("both") || (prefOption.equalsIgnoreCase("Wifi") && (networkStatus.equalsIgnoreCase("wifi")))) {

                        FileUploader fileUploader = new FileUploader(context, act);
                        String imageName = item.getFilename();
                        mPrefs = context.getSharedPreferences("myPref", 0);
                        String collectionId = mPrefs.getString("collectionID", "");

                        String imageCollectionName = item.getFilename() + collectionId;

                        db = new MySQLiteHelper(context);

                        String fileStatus = db.getFileStatus(imageCollectionName);

                        if (fileStatus.equalsIgnoreCase("not present") || fileStatus.equalsIgnoreCase("failed")) { // check whether the files is already in the database(if its there, it means the file has been uploaded

                            fileUploader.upload(item);

                            //   SharedPreferences filePreferences = context.
                            String fileNamePlusId = item.getFilename() + collectionID;

                            if (!(db.getFileStatus(fileNamePlusId).equalsIgnoreCase("not present"))) {

                                db.updateFileStatus(fileNamePlusId, "uploaded");
                                //  FileId fileId = new FileId(fileNamePlusId, "uploaded");

                            } else {
                                FileId fileId = new FileId(fileNamePlusId, "uploaded");
                                db.insertFile(fileId);

                            }
                            // FileId fileId = new FileId(fileNamePlusId,"uploading");
                            // db.insertFile(fileId);

                        }

                    }

                }
            }
            while (networkStatus == null);

//            else if(networkStatus == null ) {
//
//                    break point;
//            }


        }
} }
