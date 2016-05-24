package example.com.mpdlcamera.AutoRun;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Delete;
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
 * Created by yingli on 3/2/16.
 */
public class ManualUploadThread extends Thread {


    private static final String TAG = ManualUploadThread.class.getSimpleName();
    List<Image> waitingImages = null;
    List<Image> finishedImages = null;

    //  position in waitingImage list

    String currentImageId;
    Task task;

    // SharedPreferences
    private SharedPreferences mPrefs;
    private String apiKey;

    private TypedFile typedFile;
    private String json;

    // handler for toast
    private Handler handler = new Handler();

    private Context context;
    private String currentTaskId;
    private String collectionID;



    public ManualUploadThread(Context context,String currentTaskId) {
        super("ManualUploadThread");
        this.context = context;
        this.currentTaskId = currentTaskId;
    }

    public void run() {
        //Code
        Log.i(TAG,"Thread --> startUpload()");

        //prepare apiKey
        mPrefs = context.getSharedPreferences("myPref", 0);
        apiKey = mPrefs.getString("apiKey", "");

        if(!taskIsStopped()) {
            Log.v(TAG,"not stopped");
            Log.v(TAG, task.getState());

            task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

            // set task failed
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                return;
            }

            //prepare collectionId
            collectionID =task.getCollectionId();

            startUpload();

        }

    }

    private void startUpload() {

        if(!taskIsStopped()){

            /** WAITING, FINISHED **/
            waitingImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").execute();
            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();

            //DELETE TESTING
            Log.i(TAG, "waitingImages: " + waitingImages.size());
            Log.i(TAG, "finishedImages: " + finishedImages.size());
            Log.i(TAG, "totalImages: " + task.getTotalItems());


            if (waitingImages!=null && waitingImages.size() > 0) {

                // look into database, get first image of a task in create time desc order
                // upload begin with the first in list
                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?",String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").executeSingle();
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


                if (imageState.equalsIgnoreCase(String.valueOf(DeviceStatus.state.WAITING))) {
                    // TODO:upload image
                    String jsonPart1 = "\"collectionId\" : \"" +
                            collectionID +
                            "\"";
                    File f = new File(filePath);
                    if(f.exists() && !f.isDirectory()) {
                        // do something
                        Log.i(TAG,collectionID+": file exist");
                    }else {
                        Log.i(TAG, "file not exist: " + currentImageId);
                        // delete Image from task
                        new Delete()
                                .from(Image.class)
                                .where("imageId = ?", currentImageId)
                                .execute();
                        // continue unpload
                        uploadNext();

                        return;
                    }

                    typedFile = new TypedFile("multipart/form-data", f);
                    json = "{" + jsonPart1 + "}";
                    Log.e(TAG, "start uploading~ " + filePath);
                    Log.e(TAG, "TO remote :"+collectionID + "from local:"+ filePath);
                    Log.e(TAG, "==> step 3");

                    RetrofitClient.uploadItem(typedFile, json, callback, apiKey);

                }else {
                    Log.e(TAG,"illegal imageState:"+imageState);
                }
            }
        }
    }

    private void upload(Image image){

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


                File f = new File(filePath);
                if(f.exists() && !f.isDirectory()) {
                    // do something
                    Log.i(TAG,collectionID+": file exist");
                }else {
                    Log.i(TAG, "file not exist: " + currentImageId);
                    // delete Image from task
                    new Delete()
                            .from(Image.class)
                            .where("imageId = ?", currentImageId)
                            .execute();
                    // continue unpload
                    uploadNext();

                    return;
                }

                typedFile = new TypedFile("multipart/form-data", f);
                json = "{" + jsonPart1 + "}";
                Log.v(TAG, "start uploading: " + filePath);
                RetrofitClient.uploadItem(typedFile, json, callback, apiKey);
            }
        }
    }


    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            if(!taskIsStopped()){

                Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

                Log.e(TAG, currentImageId);
                Image currentImage = new Select().from(Image.class).where("imageId = ?",currentImageId).executeSingle();
                currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
                currentImage.save();

                //TODO: ReWrite "remove after upload" function later

            /*
                Delete the file if the setting "Remove the photos after upload" is On
             */
                if(mPrefs.contains("RemovePhotosAfterUpload")) {
                    if(mPrefs.getBoolean("RemovePhotosAfterUpload",true)) {

                        File file = typedFile.file();
                        Boolean deleted = file.delete();
                        Log.v(TAG, "deleted:" +deleted);
                    }
                }
                //TODO: remove picture
//            adapter.notifyDataSetChanged();

                try {
                    /** WAITING, FINISHED **/
                    finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
                } catch (Exception e) {
                }

                //DELETE TESTING
                Log.i(TAG, "finishedImages " + finishedImages.size());

                task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

                if(finishedImages==null||task==null) {
                    return;
                }
                task.setFinishedItems(finishedImages.size());
                task.save();

                /** move on to next **/
                int finishedNum = task.getFinishedItems();
                int totalNum = task.getTotalItems();
                if(totalNum>finishedNum){
                    Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?",String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").executeSingle();
                    if(image!=null){
                        upload(image);
                    }
                }else {
                    task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                    task.save();
                    handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Log.i(TAG,"task finished");
                }
            }
        }

        @Override
        public void failure(final RetrofitError error) {

            // set Task "INTERRUPTED" while 403||422
//            if(error.getResponse().getStatus() == 403 || error.getResponse().getStatus() == 422 ) {
//                task.setState(String.valueOf(DeviceStatus.state.INTERRUPTED));
//                task.save();
//                Log.e(TAG, "task: " + task.getTaskId());
//                Log.e(TAG, task.getState());
//                Log.e(TAG, error.getResponse().getStatus()+"");
//                return;
//            }

            if (!taskIsStopped()) {
                final Image currentImage = new Select().from(Image.class).where("imageId = ?",currentImageId).executeSingle();

                if(currentImage!=null){
                    //do sth
                }else {
                    Log.v(TAG,"currentImage:"+ currentImageId+ "is null");
                    return;
                }

                //change state not saved here

//                    currentImage.setState(String.valueOf(DeviceStatus.state.INTERRUPTED));
//                    currentImage.setLog(error.getKind().name());
//                    Log.e(TAG, error.getKind().name());

                //



                if (error == null || error.getResponse() == null) {
                    OttoSingleton.getInstance().post(new UploadEvent(null));
                    if(error.getKind().name().equalsIgnoreCase("NETWORK")) {


                    }else {

                        handler=new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Log.e("Upload failed", error.getMessage());
                                Log.e("Upload failed", error.getResponse().getStatus()+"");
                                Log.e("Upload failed",currentImage.getImageName());
                            }
                        });
                    }
                } else {
                    OttoSingleton.getInstance().post(
                            new UploadEvent(error.getResponse().getStatus()));
                    String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                    if (jsonBody.contains("already exists")) {

//                        handler=new Handler(Looper.getMainLooper());
//                        handler.post(new Runnable() {
//                            public void run() {
//                            Toast.makeText(context, "Photo already exists", Toast.LENGTH_SHORT).show();
//                            }
//                        });
                        currentImage.setLog(error.getKind().name() + " already exists");
                        currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
                        currentImage.save();

                    }
                    else {

                        handler=new Handler(Looper.getMainLooper());
                        handler.post(new Runnable() {
                            public void run() {
                                Log.e("Upload failed", error.getMessage());
                                Log.e("Upload failed", error.getResponse().getStatus()+"");
                                Log.e("Upload failed", currentImage.getImageName());
                            }
                        });
                    }
                }
                // save state changes
                currentImage.save();
                Log.v(TAG, String.valueOf(error));

                //TODO: continue upload
                uploadNext();


            }
        }
    };

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

    private void uploadNext(){
        /** exception here**/
        int totalNum = 0;
        if(task!= null){totalNum = task.getTotalItems();}
        try {
            /** WAITING, INTERRUPTED, STARTED, (FINISHED + STOPPED) **/
            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
            List<Image> allImages= new Select().from(Image.class).where("taskId = ?", currentTaskId).execute();
            totalNum = allImages.size();
        } catch (Exception e) {
        }

        task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

        if(finishedImages==null||task==null) {
            return;
        }
        task.setTotalItems(totalNum);
        task.setFinishedItems(finishedImages.size());
        task.save();

        /** move on to next **/
        int finishedNum = task.getFinishedItems();
//        totalNum = task.getTotalItems();
        if(totalNum>finishedNum){
            // continue anyway
            Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?",String.valueOf(DeviceStatus.state.WAITING)).orderBy("RANDOM()").executeSingle();
            if(image!=null){
                upload(image);
            }
        }else {
            Log.i(TAG,"task finished");
        }
    }
}
