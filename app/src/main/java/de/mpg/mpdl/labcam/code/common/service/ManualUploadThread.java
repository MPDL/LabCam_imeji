package de.mpg.mpdl.labcam.code.common.service;

import android.content.Context;
import android.util.Log;

/**
 * Created by yingli on 3/2/16.
 */
public class ManualUploadThread extends Thread {

    private static final String TAG = ManualUploadThread.class.getSimpleName();

    private Context context;

    private Long currentTaskId;

    public ManualUploadThread(Context context,Long currentTaskId) {
        super("ManualUploadThread");
        this.context = context;
        this.currentTaskId = currentTaskId;
    }

    public void run() {
        //Code
        Log.i(TAG,"Thread --> startUpload()");
        UploadService checkMateData = new UploadService(context, currentTaskId);
        checkMateData.run();
    }

}
