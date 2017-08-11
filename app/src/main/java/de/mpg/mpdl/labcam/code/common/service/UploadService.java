package de.mpg.mpdl.labcam.code.common.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.NotificationID;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.activity.MainActivity;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.data.net.MultipartUtil;
import de.mpg.mpdl.labcam.code.data.repository.UploadRepository;
import de.mpg.mpdl.labcam.code.injection.component.DaggerUploadComponent;
import de.mpg.mpdl.labcam.code.injection.module.UploadModule;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;
import de.mpg.mpdl.labcam.code.utils.ToastUtils;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;

/**
 * Created by yingli on 7/26/16.
 */

public class UploadService {

    private static final String TAG = UploadService.class.getSimpleName();

    @Inject
    UploadRepository uploadRepository;
    String currentImagePath;
    Task task;

    // SharedPreferences
    private String apiKey;
    private String userId;
    private String serverName;

    private MultipartBody.Part imageFile;
    private String json;

    private Context context;
    private String currentTaskId;
    private String collectionID;

    //compare labCam template profile
    Boolean[] checkTypeList = {false,false,false,false,false,false,false,false,false,false,false,false};

    boolean ocrIsOn = false;

    public UploadService(Context context, Long currentTaskId) {
        this.context = context;
        this.currentTaskId = currentTaskId.toString();
        DaggerUploadComponent.builder()
                .uploadModule(new UploadModule())
                .build()
                .inject(this);
    }

    public void run() {
        //Code
        Log.i(TAG,"Thread --> startUpload()");
        //prepare apiKey
        apiKey = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        userId = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(context, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");
        ocrIsOn = PreferenceUtil.getBoolean(context, Constants.SHARED_PREFERENCES, Constants.OCR_IS_ON, false);

        if(!taskIsStopped()) {
            Log.v(TAG,"not stopped");
            Log.v(TAG, task.getState());

            task = DBConnector.getTaskById(currentTaskId, userId, serverName);

            // task already failed
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                return;
            }

            //prepare collectionId
            collectionID =task.getCollectionId();

            /** start uploading **/
            startUpload();
        }
    }

    private void startUpload() {
        for (int i = 0;i <checkTypeList.length; i++) {
            Log.e(TAG, checkTypeList[i]+"" +
                    "");
        }
        if(!taskIsStopped()){

            // waitingImages is a list join waiting (and failed)
            task = DBConnector.getTaskById(currentTaskId, userId, serverName);    // get task (task state might have already changed)
            Log.d("startUpload", currentTaskId);
            Log.d("startUpload", task.getServerName());
            Log.d("startUpload", task.getUserId());
            List<String> waitingImgPathList = task.getImagePaths();
            Log.d("startUpload", waitingImgPathList.size()+"");
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                return;
            }

            task.setFinishedItems(task.getTotalItems()-task.getImagePaths().size());
            task.save();

            if (waitingImgPathList != null && waitingImgPathList.size() > 0) {             // whether task is finished or still have img for uploading
                currentImagePath = waitingImgPathList.get(0);                                // important: set currentImageId
                upload(waitingImgPathList.get(0));
            }
        }
    }

    private void upload(String imgPath){
        if(taskIsStopped()){
            return;
        }

        File f = new File(imgPath);
        if(f.exists() && !f.isDirectory()) {
            // do something
            Log.i(TAG,collectionID+": file exist");
            json = DeviceStatus.metaDataJson(collectionID, imgPath, checkTypeList, ocrIsOn, context, userId, serverName);
        }else {
            Log.i(TAG, "file not exist: " + imgPath);

            // continue uploading
            uploadNext();

            return;
        }

        imageFile = MultipartUtil.prepareFilePart("file",imgPath);
        Log.v(TAG, "start uploading: " + imgPath);
        HashMap<String, RequestBody> map = new HashMap<>();
        //upload item with new API
        Call<okhttp3.ResponseBody> call = uploadRepository.uploadItem(imageFile, MultipartUtil.createPartFromString(json));
        call.enqueue(new retrofit2.Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, retrofit2.Response<okhttp3.ResponseBody> response) {
                if(response.isSuccessful()) {
                    task = DBConnector.getTaskById(currentTaskId, userId, serverName);

                    if (task == null) {
                        return;  // task is deleted before the success callback
                    }
                    List<String> imgPaths = task.getImagePaths();
                    if (imgPaths.contains(currentImagePath)) {
                        imgPaths.remove(currentImagePath);
                    }
                    task.setImagePaths(imgPaths);
                    Log.d(TAG, "setImagePaths+ callback success");
                    task.setFinishedItems(task.getTotalItems() - task.getImagePaths().size());
                    task.save();

                    if (task.getTotalItems() > task.getFinishedItems()) {
                        if (task.getImagePaths() != null && task.getImagePaths().size() > 0) {  // move on to next
                            currentImagePath = task.getImagePaths().get(0);

                            if (task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))) {  // check task state
                                Log.e(TAG, "task, stopped");
                                return;
                            }

                            upload(currentImagePath);
                        }
                    } else {

                        if (task.getUploadMode().equalsIgnoreCase("AU")) {

                            task.setUploadMode("AU_FINISHED");
                            task.setEndDate(DeviceStatus.dateNow());
                            task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                            task.save();

                            Intent uploadIntent = new Intent(context, TaskUploadService.class);
                            context.stopService(uploadIntent);  //stop AU service

                            createAUTask(task); //create AU task

                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    notification();
                                }
                            });
                        } else { // MU task
                            task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                            task.setEndDate(DeviceStatus.dateNow());
                            task.save();
                            Handler handler = new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    notification();
                                }
                            });
                        }
                    }
                }else {
                    Log.i("POST ITEM", "failed");
                    if(currentImagePath==null){
                        Log.v(TAG, "currentImage:" + currentImagePath + "is null");
                        return;
                    }
                    // error kinds: "network", 403, 422, 404,  other
                    if (response == null) {
                        if (String.valueOf(response.errorBody()).contains("NETWORK")) {
                            Log.d(TAG,"network error");
                        }
                    } else {
                        int statusCode = response.code();
                        switch (statusCode){
                            case 403:
                                // set TASK state failed, print log
                                task.setState(String.valueOf(DeviceStatus.state.FAILED));
                                task.setEndDate(DeviceStatus.dateNow());
                                task.save();
                                Log.e(TAG, collectionID + "forbidden");
                                Handler handler_403=new Handler(Looper.getMainLooper());
                                handler_403.post(new Runnable() {
                                    public void run() {
                                        Toast.makeText(context, "Unauthorized to upload", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return;
                            case 422:
                                if (String.valueOf(response.errorBody()).contains("already exists")) {
                                    // remove from task's imgPaths
                                    task = DBConnector.getTaskById(currentTaskId, userId, serverName);
                                    List<String> imgPaths = task.getImagePaths();
                                    if(imgPaths.contains(currentImagePath)){
                                        imgPaths.remove(currentImagePath);
                                    }
                                    task.setImagePaths(imgPaths);
                                    Log.d(TAG, "setImagePaths+callback+422");
                                    task.setFinishedItems(task.getTotalItems()- task.getImagePaths().size());
                                    task.save();

                                }else if(String.valueOf(response.errorBody()).contains("Invalid collection")){
                                    task.setState(String.valueOf(DeviceStatus.state.FAILED));
                                    task.setEndDate(DeviceStatus.dateNow());
                                    task.save();
                                    Handler handler_422=new Handler(Looper.getMainLooper());
                                    handler_422.post(new Runnable() {
                                        public void run() {
                                            Toast.makeText(context, "Invalid collection", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                break;
                            case 404:
                                if (String.valueOf(response.errorBody()).contains("Not Found")) {
                                    // set currentImage state finished, print log
                                    Log.d("error_404", "Not Found");

                                    task.setState(String.valueOf(DeviceStatus.state.FAILED));
                                    task.setEndDate(DeviceStatus.dateNow());
                                    task.save();
                                    Log.d(TAG, collectionID + "collection not found");
                                    Handler  handler=new Handler(Looper.getMainLooper());
                                    handler.post(new Runnable() {
                                        public void run() {
                                            Toast.makeText(context, "collection not found", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    return;
                                }
                                break;
                            default:
                                // 400 401 500 ...
                                // remove from task's imgPaths
                                task = DBConnector.getTaskById(currentTaskId, userId, serverName);
                                List<String> imgPaths = task.getImagePaths();
                                if(imgPaths.contains(currentImagePath)){
                                    imgPaths.remove(currentImagePath);
                                }
                                task.setImagePaths(imgPaths);
                                ToastUtils.showShortMessage(context, "error: "+statusCode);
                                task.setFinishedItems(task.getTotalItems()- task.getImagePaths().size());
                                task.save();

                        }
                    }
                    // continue upload
                    uploadNext();
                }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable e) {
            }
        });
    }

    private boolean taskIsStopped (){
        task = DBConnector.getTaskById(currentTaskId, userId, serverName);

        if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))){
            Log.v(TAG,"taskIsStopped");
            return true;
        }else{
            Log.v(TAG,"task is not stopped");
            return false;
        }
    }

    private void uploadNext(){

        task = DBConnector.getTaskById(currentTaskId, userId, serverName);

        Log.i(TAG, "get task");
        if(task==null){
            return;
        }

        if (taskIsStopped()) {
            Log.e(TAG,"task is stopped");
            return;
        }else if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
            Log.e(TAG,"task is failed");
            return;
        }

        if(task.getTotalItems()>task.getFinishedItems()){            // move on to next
            // continue anyway
            if(task.getImagePaths()!=null && task.getImagePaths().size()>0){
                currentImagePath = task.getImagePaths().get(0);
                upload(currentImagePath);
            }
        }else {
            if(task.getUploadMode().equalsIgnoreCase("AU")){
                task.setUploadMode("AU_FINISHED");
                task.setEndDate(DeviceStatus.dateNow());
                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.save();

                Intent uploadIntent = new Intent(context, TaskUploadService.class);
                context.stopService(uploadIntent);  //stop AU service

                createAUTask(task);  // create new AU task

            }else {
                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.setEndDate(DeviceStatus.dateNow());
                task.save();
                notification();
            }
        }
    }


    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.glass_notification : R.drawable.notification;
    }

    private void notification(){

        //default ID for the notification (auto)
        int mNotificationId = 001;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(getNotificationIcon());
        builder.setContentTitle("LabCam");

        if(("AU_FINISHED").equalsIgnoreCase(task.getUploadMode())){
            String taskInfo = task.getTotalItems()+ " photo(s) automatically uploaded to "+ task.getCollectionName();
            builder.setContentText(taskInfo);
        }else if(("MU").equalsIgnoreCase(task.getUploadMode())){
            // Sets an ID for the notification
            mNotificationId = NotificationID.getID();
            String taskInfo = task.getTotalItems()+ " photo(s) uploaded successfully";
            builder.setContentText(taskInfo);
        }

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, builder.build());
    }

    /** create new autoTask when old task finished **/

    private void createAUTask(Task oldTask){
        Task newAUTask = new Task();
        newAUTask.setUploadMode("AU");
        newAUTask.setCollectionId(oldTask.getCollectionId());
        newAUTask.setCollectionName(oldTask.getCollectionName());
        newAUTask.setState(String.valueOf(DeviceStatus.state.WAITING));
        newAUTask.setUserName(oldTask.getUserName());
        newAUTask.setUserId(oldTask.getUserId());
        newAUTask.setTotalItems(0);
        newAUTask.setFinishedItems(0);
        newAUTask.setServerName(oldTask.getServerName());

        Long now = new Date().getTime();
        newAUTask.setStartDate(String.valueOf(now));

        newAUTask.save();

    }
}
