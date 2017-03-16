package de.mpg.mpdl.labcam.code.common.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yingli on 2/3/16.
 */
public class ManualUploadService extends Service {

    private static final String TAG = ManualUploadService.class.getSimpleName();

    // pass currentTaskId to service
    private List<Long> taskIdList = new ArrayList<>();

    //

    private Context activity = this;



    public class ServiceBinder extends Binder {

        public ManualUploadService getService() {

            return ManualUploadService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service onBind");
        return new ServiceBinder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // prepare taskId
        try {

            Long currentTaskId = intent.getLongExtra("currentTaskId", 1L);
            if(!taskIdList.contains(currentTaskId)) {
                taskIdList.add(currentTaskId);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        for (Long taskId:taskIdList){
            new ManualUploadThread(activity, taskId).start();
            taskIdList.remove(taskId);
        }

        return Service.START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service onDestroy");
    }
}