package example.com.mpdlcamera.AutoRun;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.squareup.otto.Produce;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.ImejiProfile;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Model.TO.MetadataProfileTO;
import example.com.mpdlcamera.Model.TO.StatementTO;
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

/**
 * Created by yingli on 3/2/16.
 */
public class ManualUploadThread extends Thread {


    private static final String TAG = ManualUploadThread.class.getSimpleName();

    List<Image> waitingImages = null;
    List<Image> finishedImages = null;
    List<Image> failedImages = null;

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

    //profile and meta data
    String getCollectionResponse;
    String profileId;

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

            // task already failed
            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
                return;
            }

            //prepare collectionId
            collectionID =task.getCollectionId();

            /** debug **/
            getCollectionById();

            startUpload();

        }

    }

    private void startUpload() {

        if(!taskIsStopped()){

            /** WAITING, FINISHED **/
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
                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state != ?",String.valueOf(DeviceStatus.state.STARTED)).where("state != ?",String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").executeSingle();
                String imageState = image.getState();
                String filePath = image.getImagePath();
                // important: set currentImageId, so that in the callback can find it
                currentImageId = image.getImageId();

                upload(image);

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

        String jsonPart2 = "";
        File f = new File(filePath);
        if(f.exists() && !f.isDirectory()) {
            // do something
            Log.i(TAG,collectionID+": file exist");
            jsonPart2 = DeviceStatus.metaDataJson(filePath);
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
        json = "{" + jsonPart1 + ", \"metadata\" : "+jsonPart2+"}";
        Log.v(TAG, "start uploading: " + filePath);
        RetrofitClient.uploadItem(typedFile, json, callback_upload, apiKey);
        image.setState(String.valueOf(DeviceStatus.state.STARTED));


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

    private void uploadNext(){

        int totalNum = 0;
        task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

        if(task==null){
            return;
        }

        finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();
        List<Image> allImages= new Select().from(Image.class).where("taskId = ?", currentTaskId).execute();
        totalNum = allImages.size();

        // finishedImages ofc >1
        if(finishedImages==null) {
            return;
        }

        task.setTotalItems(totalNum);
        task.setFinishedItems(finishedImages.size());
        task.setEndDate(DeviceStatus.dateNow());
        task.save();


        if (taskIsStopped()) {
            Log.e(TAG,"task is stopped");
            return;
        }else if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.FAILED))){
            Log.e(TAG,"task is failed");
            return;
        }

        /** move on to next **/
        int finishedNum = task.getFinishedItems();
//        totalNum = task.getTotalItems();
        if(totalNum>finishedNum){
            // continue anyway
            Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state != ?",String.valueOf(DeviceStatus.state.STARTED)).where("state != ?",String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").executeSingle();
            if(image!=null){
                upload(image);
            }
        }else {
            task.setState(String.valueOf(DeviceStatus.state.FINISHED));
            task.setEndDate(DeviceStatus.dateNow());
            task.save();
            notification();
            Log.i(TAG,"task finished");
        }
    }

    private void notification(){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setSmallIcon(R.drawable.notification);
        builder.setContentTitle("LabCam");

        String taskInfo = task.getTotalItems()+ " photo(s) uploaded successfully";
        builder.setContentText(taskInfo);

        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Sets an ID for the notification
        int mNotificationId = NotificationID.getID();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(mNotificationId, builder.build());
    }

    //todo: post item with MDs

    /**
     * retrieve collection by ID
     */
    private void getCollectionById(){
        RetrofitClient.getCollectionById(collectionID,callback_get_collection,apiKey);
    }


    /**
     * callbacks
     */


    Callback<ImejiFolder> callback_get_collection = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            if(imejiFolder.getProfile().getId()==null){
                //store response
                //Try to get response body
                BufferedReader reader = null;
                StringBuilder sb = new StringBuilder();
                try {

                    reader = new BufferedReader(new InputStreamReader(response.getBody().in()));

                    String line;

                    try {
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                getCollectionResponse = sb.toString();
                Log.e(TAG,getCollectionResponse);

                //create new profile
                String collectionTitle = imejiFolder.getTitle();
                String jsonTitlePart = " \"title\" :"+" \""+ collectionTitle+" \"";
                String jsonStatementsPart = "\n" +
                        "      \"statements\": [\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#date\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Creation Date\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Make\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Model\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#number\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"ISO Speed Ratings\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#geolocation\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Geolocation\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Exposure Mode\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Exposure Time\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        }\n" +
                        "      ]\n";

                String jsonPostProfile = "{" + jsonTitlePart + ","+jsonStatementsPart+"}";
                RetrofitClient.createProfile(jsonPostProfile,callback_create_profile,apiKey);

                Log.e(TAG,"create new profile");
            }else {
                //TODO: retrieve profile by ID
                Log.e(TAG,"retrieve profile by ID");
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG,"failure in callback_get_collection");
        }
    };

    // post profile callback
    Callback<MetadataProfileTO> callback_create_profile = new Callback<MetadataProfileTO>() {
        @Override
        public void success(MetadataProfileTO metadataProfileTO, Response response) {
            profileId = metadataProfileTO.getTitle();
            Log.e(TAG,"profileId: "+profileId);

            List<StatementTO> statementTOList = metadataProfileTO.getStatements();
            for (StatementTO statementTO: statementTOList) {
                Log.e(TAG,statementTO.getLabels()+"");
//                statementTO.getLiteralConstraints()
                Log.e(TAG,statementTO.getPos()+"");
                Log.e(TAG,statementTO.getType()+"");
            }
            //update collection
            //build update json
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(getCollectionResponse).getAsJsonObject();
            jsonObject.remove("profile");
            JsonObject jsProfile = new JsonObject();
                jsProfile.addProperty("id",profileId);
                jsProfile.addProperty("method", "copy");
            jsonObject.add("profile",jsProfile);

            Log.e(TAG, "modified json:"+jsonObject.toString());

            RetrofitClient.updateCollection(collectionID,jsonObject, callback_update_collection,apiKey);
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG,"profileId: failed");
            if(error.getResponse()!=null){
                int responseCode = error.getResponse().getStatus();
                Log.e(TAG,responseCode+"(error code)");
//                //Try to get response body
//                BufferedReader reader = null;
//                StringBuilder sb = new StringBuilder();
//                try {
//
//                    reader = new BufferedReader(new InputStreamReader(error.getResponse().getBody().in()));
//
//                    String line;
//
//                    try {
//                        while ((line = reader.readLine()) != null) {
//                            sb.append(line);
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                String errorMassage = sb.toString();
//                Log.e(TAG,"create profile: "+ errorMassage);

                if(responseCode==403){
                    //TODO: post item with MD ...

                }

            }
        }
    };

    Callback<ImejiFolder> callback_update_collection = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.e(TAG,"callback_update_collection success");
            //TODO : post item with MD
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG,"callback_update_collection failed");
        }
    };

    Callback<DataItem> callback_upload = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());
            Log.e(TAG, currentImageId);
            Image currentImage = new Select().from(Image.class).where("imageId = ?",currentImageId).executeSingle();

            if (currentImage == null) {
                Log.v(TAG, currentImageId + "is not in database, task might be resumed");
                // task is deleted/resumed so break and left callback
                return;
            }

            currentImage.setState(String.valueOf(DeviceStatus.state.FINISHED));
            currentImage.save();

            finishedImages = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).orderBy("RANDOM()").execute();

            task = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();

            if(finishedImages==null||task==null) {
                return;
            }
            task.setFinishedItems(finishedImages.size());
            task.save();

            /** move on to next **/

            if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))){
                Log.e(TAG,"MU task, stopped");
                return;
            }

            int finishedNum = task.getFinishedItems();
            int totalNum = task.getTotalItems();
            if(totalNum>finishedNum){
                Image image = new Select().from(Image.class).where("taskId = ?", currentTaskId).where("state != ?",String.valueOf(DeviceStatus.state.FINISHED)).where("state != ?",String.valueOf(DeviceStatus.state.STARTED)).orderBy("RANDOM()").executeSingle();
                if(image!=null){
                    upload(image);
                }
            }else {
                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.setEndDate(DeviceStatus.dateNow());
                task.save();
                handler=new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        notification();
//                        Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
                    }
                });
                Log.i(TAG,"task finished");
            }
        }

        @Override
        public void failure(final RetrofitError error) {

            if (taskIsStopped()) {
                return;
            }
            final Image currentImage = new Select().from(Image.class).where("imageId = ?",currentImageId).executeSingle();

            if(currentImage!=null){
                //do sth
                currentImage.setState(String.valueOf(DeviceStatus.state.FAILED));
            }else {
                Log.v(TAG, "currentImage:" + currentImageId + "is null");
                return;
            }

            // error kinds: "network", 403, 422, other
            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                if (error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    Log.e(TAG,"network error");
                } else {
                    // error == null, but it is not network error
                }
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));

                int statusCode = error.getResponse().getStatus();
                switch (statusCode){
                    case 403:
                        // set TASK state failed, print log
                        try{
                            task.setState(String.valueOf(DeviceStatus.state.FAILED));
                            task.setEndDate(DeviceStatus.dateNow());
                            task.save();
                            Log.e(TAG, collectionID + "forbidden");
                            handler=new Handler(Looper.getMainLooper());
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(context, "Unauthorized to upload", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(context, "remote collectionId not exist, no such folder", Toast.LENGTH_SHORT).show();
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
            uploadNext();
        }
    };

}
