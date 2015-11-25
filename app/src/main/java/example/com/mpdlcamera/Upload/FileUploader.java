package example.com.mpdlcamera.Upload;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
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
 * Created by kiran on 29.09.15.
 */
public class FileUploader {

    private String collectionID;
    public TypedFile typedFile;
    String json;
    private SharedPreferences mPrefs;
    private SharedPreferences nPrefs;


    private Context context=null;

    private Activity act;

    private static final String TAG = "FileUploader";
    private String username;
    private String password;

    public FileUploader(Context context,Activity act) {
        this.context = context;
        this.act = act;
    }
    public FileUploader() {

    }



    /*
        method which uploads the dataitem
     */
    public void upload(DataItem item) {

        mPrefs = context.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");
        if (mPrefs.contains("collectionID")) {
            collectionID = mPrefs.getString("collectionID", ""); // get the CollectionID from the preferences and use the appropriate ColletionID
        } else
            collectionID = DeviceStatus.collectionID;

        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
//        item.getMetadata().setDeviceID("1");
        typedFile = new TypedFile("multipart/form-data", new File(item.getLocalPath()));

        json = "{" + jsonPart1 + "}";
        Log.v(TAG, json);

        if(isNetworkAvailable()) {
            RetrofitClient.uploadItem(typedFile, json, callback, username, password);
        }
        else {
            //Toast.makeText(context, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
        }

    }


    /*
        callback method for upload
     */
    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            nPrefs = context.getSharedPreferences("myPref", 0);
            String collectionId = mPrefs.getString("collectionID","");
            MySQLiteHelper db = new MySQLiteHelper(context);

            String fileNamePlusId = dataItem.getFilename() + collectionID;

            if(!(db.getFileStatus(fileNamePlusId).equalsIgnoreCase("not present"))) {

                db.updateFileStatus(fileNamePlusId,"uploaded");
                //  FileId fileId = new FileId(fileNamePlusId, "uploaded");

            }
            else {
                FileId fileId = new FileId(fileNamePlusId, "uploaded");
                db.insertFile(fileId);

            }
          //  FileId fileId = new FileId(fileNamePlusId,"uploaded");

          //  db.insertFile(fileId);

            List<FileId> fileIds;
            fileIds = db.getAllFiles();

            Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

            mPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            //Delete the photos if the settings "Remove the Photos After upload" is on
            if(mPrefs.contains("RemovePhotosAfterUpload")) {

                if(mPrefs.getBoolean("RemovePhotosAfterUpload",true)) {

                    File file = typedFile.file();
                    Boolean deleted = file.delete();
                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    scanIntent.setData(Uri.fromFile(file));
                    context.sendBroadcast(scanIntent);
                    Log.v(TAG, "deleted:" +deleted);
                    Toast.makeText(context, "Uploaded and deleted", Toast.LENGTH_SHORT).show();
                }

            }

            //Logout from the app if the settings "Logout after upload" is on
            if(mPrefs.contains("LogoutAfterUpload")) {

                if(mPrefs.getBoolean("LogoutAfterUpload", true)) {

                    Intent intent = new Intent(act, PopupActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    act.startActivity(intent);

                    nPrefs = act.getSharedPreferences("logout", Context.MODE_PRIVATE);

                    if(nPrefs.getString("option","").equalsIgnoreCase("yes")) {
                        act.finish();
                    }

                }
            }


        }

        @Override
        public void failure(RetrofitError error) {


            MySQLiteHelper db = new MySQLiteHelper(context);
            String fileName = typedFile.fileName();
            String fileCollectionName = fileName + collectionID;

            db.updateFileStatus(fileCollectionName,"failed");

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                if(error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    //Toast.makeText(context, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
                    //
                }
                else {
                  //  Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {


                    if(!(db.getFileStatus(fileCollectionName).equalsIgnoreCase("not present"))) {

                        db.updateFileStatus(fileCollectionName,"uploaded");
                        //  FileId fileId = new FileId(fileNamePlusId, "uploaded");

                    }
                    else {
                        FileId fileId = new FileId(fileCollectionName, "uploaded");
                        db.insertFile(fileId);

                    }
                }
              //  else
                //    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();

            }

            Log.v(TAG, String.valueOf(error));

        }
    };

    /*
        method which returns the value whether the network is available or not
     */
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
