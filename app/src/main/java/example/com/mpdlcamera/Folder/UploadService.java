package example.com.mpdlcamera.Folder;

/**
 * Created by kiran on 28.09.15.
 */
import android.app.IntentService;
import android.app.Service;
import android.content.ClipData;
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
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.MetaData;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.OttoSingleton;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.UploadEvent;
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



    private DataItem item = new DataItem();
    private MetaData meta = new MetaData();
    private String username;
    private String password;
    private User user = new User();
    private String collectionID = DeviceStatus.collectionID;
    public TypedFile typedFile;
    String json;






    @Override
    protected void onHandleIntent(Intent intent) {
        //DO SOMETHING
        Log.d(TAG, "Service Started!");

        user.setCompleteName("Kiran");
        user.save();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        String networkStatus;
        networkStatus = networkInfo.getTypeName();

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

        if(!TextUtils.isEmpty("qw"))
        {

            try {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                HashMap<String, String> folderSyncMap = new HashMap<String, String>();
                folderSyncMap = (HashMap) preferences.getAll();
                String status = preferences.getString("status", "");

                // Iterator hashIterator = folderSyncMap.keySet().iterator();

                if (status.equalsIgnoreCase("both") || (status.equalsIgnoreCase("Wifi") && (networkStatus.equalsIgnoreCase("wifi"))))

                for (Map.Entry<String, String> entry : folderSyncMap.entrySet()) {


                    if (entry.getValue().toString().equalsIgnoreCase("On")) {

                        String folderName = (String) entry.getKey();

                        while (cursor.moveToNext()) {

                            if (folderName.equalsIgnoreCase(cursor.getString(column_index_folder_name))) {


                                String fileName = cursor.getString(column_index_file_name);
                                String path = cursor.getString(column_index_data);

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


                    }


                }
            }catch (Exception e) {

                /* Sending error message back to activity */
                bundle.putString(Intent.EXTRA_TEXT, e.toString());
                receiver.send(0, bundle);
            }





            receiver.send(0, Bundle.EMPTY);



        }


        Log.d(TAG, "Service Stopping!");
        this.stopSelf();
       /* Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("Received");
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        sendBroadcast(broadcastIntent); */
    }

    private void upload(DataItem item) {
        String jsonPart1 = "\"collectionId\" : \"" +
                collectionID +
                "\"";
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        item.getMetadata().setDeviceID("1");
        typedFile = new TypedFile("multipart/form-data", new File(item.getLocalPath()));


        // json = "{" + jsonPart1 + "}";
        json = "{" + jsonPart1 + "}";

        Log.v(TAG, json);
        RetrofitClient.uploadItem(typedFile, json, callback, username, password);


    }


    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Toast.makeText(mContext.getApplicationContext(), "Uploaded Successfully", Toast.LENGTH_LONG).show();
            Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());




          /*  List<DataItem> tempList =  dataList;
            for(int i = 0; i<dataList.size(); i++){
                DataItem d = tempList.get(i);
                dataList.remove(d);
            } */


            if (new Select()
                    .from(DataItem.class)
                    .where("isLocal = ?", true)
                    .execute().size() < 1) {
                //upload a POI as Album on Imeji
                // RetrofitClient.createPOI(createNewPOI(), callbackPoi, username, password);

                //You cannot modify, add/remove, a List while iterating through it.
                //The foreach loop you are using creates an Iterator object in the background.
                // Use a regular for loop if you'd like to modify the list.

//            for (DataItem item: dataList){
//                //if(item.getFilename().equals(dataItem.getFilename())){
//                    dataList.remove(item);
//                //}
//            }

//            List<DataItem> tempList =  dataList;
//            for(int i = 0; i<dataList.size(); i++){
//                DataItem d = tempList.get(i);
//                dataList.remove(d);
//            }


            }
        }

        @Override
        public void failure(RetrofitError error) {

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                Toast.makeText(mContext.getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {
                    Toast.makeText(mContext.getApplicationContext(), "File already synced ", Toast.LENGTH_SHORT).show();
                }
                else if (jsonBody.contains("Unauthorized")) {
                    Toast.makeText(mContext.getApplicationContext(), "Unauthorized access ", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(mContext.getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();

            }

            //Log.v(LOG_TAG, jsonBody);

            Log.v(TAG, String.valueOf(error));

        }
    };
}
