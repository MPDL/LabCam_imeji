package de.mpg.mpdl.labcam.code.common.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

/**
 * Created by yingli on 1/26/16.
 */
public class TaskUploadService extends Service {

    private static String TAG = TaskUploadService.class.getSimpleName();

    // get last Task, may need to add a param to set task
    private Task task;
    private int finishedNum;
    private Long currentTaskId;

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
        userId =  PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        //set task
        task = DBConnector.getAuTask(userId, serverName);
        if (task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))) {
            return super.onStartCommand(intent, flags, startId);
        }
        //set currentTaskId
        currentTaskId = task.getId();
        finishedNum = task.getFinishedItems();
        UploadService checkMateData = new UploadService(activity, currentTaskId);
        checkMateData.run();

        return super.onStartCommand(intent, flags, startId);
    }

}
