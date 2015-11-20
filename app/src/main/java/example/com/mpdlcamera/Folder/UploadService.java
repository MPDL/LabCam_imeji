package example.com.mpdlcamera.Folder;

/**
 * Created by kiran on 28.09.15.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.MetaData;
import example.com.mpdlcamera.Model.User;
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

//import android.support.annotation.Nullable;

/**
 * Created by kiran on 28.09.15.
 */
public class UploadService extends IntentService {

    Context mContext = this;
    private static final String TAG = "UploadService";
    public UploadService() {
        super("UploadService");
    }
    private SharedPreferences mPrefs;
    private SharedPreferences nPrefs;
    FileId fileId;


    private DataItem item = new DataItem();
    private MetaData meta = new MetaData();

    private String username;
    private String password;
    private User user = new User();
    private String collectionID;
    public TypedFile typedFile;
    String json;



    /*
    Invoked independently by the activity.
    When all the requests are handled, it kills itself.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        if(isNetworkAvailable()) {
            Log.d(TAG, "Service Started!");

            user.setCompleteName("Kiran");
            user.save();
            ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            String networkStatus = networkInfo.getTypeName();

            mPrefs = this.getSharedPreferences("myPref", 0);
            username = mPrefs.getString("username", "");
            password = mPrefs.getString("password", "");
            if(mPrefs.contains("collectionID")) {
                collectionID = mPrefs.getString("collectionID","");
            }
            else
               collectionID = DeviceStatus.collectionID;

            Uri uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DISPLAY_NAME};
            Cursor cursor = this.getContentResolver().query(uri, projection, null,
                    null, null);

            int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);


            int column_index_folder_name = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            int column_index_file_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            final ResultReceiver receiver = intent.getParcelableExtra("receiver");

            Bundle bundle = new Bundle();

                try {

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    SharedPreferences preferencesFiles = getSharedPreferences("gallery", Context.MODE_PRIVATE);
                    SharedPreferences preferencesFolders = getSharedPreferences("folder", Context.MODE_PRIVATE);
                    HashMap<String, String> folderSyncMap = new HashMap<String, String>();
                    folderSyncMap = (HashMap) preferencesFolders.getAll();
                    String status = preferences.getString("status", "");


                    if (status.equalsIgnoreCase("both") || (status.equalsIgnoreCase("Wifi") && (networkStatus.equalsIgnoreCase("wifi")))) {

                        for (Map.Entry<String, String> entry : folderSyncMap.entrySet()) {

                          //  if(String.valueOf())entry.getValue().toString().

                            if (String.valueOf(entry.getValue()).equalsIgnoreCase("On")) {

                                String folderName = (String) entry.getKey();

                                String directoryPath = preferencesFiles.getString(folderName,"");

                                File folder = new File(directoryPath);
                                File[] folderFiles = folder.listFiles();

                                for(File imageFile : folderFiles) {

                                    String imageName = imageFile.getName().toString();
                                    nPrefs = getSharedPreferences("myPref", 0);
                                    String collectionId = mPrefs.getString("collectionID","");
                                    String imageCollectionName = imageName + collectionId;
                                    MySQLiteHelper database = new MySQLiteHelper(mContext);

                                    Boolean fileFlag = database.getFile(imageCollectionName);

                                    if(!fileFlag)

                                    {
                                        item.setFilename(imageName);

                                        meta.setTags(null);

                                        meta.setAddress("blabla");

                                        meta.setTitle(imageName);

                                        meta.setCreator(user.getCompleteName());

                                        item.setCollectionId(collectionID);

                                        item.setLocalPath(imageFile.toString());

                                        item.setMetadata(meta);

                                        item.setCreatedBy(user);

                                        meta.save();
                                        item.save();

                                        upload(item);
                                    }


                                }



    /*                            while (cursor.moveToNext()) {

                                    if (folderName.equalsIgnoreCase(cursor.getString(column_index_folder_name))) {


                                        String fileName = cursor.getString(column_index_file_name);
                                        String path = cursor.getString(column_index_data);

                                        MySQLiteHelper db = new MySQLiteHelper(mContext);

                                        Boolean b = db.getFile(fileName);

                                        if(!b)

                                        {
                                            item.setFilename(fileName);

                                            meta.setTags(null);

                                            meta.setAddress("blabla");

                                            meta.setTitle(fileName);

                                            meta.setCreator(user.getCompleteName());

                                            item.setCollectionId(collectionID);

                                            item.setLocalPath(path);

                                            item.setMetadata(meta);

                                            item.setCreatedBy(user);

                                            meta.save();
                                            item.save();

                                            upload(item);
                                        }
                                    }
                                }*/
                            }
                        }
                    }

                }catch (Exception e) {

                    /* Sending error message back to activity */
                    bundle.putString(Intent.EXTRA_TEXT, e.toString());
                    receiver.send(0, bundle);
                }

                receiver.send(1, Bundle.EMPTY);



            Log.d(TAG, "Service Stopping!");
            this.stopSelf(); //self destructing service.
        }else {
            //Toast.makeText(mContext, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    /*
    uploads the dataitem
     */
    private void upload(DataItem item) {
        nPrefs = this.getSharedPreferences("myPref", 0);
        if(mPrefs.contains("collectionID")) {
            collectionID = mPrefs.getString("collectionID","");
        }
        else
            collectionID = DeviceStatus.collectionID;

        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        item.getMetadata().setDeviceID("1");
        typedFile = new TypedFile("multipart/form-data", new File(item.getLocalPath()));

        json = "{" + jsonPart1 + "}";
        Log.v(TAG, json);

        if(isNetworkAvailable()) {
            RetrofitClient.uploadItem(typedFile, json, callback, username, password);
        }
        else {
            //Toast.makeText(mContext, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
        }
    }


    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            nPrefs = getSharedPreferences("myPref", 0);
            String collectionId = mPrefs.getString("collectionID","");
            MySQLiteHelper db = new MySQLiteHelper(mContext);
           //SQLiteDatabase dBase = mContext.get

            String fileNamePlusId = dataItem.getFilename() + collectionID;
            FileId fileId = new FileId(fileNamePlusId,"yes");

            db.insertFile(fileId);

            Toast.makeText(mContext, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

            mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            if(mPrefs.contains("RemovePhotosAfterUpload")) {

                if(mPrefs.getBoolean("RemovePhotosAfterUpload",true)) {

                    File file = typedFile.file();
                    Boolean deleted = file.delete();
                    Log.v(TAG, "deleted:" +deleted);
                    Toast.makeText(mContext, "Uploaded and deleted", Toast.LENGTH_SHORT).show();

                }

            }

        }

        @Override
        public void failure(RetrofitError error) {

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                if(error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    //Toast.makeText(mContext, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
                }
                else {
                  //  Toast.makeText(mContext, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {

                    MySQLiteHelper db = new MySQLiteHelper(mContext);
                    String fileName = typedFile.fileName();
                    FileId fileId = new FileId(fileName,"yes");
                    db.insertFile(fileId);





                    // Toast.makeText(mContext.getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
               // else
                    //Toast.makeText(mContext, "Upload failed", Toast.LENGTH_SHORT).show();

            }

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
}
