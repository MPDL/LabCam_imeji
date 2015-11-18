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
 */

public class NewFileObserver extends ContentObserver {

    SharedPreferences settings;


    private Context context;
    private Activity act;
    private SharedPreferences mPrefs;


    FileUploader fileUploader = new FileUploader();


    private ExecutorService queue;

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

    @Override
    public void onChange(boolean selfChange) {

        settings = PreferenceManager.getDefaultSharedPreferences(context);

        String prefOption = settings.getString("status", "");

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
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

            if (prefOption.equalsIgnoreCase("both") || (prefOption.equalsIgnoreCase("Wifi") && (networkStatus.equalsIgnoreCase("wifi")))) {

                FileUploader fileUploader = new FileUploader(context,act);
                String imageName = item.getFilename();
                mPrefs = context.getSharedPreferences("myPref", 0);
                String collectionId = mPrefs.getString("collectionID", "");

                String imageCollectionName = imageName + collectionId;

                MySQLiteHelper db = new MySQLiteHelper(context);

                Boolean b = db.getFile(imageCollectionName);

                if(!b) {

                    fileUploader.upload(item);

                }

            }


        }


    }


}
