package example.com.mpdlcamera.Upload;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.preference.PreferenceManager;

import java.util.concurrent.ExecutorService;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;

/**
 * Created by kiran on 29.09.15.
 * The Content Observer method which observes the recent file added to the file system.
 */

public class NewFileObserver extends ContentObserver {

    SharedPreferences settings;


    private Context context;
    private Activity act;
    private SharedPreferences mPrefs;


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

        settings = PreferenceManager.getDefaultSharedPreferences(context);

        String prefOption = settings.getString("status", "");

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        String networkStatus;
        networkStatus = networkInfo.getTypeName();

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
            if (prefOption.equalsIgnoreCase("both") || (prefOption.equalsIgnoreCase("Wifi") && (networkStatus.equalsIgnoreCase("wifi")))) {

                FileUploader fileUploader = new FileUploader(context,act);
                String imageName = item.getFilename();
                mPrefs = context.getSharedPreferences("myPref", 0);
                String collectionId = mPrefs.getString("collectionID", "");

                String imageCollectionName = imageName + collectionId;

                MySQLiteHelper db = new MySQLiteHelper(context);

                Boolean flag = db.getFile(imageCollectionName);

                if(!flag) { // check whether the files is already in the database(if its there, it means the file has been uploaded

                    fileUploader.upload(item);

                }

            }


        }


    }


}
