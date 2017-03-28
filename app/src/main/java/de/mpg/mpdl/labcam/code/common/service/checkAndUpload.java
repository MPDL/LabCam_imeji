package de.mpg.mpdl.labcam.code.common.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import de.mpg.mpdl.labcam.code.activity.MainActivity;
import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.NotificationID;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.data.model.TO.MetadataProfileTO;
import de.mpg.mpdl.labcam.code.data.model.TO.StatementTO;
import de.mpg.mpdl.labcam.code.data.net.MultipartUtil;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

/**
 * Created by yingli on 7/26/16.
 */

public class checkAndUpload {

    private static final String TAG = checkAndUpload.class.getSimpleName();

    String currentImagePath;
    Task task;

    // SharedPreferences
    private String apiKey;
    private String userId;
    private String serverName;

    private TypedFile typedFile;
    private String json;


    private Context context;
    private String currentTaskId;
    private String collectionID;

    //profile and meta data
    String getCollectionResponse;
    String profileId;

    List<StatementTO> profileStatementTOList;
    List<StatementTO> collectionProfileStatementTOList;

    //compare labCam template profile
    String[] labCamTemplateProfileLabels = {"Make", "Model", "ISO Speed Ratings","Creation Date", "Geolocation","GPS Version ID", "Sensing Method", "Aperture Value", "Color Space", "Exposure Time", "Note", "OCR"};
    String[] labCamTemplateProfileTypes ={"text", "text", "number", "date", "geolocation", "text", "text", "text", "text", "text", "text", "text"};
    Boolean[] checkTypeList = {false,false,false,false,false,false,false,false,false,false,false,false};
    boolean ocrIsOn = false;

    public checkAndUpload(Context context, Long currentTaskId) {
        this.context = context;
        this.currentTaskId = currentTaskId.toString();
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
            getCollectionById();

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

    /**
     * upload a image (unit function)
     */
    private void upload(String imgPath){
        if(taskIsStopped()){
            return;
        }

        // upload image
        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";

        String jsonPart2 = "";
        File f = new File(imgPath);
        if(f.exists() && !f.isDirectory()) {
            // do something
            Log.i(TAG,collectionID+": file exist");
            jsonPart2 = DeviceStatus.metaDataJson(imgPath, checkTypeList, ocrIsOn, context, userId, serverName);
        }else {
            Log.i(TAG, "file not exist: " + imgPath);

            // continue uploading
            uploadNext();

            return;
        }

        typedFile = new TypedFile("multipart/form-data", f);
        json = "{" + jsonPart1 + ", \"metadata\" : "+jsonPart2+"}";
        Log.v(TAG, "start uploading: " + imgPath);

        MultipartBody.Part body = MultipartUtil.prepareFilePart("img", imgPath);
        HashMap<String, RequestBody> map = new HashMap<>();
        RequestBody jsonBody = MultipartUtil.createPartFromString(json);
        map.put("json", jsonBody);
        RetrofitClient.uploadItem(map, body);
//        RetrofitClient.uploadItem(typedFile, json, callback_upload, apiKey);
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
                        "              \"value\": \"GPS Version ID\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Sensing Method\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Aperture Value\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Color Space\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Exposure Time\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"Note\",\n" +
                        "              \"lang\": \"en\"\n" +
                        "            }\n" +
                        "          ]\n" +
                        "        },\n" +
                        "        {\n" +
                        "          \"type\": \"http://imeji.org/terms/metadata#text\",\n" +
                        "          \"labels\": [\n" +
                        "            {\n" +
                        "              \"value\": \"OCR\",\n" +
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
                String profileId = imejiFolder.getProfile().getId();
                RetrofitClient.getProfileById(profileId, callback_get_profile,apiKey);

            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG,"failure in callback_get_collection");

            if (error == null || error.getResponse() == null) {
                if (error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    Log.d(TAG,"network error");
                } else {
                    // error == null, but it is not network error
                }
            } else if(error.getResponse().getStatus()== 404) {
                task.setState(String.valueOf(DeviceStatus.state.FAILED));
                task.setEndDate(DeviceStatus.dateNow());
                task.save();
                Log.d(TAG, collectionID + "collection not found");
                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(new Runnable() {
                    public void run() {
                        Toast.makeText(context, "collection not found", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
        }
    };

    //get profile by profileDd
    Callback<MetadataProfileTO> callback_get_profile = new Callback<MetadataProfileTO>() {
        @Override
        public void success(MetadataProfileTO metadataProfileTO, Response response) {
            Log.d(TAG,"callback_get_profile: ");

            Log.d(TAG,"collection profileId: " + metadataProfileTO.getTitle());

            collectionProfileStatementTOList = metadataProfileTO.getStatements();

            //TODO: compare with our template
            for( int i = 0; i < labCamTemplateProfileLabels.length; i++)
            {
                // "make"
                for (StatementTO statementTO: collectionProfileStatementTOList) {
                    if(labCamTemplateProfileLabels[i].equalsIgnoreCase(statementTO.getLabels().get(0).getValue())){
                        if(statementTO.getType().contains(labCamTemplateProfileTypes[i])){
                            // type correct
                            checkTypeList[i]  = true;
                        }
                    }
                }
            }
            startUpload();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.d(TAG,"get profile failed");
            if(error.getResponse()!=null){
                int responseCode = error.getResponse().getStatus();
                Log.d(TAG,responseCode+"(error code)");
            }
        }
    };

    // post profile callback
    Callback<MetadataProfileTO> callback_create_profile = new Callback<MetadataProfileTO>() {
        @Override
        public void success(MetadataProfileTO metadataProfileTO, Response response) {
            profileId = metadataProfileTO.getId();
            Log.e(TAG,"new profileId: "+profileId);

            profileStatementTOList = metadataProfileTO.getStatements();

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

                if(responseCode==403){
                    //post item without MD ...
                    startUpload();
                }else if(responseCode==405){
                    //post item without MD ...
                    //method not allowed on spot...
                    startUpload();
                }else {
                    Toast.makeText(context,"failed to create profile",Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    Callback<ImejiFolder> callback_update_collection = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.e(TAG,"callback_update_collection success");
            //TODO : post item with MD
            for(int i=0; i<checkTypeList.length; i++){
                checkTypeList[i]= true;
            }
            startUpload();
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG,"callback_update_collection failed");
            // TODO: 8/3/16 delete profile by id ; post item without MD
            RetrofitClient.deleteProfileById(profileId,callback_delete_profile ,apiKey);
            for(int i=0; i<checkTypeList.length; i++){
                checkTypeList[i]= false;
            }
                startUpload();
        }
    };

    Callback<String> callback_delete_profile = new Callback<String>() {
        @Override
        public void success(String metadataProfileTO, Response response) {
            Log.e(TAG,"callback_delete_profile success");
        }

        @Override
        public void failure(RetrofitError error) {
            Log.e(TAG,"callback_delete_profile failed");
        }
    };


    Callback<DataItem> callback_upload = new Callback<DataItem>() {
        @Override
        public void success(DataItem dataItem, Response response) {

            Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

            task = DBConnector.getTaskById(currentTaskId, userId, serverName);

            if(task==null){
                return;  // task is deleted before the success callback
            }
            List<String> imgPaths = task.getImagePaths();
            if(imgPaths.contains(currentImagePath)){
                imgPaths.remove(currentImagePath);
            }
            task.setImagePaths(imgPaths);
            Log.d(TAG, "setImagePaths+ callback success");
            task.setFinishedItems(task.getTotalItems()- task.getImagePaths().size());
            task.save();

            if(task.getTotalItems() > task.getFinishedItems()){
                if(task.getImagePaths()!=null && task.getImagePaths().size()>0){  // move on to next
                    currentImagePath = task.getImagePaths().get(0);

                    if(task.getState().equalsIgnoreCase(String.valueOf(DeviceStatus.state.STOPPED))){  // check task state
                        Log.e(TAG,"task, stopped");
                        return;
                    }

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

                    createAUTask(task); //create AU task

                    Handler  handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            notification();
                        }
                    });
                }else{ // MU task
                    task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                    task.setEndDate(DeviceStatus.dateNow());
                    task.save();
                    Handler  handler=new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        public void run() {
                            notification();
                        }
                    });
                }
            }
        }

        @Override
        public void failure(final RetrofitError error) {

            if(currentImagePath==null){
                Log.v(TAG, "currentImage:" + currentImagePath + "is null");
                return;
            }

            // error kinds: "network", 403, 422, 404,  other
            if (error == null || error.getResponse() == null) {
                if (error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    Log.d(TAG,"network error");
                }
            } else {
                int statusCode = error.getResponse().getStatus();
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
                        String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        if (jsonBody.contains("already exists")) {

                            //TODO Update Item

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

                        }else if(jsonBody.contains("Invalid collection")){
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
                        String jsonBody_404 = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                        Log.d("error_404", jsonBody_404);
                        if (jsonBody_404.contains("Not Found")) {
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
                        Log.d(TAG, "setImagePaths+callback+400...");
                        task.setFinishedItems(task.getTotalItems()- task.getImagePaths().size());
                        task.save();

                }
            }
            Log.v(TAG, String.valueOf(error));

            // continue upload
            uploadNext();
        }
    };


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
