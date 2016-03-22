package example.com.mpdlcamera.AutoRun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.LocalModel.Task;

/**
 * Created by yingli on 2/3/16.
 */
public class ManualUploadService extends Service {

    private static final String TAG = ManualUploadService.class.getSimpleName();

    //  position in waitingImage list


    Task task;

    // pass currentTaskId to service
    private List<String> taskIdList = new ArrayList<>();

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
        Log.i(TAG, "Service onStartComman");


        // prepare taskId
        try {
            String currentTaskId = intent.getStringExtra("currentTaskId");
            if(!taskIdList.contains(currentTaskId)){
            taskIdList.add(currentTaskId);
            }
        }catch (Exception e){
              Log.i(TAG, "e~~~");
        }

        for (String taskId:taskIdList){

            new ManualUploadThread(activity,taskId).start();
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