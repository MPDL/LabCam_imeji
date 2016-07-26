package example.com.mpdlcamera.AutoRun;

import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.squareup.otto.Produce;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.LocalAlbum;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
import example.com.mpdlcamera.Utils.UiElements.Notification.NotificationID;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;

/**
 * Created by yingli on 1/26/16.
 */
public class TaskUploadService extends Service {

    Context mContext = this;
    private static String TAG = TaskUploadService.class.getSimpleName();


    // get last Task, may need to add a param to set task
    private Task task;
    private int finishedNum;
    private String currentTaskId;
    private String currentImageId;
    private Handler handler = new Handler();

    // Images
    List<Image> waitingImages = null;
    List<Image> failedImages = null;
    List<Image> finishedImages = null;

    private SharedPreferences mPrefs;
    private String username;
    private String userId;
    private String apiKey;
    private String serverName;
    private String collectionID;
    public TypedFile typedFile;
    String json;

    //get context
    private Context activity = this;

    //check type
    Boolean[] checkTypeList = {false, false, false, false, false, false, false};


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
        username = mPrefs.getString("username", "");
        apiKey = mPrefs.getString("apiKey", "");
        userId = mPrefs.getString("userId", "");
        serverName = mPrefs.getString("server", "");


        //set task
        try {
            task = DeviceStatus.getAuTask(userId, serverName);

            if (task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))) {
                return super.onStartCommand(intent, flags, startId);
            }
            //set currentTaskId
            currentTaskId = task.getTaskId();
            Log.i(TAG, "currentTaskId: " + currentTaskId);
            finishedNum = task.getFinishedItems();
            Log.v("~~~", "onCreate getFinishedItems: " + finishedNum);

            checkAndUpload checkMateData = new checkAndUpload(activity, currentTaskId);
            checkMateData.run();
        } catch (Exception e) {
            // no task or exception in query
            Log.v(TAG, "no task or exception in query");
        }


        return super.onStartCommand(intent, flags, startId);
    }

}
