package example.com.mpdlcamera.AutoRun;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Model.MetaData;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;
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
    private SharedPreferences mPrefs;

    // get last Task, may need to add a param to set task
    Task mTask;
    int next = 0;
    int finishedNum;

    FileId fileId;

    //Local database objects
    private List<Image> images;

    private Long imageId;
    private DataItem item = new DataItem();
    private MetaData meta = new MetaData();

    private String username;
    private String apiKey;
    private String collectionID;
    public TypedFile typedFile;
    String json;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "TaskUploadService Destroy!");
    }

    @Override
    public void onCreate() {
        super.onCreate();

            if(isNetworkAvailable()) {
                Log.v(TAG, "TaskUploadService Started!");
                Toast.makeText(mContext, "TaskUploadService Started", Toast.LENGTH_SHORT).show();
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                String networkStatus = networkInfo.getTypeName();

                mPrefs = this.getSharedPreferences("myPref", 0);
                username = mPrefs.getString("username", "");
                apiKey = mPrefs.getString("apiKey", "");

                //set collectionID
                try{
                    mTask = DeviceStatus.getTask();
                    Log.v("~~~","mTask get");
                    finishedNum = mTask.getFinishedItems();
                    Log.v("~~~","onCreate getFinishedItems: "+finishedNum);
                }
                catch (Exception e){
                    // no task or exception in query
                    //TODO: handle exception
                    Log.v(TAG,"no task or exception in query");
                }
                if(mTask!=null) {
                    collectionID = mTask.getCollectionId();
                    Log.v("~~~","collectionID set");
                }else{
                    Log.v(TAG,"mTask is null, can't get collectionID");
                }

                //Images
                try{
                    images = DeviceStatus.getImagesByTaskId(DeviceStatus.getTask().getTaskId());
                    Log.v("~~~","images get");}

                catch (Exception e){
                    //TODO: handle exception
                    Log.v(TAG,"no image in this task");
                }

                if(images!=null){
                    //get next Image in task
//                    Image image = images.get(next);


                    // image state is WAITING
                    for (Image im: images) {
                        if(im.getState().equals(String.valueOf(DeviceStatus.state.WAITING))){
                            //upload
                            upload(im);
                            Log.v("~~~", " --first available--" + finishedNum);
                            //set finished items num in local database
                            Task task = Task.load(Task.class, mTask.getId());
                            task.setFinishedItems(finishedNum);
                            task.save();

                            break;
                        }else if(im.getState().equals(String.valueOf(DeviceStatus.state.FINISHED))){
                            // next image exist in task (next = 0, size = 1)
                            Log.v("~~~","current image FINISHED, move to next image");

                            if ((finishedNum) < mTask.getTotalItems()) {
                                finishedNum = finishedNum + 1;
                                Log.v("~~~","go to next: "+next);
                            }
                        }else{
                            Log.v("~~~","state error "+next);
                        }

                    }

                }

            }else {
                //Toast.makeText(mContext, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
            }
    }

    /*
        uploads the dataitem
         */
    private void upload(Image image) {

        //get collectionId
        collectionID = mTask.getCollectionId();
        //update current imageId
        imageId = image.getId();
        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        // item.getMetadata().setDeviceID("1");

        typedFile = new TypedFile("multipart/form-data", new File(image.getImagePath()));

        json = "{" + jsonPart1 + "}";
        Log.v(TAG, json);

        if(isNetworkAvailable()) {
            //TODO: set serverURL without login
            String BASE_URL = "https://dev-faces.mpdl.mpg.de/rest/";
            RetrofitClient.setRestServer(BASE_URL);

            RetrofitClient.uploadItem(typedFile, json, callback, apiKey);
        }
        else {
            //Toast.makeText(mContext, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
        }
    }


    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Log.v("~~~","callback success No."+ finishedNum);
            Toast.makeText(mContext, "Uploaded Successfully", Toast.LENGTH_SHORT).show();

            //update mTask
            mTask = DeviceStatus.getTask();
            // change state
            updateImageState();
            Task task = Task.load(Task.class, mTask.getId());
            task.setFinishedItems(task.getFinishedItems() + 1);
            finishedNum = task.getFinishedItems()+1;
            Log.i("~~~","get finishedNum"+ finishedNum+"="+"task.getFinishedItems()"+task.getFinishedItems()+"+1");
            task.save();


            //get Images again in case new image come in
            try{
                images = DeviceStatus.getImagesByTaskId(DeviceStatus.getTask().getTaskId());}
            catch (Exception e){
                //TODO: handle exception
                Log.v(TAG,"no image in this task");
            }

            // images not null, continue
            if(images!=null) {
                // next image exist in task (next = 0, size = 1)

                if ((finishedNum) < mTask.getTotalItems()) {
                    //get next Image in task
                    Image nextImage;
                    nextImage = images.get(next);

                    // image state is WAITING
                    if (nextImage.getState().equals(String.valueOf(DeviceStatus.state.WAITING))) {
                        //upload
                        upload(nextImage);
                    }
                }else{
                    // all uploaded
                    next = next - 1;

                    Log.i("~~~","all uploaded"+finishedNum+"/"+mTask.getTotalItems());
                    //TODO: delete task?

                }

                //TODO: discuss rewrite or keep this function
                mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

                if (mPrefs.contains("RemovePhotosAfterUpload")) {

                    if (mPrefs.getBoolean("RemovePhotosAfterUpload", true)) {

                        File file = typedFile.file();
                        Boolean deleted = file.delete();
                        Log.v(TAG, "deleted:" + deleted);
                        Toast.makeText(mContext, "Uploaded and deleted", Toast.LENGTH_SHORT).show();

                    }

                }
            }
        }

        @Override
        public void failure(RetrofitError error) {

            System.out.println("~uploading image error");
            Log.i("error", error.getMessage());


            if(error.getMessage().equals("422 Unprocessable Entity")){
                Log.i("error","catch 422 already uploaded");
                // change it to finished? or
                updateImageState();

                //update mTask
                mTask = DeviceStatus.getTask();

                next = next +1;
                if ((next) < mTask.getTotalItems()) {
                    //get next Image in task
                    Image nextImage;
                    nextImage = images.get(next);

                    // image state is WAITING
                    if (nextImage.getState().equals(String.valueOf(DeviceStatus.state.WAITING))) {
                        //upload
                        upload(nextImage);
                    }
                }else{
                    // all uploaded
                    next = next - 1;
                    Log.i("~~~","all uploaded");
                    //TODO: delete task?

                }
            }
            //Images
            try{
                images = DeviceStatus.getImagesByTaskId(DeviceStatus.getTask().getTaskId());}
            catch (Exception e){
                //TODO: handle exception
                Log.v(TAG,"no image in this task");
            }

            if(images!=null){
                //get next Image in task
                Image image = images.get(next);

                // image state is WAITING
                if(image.getState().equals(String.valueOf(DeviceStatus.state.WAITING))){
                    //upload
                    upload(image);
                }
            }

            //TODO: write error massage to log


            Log.v(TAG, String.valueOf(error));

        }
    };

    /*
        checks whether the network is available or not
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //update Image in db (not static)
    private void updateImageState(){
        Image image = Image.load(Image.class, imageId);
        image.setState(String.valueOf(DeviceStatus.state.FINISHED));
        image.save();
    }
}
