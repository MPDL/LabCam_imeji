package de.mpg.mpdl.labcam.AutoRun;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;

import java.util.List;

import retrofit.mime.TypedFile;

/**
 * Created by yingli on 3/2/16.
 */
public class ManualUploadThread extends Thread {

    private static final String TAG = ManualUploadThread.class.getSimpleName();

    private Context context;
    private String currentTaskId;

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
