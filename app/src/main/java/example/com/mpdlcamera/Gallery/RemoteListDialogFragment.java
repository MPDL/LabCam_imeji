package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import example.com.mpdlcamera.AutoRun.ManualUploadService;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.LocalUser;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Settings.SettingsListAdapter;
import example.com.mpdlcamera.UploadActivity.CollectionIdInterface;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yingli on 2/15/16.
 */
public class RemoteListDialogFragment extends DialogFragment implements CollectionIdInterface {

    private static final String LOG_TAG = RemoteListDialogFragment.class.getSimpleName();
    //user
    private String username;
    private String APIkey;
    private String email;
    private SharedPreferences mPrefs;


    private SettingsListAdapter adapter;
    private ListView listView;
    private Activity activity;

    private String collectionId;
    private String collectionName;
    private List<ImejiFolder> collectionList = new ArrayList<ImejiFolder>();

    //taskId
    String currentTaskId;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_dialog_remote_list, null);
        activity = this.getActivity();
        currentTaskId = getArguments().getString("taskId");

        mPrefs = activity.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        APIkey = mPrefs.getString("apiKey", "");
        email = mPrefs.getString("email", "");

        // builder
        AlertDialog.Builder b=  new  AlertDialog.Builder(getActivity())
                .setTitle("Enter Players")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // set collectionId and save
                                if(collectionId!=null){
                                Task currentTask = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
                                currentTask.setCollectionId(collectionId);
                                currentTask.setTaskName("Manual  " + collectionId);
                                currentTask.setCollectionName(collectionName);
                                    currentTask.save();

                                Log.v(LOG_TAG,currentTask.getTaskId());
                                Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
                                manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
                                activity.startService(manualUploadServiceIntent);
                                }
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                );

        //remote folder list
        listView = (ListView) view.findViewById(R.id.settings_remote_listView);
        adapter = new SettingsListAdapter(activity, collectionList,this);
        listView.setAdapter(adapter);

        //updateFolder
        RetrofitClient.getGrantCollectionMessage(callback, APIkey);

        b.setView(view);
        return b.create();
    }

    @Override
    public void setCollectionId(int Id) {
        collectionId = collectionList.get(Id).getImejiId();
        collectionName = collectionList.get(Id).getTitle();
    }


    //callbacks

    Callback<JsonObject> callback = new Callback<JsonObject>() {
        @Override
        public void success(JsonObject jsonObject, Response response) {

            JsonArray array;
            List<ImejiFolder> folderList = new ArrayList<>();

            array = jsonObject.getAsJsonArray("results");
            Log.i("results", array.toString());
            Gson gson = new Gson();
            for(int i = 0 ; i < array.size() ; i++){
                ImejiFolder imejiFolder = gson.fromJson(array.get(i), ImejiFolder.class);
                folderList.add(imejiFolder);
            }
            ActiveAndroid.beginTransaction();
            try {
                collectionList.clear();
                for(ImejiFolder folder : folderList){
                    folder.setImejiId(folder.id);
                    folder.setTitle(folder.getTitle());
                    collectionList.add(folder);
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();

                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
        }
    };

}
