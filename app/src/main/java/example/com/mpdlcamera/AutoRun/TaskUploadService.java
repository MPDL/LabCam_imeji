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
    List<Image> failedImages = null;
    List<Image>  finishedImages = null;

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
        Log.e(TAG, "TaskUploadService Destroy!");
        super.onDestroy();
    }

    @Override
    public void onCreate() {

        super.onCreate();

                Log.v(TAG, "TaskUploadService onCreate!");

//                // prepare auth for upload
//                mPrefs = this.getSharedPreferences("myPref", 0);
//                username = mPrefs.getString("username", "");
//                apiKey = mPrefs.getString("apiKey", "");
//                userId = mPrefs.getString("userId", "");
//
//                //set task
//                try{
//                    // AuTask can not be more than 1 (<=1)
//                    task =  DeviceStatus.getAuTask(userId);
//                    // set currentTask id
//                    currentTaskId = task.getTaskId();
//
//                    // task already failed
//                    if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
//                        return;
//                    }
//
//                    Log.i(TAG, "currentTaskId" + currentTaskId);
//                    finishedNum = task.getFinishedItems();
//                    Log.v(TAG,"onCreate getFinishedItems: "+finishedNum);
//                }
//                catch (Exception e){
//                    // no task or exception in query
//                    Log.v(TAG,"no task or exception in query");
//                }
//                // set collectionId
//                if(task!=null) {
//                    collectionID = task.getCollectionId();
//                    Log.v(TAG+"onCreate","collectionID set");
//                }else{
//                    Log.v(TAG,"mTask is null, can't get collectionID");
//                    return;
//                }
//                // task exist,then start task
//                startUpload();
            }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

            Log.v(TAG, "onStartCommand!");

            // prepare auth for upload
            mPrefs = this.getSharedPreferences("myPref", 0);
            username = mPrefs.getString("username", "");
            apiKey = mPrefs.getString("apiKey", "");
            userId = mPrefs.getString("userId", "");
            serverName = mPrefs.getString("server","");


            //set task
            try{
                task =  DeviceStatus.getAuTask(userId,serverName);

                if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                    return super.onStartCommand(intent, flags, startId);
                }
                //set currentTaskId
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

                if(!taskIsStopped()) {
                    Log.v(TAG,"not stopped");
                    Log.v(TAG,task.getState());
                    // auto task is WAITING
                    if (task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                        //start uploading
                        startUpload();
                    }
                }

            }else{
                Log.v(TAG,"mTask is null, can't get collectionID");
            }

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean taskIsStopped (){

        try{
            task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
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
                // search in database, count the number of images that have state = waiting/finished
                // update the finishedItems number in task
            waitingImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").execute();
            failedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FAILED)).orderBy("RANDOM()").execute();
            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();

            task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

            // joinList is a list join waiting and failed
            List<Image> joinList = new ArrayList<>();
            joinList.addAll(waitingImages);
            joinList.addAll(failedImages);

            // task not exist
            if(finishedImages==null||task==null) {
                return;
            }

            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                return;
            }

            task.setFinishedItems(finishedImages.size());
            task.save();

            if (joinList != null && joinList.size() > 0) {

                // look into database, get first image of a task in create time desc order
                // upload begin with the first in list
                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state != ?", String.valueOf(DeviceStatus.state.FINISHED)).where("state != ?", String.valueOf(DeviceStatus.state.STARTED)).orderBy("createTime DESC").executeSingle();
                String imageState = image.getState();
                String filePath = image.getImagePath();
                // important: set currentImageId, so that in the callback can find it
                currentImageId = image.getImageId();
                Log.e(TAG, "---------------------------");
                Log.e(TAG, "==> step 1");
                Log.e(TAG, "prepare upload picture");
                Log.e(TAG, "currentImageId: "+currentImageId);
                Log.e(TAG, "filePath: "+filePath);
                Log.e(TAG, "createTime: "+ image.getCreateTime());
                Log.e(TAG, "==> step 2");


                // TODO:upload image
                String jsonPart1 = "\"collectionId\" : \"" +
                        collectionID +
                        "\"";

                typedFile = new TypedFile("multipart/form-data", new File(filePath));
                json = "{" + jsonPart1 + "}";
                Log.e(TAG, "start uploading~ " + filePath);
                Log.e(TAG, "TO remote :"+collectionID + "from local:"+ filePath);
                Log.e(TAG, "==> step 3");

                // image set state stated
                image.setState(String.valueOf(DeviceStatus.state.STARTED));
                Log.e(TAG, "imageName! "+image.getImageName());
                Log.e(TAG, "status! " + image.getState());
                RetrofitClient.uploadItem(typedFile, json, callback, apiKey);
                Log.e(TAG, "2imageName! "+image.getImageName());
                Log.e(TAG, "2status! " + image.getState());

            }
        }
    }
    private void upload(Image image){

        if(taskIsStopped()){
            return;
        }

        // prepare image
        String imageState = image.getState();
        String filePath = image.getImagePath();
        currentImageId = image.getImageId();
        Log.e(TAG, "upload" + currentImageId);

        // upload image
        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";

        typedFile = new TypedFile("multipart/form-data", new File(filePath));
        json = "{" + jsonPart1 + "}";
        Log.v(TAG, "start uploading: " + filePath);

        RetrofitClient.uploadItem(typedFile, json, callback, apiKey);
        image.setState(String.valueOf(DeviceStatus.state.STARTED));

    }

    private void notification(){
        //default ID for the notification (auto)
        int mNotificationId = 001;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);

        builder.setSmallIcon(R.drawable.icon_app);
        builder.setContentTitle("LabCam");

        if(("AU").equalsIgnoreCase(task.getUploadMode())){
            String taskInfo = "Automatic upload success";
            builder.setContentText(taskInfo);
        }else{
            // Sets an ID for the notification
            mNotificationId = NotificationID.getID();
            String taskInfo = task.getTotalItems()+ " photo(s) uploaded successfully";
            builder.setContentText(taskInfo);
        }

        Intent intent = new Intent(mContext, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, builder.build());
    }

    //callback
    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {
            Log.e(TAG, "currentImageId: "+currentImageId);
            Log.e(TAG, "upload success");
            Log.e(TAG,"collection:"+ collectionID);

            Log.e(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

            Image currentImage = new Select().from(Image.class).where("imageId = ?", currentImageId).executeSingle();
            if (currentImage == null) {
                Log.e(TAG, currentImageId + "is not in database, task might be resumed");
                // task is deleted/resumed so break and left callback
                return;
            }

            // set image state finished
            currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
            currentImage.save();

            mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();

            task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

            if(finishedImages==null||task==null) {
                Log.e(TAG,"finishedImages==null||task==null");
                return;
            }

            task.setFinishedItems(finishedImages.size());
            task.save();

            Log.e(TAG, "x.1");

            /** move on to next **/
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))){
                Log.e(TAG,"task stopped");
                return;
            }
            // Failed task ignore
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                Log.e(TAG,"task failed");
                return;
            }

            int finishedNum = finishedImages.size();
            int totalNum = task.getTotalItems();

            Log.e(TAG,"x.2");

            Log.e(TAG, "totalNum: " + totalNum + "  finishedNum" + finishedNum);
            if (totalNum > finishedNum) {
                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state != ?", String.valueOf(DeviceStatus.state.FINISHED)).where("state != ?", String.valueOf(DeviceStatus.state.STARTED)).orderBy("RANDOM()").executeSingle();
                if (image != null) {
                    Log.e(TAG,"x.3");
                    upload(image);
                }
            } else {
                /** delete finished tasks before reset Au task **/
                new Delete().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).execute();

                task.setEndDate(DeviceStatus.dateNow());
                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.setTotalItems(0);
                task.setFinishedItems(0);
                task.save();

                Log.e(TAG, "getTotalItems:" + task.getTotalItems());
                Log.e(TAG, "getFinishedItems:" + task.getFinishedItems());

                //push local notification
                notification();
                Log.e(TAG, "task finished");
            }
        }

        @Override
        public void failure(RetrofitError error) {

            Log.e(TAG, "currentImageId: "+currentImageId);
            Log.e(TAG, "upload failure");
            Log.e(TAG,"collection:"+ collectionID);

            /** get currentImage by currentImageId
             *  Image can be any state
             * **/
            Image currentImage = new Select().from(Image.class).where("imageId = ?", currentImageId).executeSingle();
            //TODO:what to do with interrupted
            if(currentImage!=null){
                //do sth
                currentImage.setState(String.valueOf(DeviceStatus.state.FAILED));
            }else {
                Log.v(TAG,"currentImage:"+ currentImageId+ "is null");
                return;
            }

            // error kinds: "network", 403, 422, other
            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                if (error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    Log.e(TAG,"network error");
                } else {
                }
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));

                int statusCode = error.getResponse().getStatus();
                switch (statusCode){
                    case 403:
                        // set TASK state failed, print log
                        try{
                            task.setEndDate(DeviceStatus.dateNow());
                            task.setState(String.valueOf(DeviceStatus.state.FAILED));
                            task.save();
                            Log.e(TAG, collectionID + "forbidden");
                            handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, "Unauthorized to upload", Toast.LENGTH_SHORT).show();
                                }
                            });
                            return;
                        }catch (Exception e){}
                        break;
                    case 422:
                        String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        if (jsonBody.contains("already exists")) {
                            // set currentImage state finished, print log
                            try{
                                currentImage.setLog(error.getKind().name() + " already exists");
                                currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
                                currentImage.save();
                                Log.e(TAG, currentImage.getImageName() + "  already exists");
                            }catch (Exception e){}
                        }else {
                            // case: collectionId not exist, Task failed
                            try {
                                currentImage.setLog(error.getKind().name() + " collectionId not exist, no such folder");
                                task.setEndDate(DeviceStatus.dateNow());
                                task.setState(String.valueOf(DeviceStatus.state.FAILED));
                                task.save();
                                Log.e(TAG, currentImage.getImageName() + " collectionId not exist, no such folder");
                                handler=new Handler(Looper.getMainLooper());
                                handler.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(activity, "remote collectionId not exist, no such folder", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            }catch (Exception e){}
                        }
                        break;
                    default:
                        // 400 401 500 ...
                        // set currentImage state finished, print log
                        try{
                            currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
                            currentImage.save();
                        }catch (Exception e){}
                        Log.e(TAG, currentImage.getImageName() + "failed, code: "+ statusCode);
                }
            }
            // save state changes
            currentImage.save();
            Log.v(TAG, String.valueOf(error));

            //TODO: continue upload
            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
            Log.e(TAG, "finishedImages " + finishedImages.size());

            task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

            if(finishedImages==null||task==null) {
                Log.e(TAG,"finishedImages==null||task==null");
                return;
            }
            task.setFinishedItems(finishedImages.size());
            task.save();


            if (taskIsStopped()) {
                Log.e(TAG,"task is stopped");
                return;
            }

            /** move on to next **/
            int finishedNum = task.getFinishedItems();
            int totalNum = task.getTotalItems();
            if (totalNum > finishedNum) {
                // continue anyway
                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state != ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").executeSingle();
                if (image != null) {
                    upload(image);
                }
            } else {
                Log.i(TAG, "task finished");
            }
        }
    };
}
