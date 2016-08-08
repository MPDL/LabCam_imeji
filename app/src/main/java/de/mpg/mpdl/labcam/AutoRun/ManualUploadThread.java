package de.mpg.mpdl.labcam.AutoRun;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import retrofit.mime.TypedFile;

/**
 * Created by yingli on 3/2/16.
 */
public class ManualUploadThread extends Thread {


    private static final String TAG = ManualUploadThread.class.getSimpleName();

    List<Image> waitingImages = null;
    List<Image> finishedImages = null;
    List<Image> failedImages = null;

    //  position in waitingImage list

    String currentImageId;
    Task task;

    // SharedPreferences
    private SharedPreferences mPrefs;
    private String apiKey;

    private TypedFile typedFile;
    private String json;

    // handler for toast
    private Handler handler = new Handler();

    private Context context;
    private String currentTaskId;
    private String collectionID;

    public ManualUploadThread(Context context,String currentTaskId) {
        super("ManualUploadThread");
        this.context = context;
        this.currentTaskId = currentTaskId;
    }

    public void run() {
        //Code
        Log.i(TAG,"Thread --> startUpload()");
        checkAndUpload checkMateData = new checkAndUpload(context,currentTaskId);
        checkMateData.run();
    }

}
