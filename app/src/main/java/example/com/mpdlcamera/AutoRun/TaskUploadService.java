package example.com.mpdlcamera.AutoRun;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
 * Created by yingli on 1/26/16.
 */
public class TaskUploadService extends Service{

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
    List<Image>  finishedImages = null;

    private SharedPreferences mPrefs;
    private String username;
    private String userId;
    private String apiKey;
    private String collectionID;
    public TypedFile typedFile;
    String json;

    //get context
    private Context activity = this;

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class AutoUploadServiceBinder extends Binder {
        public TaskUploadService  getService() {
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
        super.onDestroy();
        Log.v(TAG, "TaskUploadService Destroy!");
    }

    @Override
    public void onCreate() {

        super.onCreate();


                Log.v(TAG, "TaskUploadService onCreate!");


                // prepare auth for upload
                mPrefs = this.getSharedPreferences("myPref", 0);
                username = mPrefs.getString("username", "");
                apiKey = mPrefs.getString("apiKey", "");
                userId = mPrefs.getString("userId", "");

                //set task
                try{
                    task =  DeviceStatus.getAuTask(userId);
                    currentTaskId = task.getTaskId();
                    Log.i(TAG, "currentTaskId" + currentTaskId);
                    finishedNum = task.getFinishedItems();
                    Log.v(TAG,"onCreate getFinishedItems: "+finishedNum);
                }
                catch (Exception e){
                    // no task or exception in query
                    Log.v(TAG,"no task or exception in query");
                }
                // set collectionId
                if(task!=null) {
                    collectionID = task.getCollectionId();
                    Log.v(TAG+"onCreate","collectionID set");
                }else{
                    Log.v(TAG,"mTask is null, can't get collectionID");
                }
                // auto task is WAITING

                    startUpload();

            }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            Log.v(TAG, "onStartCommand!");

            // prepare auth for upload
            mPrefs = this.getSharedPreferences("myPref", 0);
            username = mPrefs.getString("username", "");
            apiKey = mPrefs.getString("apiKey", "");

            //set task
            try{
                task =  DeviceStatus.getAuTask(userId);
                currentTaskId = task.getTaskId();
                Log.i(TAG, "currentTaskId: " + currentTaskId);
                finishedNum = task.getFinishedItems();
                Log.v("~~~","onCreate getFinishedItems: "+finishedNum);
            }
            catch (Exception e){
                // no task or exception in query
                Log.v(TAG,"no task or exception in query");
            }
            // set collectionId
            if(task!=null) {
                collectionID = task.getCollectionId();
                Log.v(TAG,"collectionID set");
            }else{
                Log.v(TAG,"mTask is null, can't get collectionID");
            }

            if(!taskIsStopped()) {
                Log.v(TAG,"not stopped");
                Log.v(TAG,task.getState());
                // auto task is WAITING
                if (task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                    //start uploading
                    startUpload();
                }
            }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean taskIsStopped (){
        task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

        try{
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))){
            Log.v(TAG,"taskIsStopped");
            return true;
        }else{
            Log.v(TAG,"task is not stopped");
            return false;
        }}catch (Exception e){
            Log.e(TAG,"taskIsStopped exception");

            return false;
        }

    }

    private void startUpload() {
        Log.v(TAG, "startUpload()");

        if(!taskIsStopped()) {
            try {
                /** WAITING, INTERRUPTED, STARTED, (FINISHED + STOPPED) **/
                waitingImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").execute();
                finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
            } catch (Exception e) {
            }

            //DELETE TESTING
            Log.i(TAG, "waitingImages: " + waitingImages.size());
            Log.i(TAG, "finishedImages "+finishedImages.size());
            Log.i(TAG, "totalImages: " + task.getTotalItems());
            task.setFinishedItems(finishedImages.size());
            task.save();

            if (waitingImages != null && waitingImages.size() > 0) {

                //upload begin with the first in list

                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.WAITING)).orderBy("createTime DESC").executeSingle();
                String imageState = image.getState();
                String filePath = image.getImagePath();
                currentImageId = image.getImageId();
                Log.e(TAG, currentImageId);


                if (imageState.equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                    // TODO:upload image
                    String jsonPart1 = "\"collectionId\" : \"" +
                            collectionID +
                            "\"";

                    typedFile = new TypedFile("multipart/form-data", new File(filePath));
                    json = "{" + jsonPart1 + "}";
                    Log.v(TAG, "start uploading: " + filePath);
//                    image.setState(String.valueOf(DeviceStatus.state.STARTED));
                    RetrofitClient.uploadItem(typedFile, json, callback, apiKey);

                } else {
                    Log.e(TAG, "illegal imageState:" + imageState);
                }
            }
        }
    }
    private void upload(Image image){

        try {
            /** WAITING, INTERRUPTED, STARTED, (FINISHED + STOPPED) **/
            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
        } catch (Exception e) {
        }

        //DELETE TESTING
        Log.i(TAG, "finishedImages "+finishedImages.size());
        Log.i(TAG, "totalImages: " + task.getTotalItems());
        task.setFinishedItems(finishedImages.size());
        task.save();


            if(!taskIsStopped()){
            //upload image
            String imageState = image.getState();
            String filePath = image.getImagePath();
            currentImageId = image.getImageId();
            Log.e(TAG, "upload" + currentImageId);

            if (imageState.equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                // TODO:upload image
                String jsonPart1 = "\"collectionId\" : \"" +
                        collectionID +
                        "\"";

                typedFile = new TypedFile("multipart/form-data", new File(filePath));
                json = "{" + jsonPart1 + "}";
                Log.v(TAG, "start uploading: " + filePath);
//                image.setState(String.valueOf(DeviceStatus.state.STARTED));
                RetrofitClient.uploadItem(typedFile, json, callback, apiKey);
            }
        }
    }

    //callback
    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            if(!taskIsStopped()) {
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });

                Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

                Log.e(TAG, currentImageId);
                Image currentImage = new Select().from(Image.class).where("imageId = ?", currentImageId).executeSingle();
                if (currentImage == null) {
                    Log.v(TAG, currentImageId + "is not in database, task might be resumed");
                    // task is deleted/resumed so break and left callback
                } else {
                    currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
                    currentImage.save();

                    //TODO: ReWrite "remove after upload" function later
                    mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

            /*
                Delete the file if the setting "Remove the photos after upload" is On
             */
                    if (mPrefs.contains("RemovePhotosAfterUpload")) {
                        if (mPrefs.getBoolean("RemovePhotosAfterUpload", true)) {

                            File file = typedFile.file();
                            Boolean deleted = file.delete();
                            Log.v(TAG, "deleted:" + deleted);
                        }
                    }
                    //TODO: remove picture
//            adapter.notifyDataSetChanged();


                    /** move on to next **/

                    try {
                        finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
                    } catch (Exception e) {
                    }

                    //DELETE TESTING
                    Log.i(TAG, "finishedImages " + finishedImages.size());
                    Log.i(TAG, "totalImages: " + task.getTotalItems());
                    task.setFinishedItems(finishedImages.size());
                    task.save();

                    int finishedNum = finishedImages.size();
                    int totalNum = task.getTotalItems();

                    Log.i(TAG, "totalNum: " + totalNum + "  finishedNum" + finishedNum);
                    if (totalNum > finishedNum) {
                        Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").executeSingle();
                        if (image != null) {
                            upload(image);
                            Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                public void run() {
                                    // Actions to do after 3 seconds
                                }
                            }, 3000);
                        }
                    } else {
                        task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                        Log.i(TAG, "task finished");
                    }
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {

//            if(error.getResponse().getStatus() == 403 || error.getResponse().getStatus() == 422 ) {
//                task.setState(String.valueOf(DeviceStatus.state.INTERRUPTED));
//                task.save();
//                Log.e(TAG,task.getState());
//                isInterrupted = true;
//            }


            if (!taskIsStopped()) {
                Image currentImage = new Select().from(Image.class).where("imageId = ?", currentImageId).executeSingle();
                //TODO:what to do with interrupted

//
//                    currentImage.setState(String.valueOf(DeviceStatus.state.INTERRUPTED));
//                    currentImage.setLog(error.getKind().name());
//                    Log.e(TAG, error.getKind().name());


                if (error == null || error.getResponse() == null) {
                    OttoSingleton.getInstance().post(new UploadEvent(null));
                    if (error.getKind().name().equalsIgnoreCase("NETWORK")) {

                // NETWORK disconnect situation don't need to change image state
                        handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
//                                Toast.makeText(activity, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {

                        handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    OttoSingleton.getInstance().post(
                            new UploadEvent(error.getResponse().getStatus()));
                    String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                    if (jsonBody.contains("already exists")) {

                        handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
//                                Toast.makeText(activity, "Photo already exists", Toast.LENGTH_SHORT).show();
                            }
                        });

                        try{
                        currentImage.setLog(error.getKind().name() + " already exists");
                        currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
                        currentImage.save();}catch (Exception e){}

                    } else {

                        handler = new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                // save state changes
                currentImage.save();
                Log.v(TAG, String.valueOf(error));

                //TODO: continue upload

                try {
                    finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
                    Log.e(TAG, "finishedImages " + finishedImages.size());
                    task.setFinishedItems(finishedImages.size());
                    task.save();


                    /** move on to next **/
                    int finishedNum = task.getFinishedItems();
                    int totalNum = task.getTotalItems();
                    Log.i(TAG, "totalNum: " + totalNum + "  finishedNum" + finishedNum);
                    if (totalNum > finishedNum) {
                        // continue anyway
                        Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").executeSingle();
                        if (image != null) {
                            upload(image);
                        }
                    } else {
                        task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                        Log.i(TAG, "task finished");
                    }
                } catch (Exception e) {
                }
            }
        }
    };


}
