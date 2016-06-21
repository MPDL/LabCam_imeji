package example.com.mpdlcamera.Upload;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Handler;

import com.activeandroid.query.Select;

import java.util.concurrent.ExecutorService;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
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

    }

    //get latest task
    public static Task getTask() {
        return new Select()
                .from(Task.class)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    //get latest image
    public static Image getImage() {
        return new Select()
                .from(Image.class)
                .orderBy("createTime DESC")
                .executeSingle();
    }
}
