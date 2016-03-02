package example.com.mpdlcamera.AutoRun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

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