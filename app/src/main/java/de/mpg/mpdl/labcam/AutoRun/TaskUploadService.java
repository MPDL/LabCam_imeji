package de.mpg.mpdl.labcam.AutoRun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;

/**
 * Created by yingli on 1/26/16.
 */
public class TaskUploadService extends Service {

    private static String TAG = TaskUploadService.class.getSimpleName();

    // get last Task, may need to add a param to set task
    private Task task;
    private int finishedNum;
    private Long currentTaskId;

    private SharedPreferences mPrefs;
    private String userId;
    private String serverName;

    //get context
    private Context activity = this;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class AutoUploadServiceBinder extends Binder {
        public TaskUploadService getService() {
            return TaskUploadService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new AutoUploadServiceBinder();

    @Override
    public void onDestroy() {
        Log.e(TAG, "TaskUploadService Destroy!");
        super.onDestroy();
    }

    @Override
    public void onCreate() {

        super.onCreate();

        Log.v(TAG, "TaskUploadService onCreate!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.v(TAG, "onStartCommand!");

        // prepare auth for upload
        mPrefs = this.getSharedPreferences("myPref", 0);
        userId = mPrefs.getString("userId", "");
        serverName = mPrefs.getString("serverName", "");


        //set task
        task = DBConnector.getAuTask(userId, serverName);

        if (task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))) {
            return super.onStartCommand(intent, flags, startId);
        }
        //set currentTaskId
        currentTaskId = task.getId();
        Log.i(TAG, "currentTaskId: " + currentTaskId);
        finishedNum = task.getFinishedItems();
        Log.v("~~~", "onCreate getFinishedItems: " + finishedNum);

        checkAndUpload checkMateData = new checkAndUpload(activity, currentTaskId);
        checkMateData.run();

        return super.onStartCommand(intent, flags, startId);
    }

}
