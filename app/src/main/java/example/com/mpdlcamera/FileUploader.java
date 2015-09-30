package example.com.mpdlcamera;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;

import example.com.mpdlcamera.Model.DataItem;
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
 * Created by kiran on 29.09.15.
 */
public class FileUploader {

    private String collectionID = DeviceStatus.collectionID;
    public TypedFile typedFile;
    String json;

    private Context context;

    private static final String TAG = "FileUploader";
    private String username = DeviceStatus.username;
    private String password = DeviceStatus.password;

    public FileUploader(Context context) {
        this.context = context;
    }

    public FileUploader() {

    }

    public void upload(DataItem item) {
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
        RetrofitClient.uploadItem(typedFile, json, callback, username, password);

    }


    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            Log.v(TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());


        }

        @Override
        public void failure(RetrofitError error) {

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {
                    // Toast.makeText(mContext.getApplicationContext(), "", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show();

            }

            Log.v(TAG, String.valueOf(error));

        }
    };
}
