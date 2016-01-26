package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Model.MessageModel.CollectionMessage;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.TaskManager.TaskProvider;
import example.com.mpdlcamera.UploadFragment.CollectionIdInterface;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RemoteCollectionSettingsActivity extends AppCompatActivity implements CollectionIdInterface{
    private String username;
    private String APIkey;
    private SharedPreferences mPrefs;

    private SettingsListAdapter adapter;
    private ListView listView;
    private Activity activity = this;
    private final String LOG_TAG = RemoteCollectionSettingsActivity.class.getSimpleName();
    private View rootView;
    private Toolbar toolbar;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();

    private String collectionID;
    private String collectionName;

    private int selectedItem;
    private CollectionIdInterface ie;



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
                collectionListLocal.clear();
                for(ImejiFolder folder : folderList){
                    Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                    Log.v(LOG_TAG, "collection id: " + String.valueOf(folder.id));
                    folder.setImejiId(folder.id);
                    collectionListLocal.add(folder);
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();
                Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");

                adapter.notifyDataSetChanged();
            }

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
            DeviceStatus.showSnackbar(rootView, "update data failed");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_remote);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        APIkey = mPrefs.getString("apiKey", "");


        collectionListLocal = new Select()
                .from(ImejiFolder.class)
                .execute();
        Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");


        listView = (ListView) findViewById(R.id.settings_remote_listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new SettingsListAdapter(activity, collectionListLocal,this);
        listView.setAdapter(adapter);

        //save collection folder
        saveCollection();
    }

    @Override
    public void onStart() {
        super.onStart();
        updateFolder();
    }

    @Override
    public void onResume(){
        super.onResume();

    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateFolder(){
        RetrofitClient.getCollectionMessage(callback, APIkey);
    }

    private void saveCollection(){
        Button saveButton = (Button) rootView.findViewById(R.id.save_collection);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // default collectionID

                Log.i("~collectionID", collectionID);
                createTask(collectionID);

                Log.v("task.size",""+DeviceStatus.getTasks().size());
                for(Task task:DeviceStatus.getTasks()){
                    if(task.getTaskName()!=null){
                    Log.v("~taskName",task.getTaskName());}
                }
            }
        });

    }

    private void createTask(String collectionID){
        mPrefs = getSharedPreferences("myPref", 0);
        String userName = mPrefs.getString("username", "");

        Task latestTask = getTask();

        if(latestTask==null){
            Log.v("create Task", "no task in database");
            Task task = new Task();

            TaskProvider taskProvider = new TaskProvider();
            taskProvider.insert(Uri.parse("content://example.com.mpdlcamera/") ,new ContentValues());

            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionID);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(userName);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));
            task.setTaskName("AU to" + collectionName + currentDateTimeString);

            task.save();

        }else if(!latestTask.getCollectionId().equals(collectionID)) {
            if(latestTask!=null){
            Log.v("latestTask",latestTask.getCollectionId());}
            else {
                Log.v("latestTask","latest task is null");
            }
            Log.v("collectionID",collectionID);
            Task task = new Task();
            task.setTotalItems(0);
            task.setFinishedItems(0);

            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionID);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(userName);

            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));
            task.setTaskName("AU to" + collectionName + currentDateTimeString);

            task.save();
        }
    }

    //get latest task
    // TODO: move away for reuse
    public static Task getTask() {
        return new Select()
                .from(Task.class)
                .orderBy("startDate DESC")
                .executeSingle();
    }



    @Override
    public void setCollectionId(int Id) {
        collectionID = collectionListLocal.get(Id).getImejiId();
        collectionName = collectionListLocal.get(Id).getTitle();
    }
}
