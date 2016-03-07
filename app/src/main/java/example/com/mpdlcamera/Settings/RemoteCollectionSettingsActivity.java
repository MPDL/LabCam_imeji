package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import example.com.mpdlcamera.AutoRun.ManualUploadService;
import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.UploadActivity.CollectionIdInterface;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RemoteCollectionSettingsActivity extends AppCompatActivity implements CollectionIdInterface{
    private String username;
    private String userId;
    private String APIkey;
    private String email;
    private SharedPreferences mPrefs;

    private SettingsListAdapter adapter;
    private ListView listView;
    private Activity activity = this;
    private final String LOG_TAG = RemoteCollectionSettingsActivity.class.getSimpleName();
    private View rootView;
    private Toolbar toolbar;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();

    private String collectionID = "";
    private String collectionName;

    private Context context =this;
    private int selectedItem;
    private CollectionIdInterface ie = this;

    //latestTask
    Task latestTask;



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
            new Delete().from(ImejiFolder.class).execute();
            ActiveAndroid.beginTransaction();
            try {
                collectionListLocal.clear();
                for(ImejiFolder folder : folderList){
                    Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                    Log.v(LOG_TAG, "collection id: " + String.valueOf(folder.id));
                    folder.setImejiId(folder.id);
                    collectionListLocal.add(folder);

                    ImejiFolder imejiFolder = new ImejiFolder();
                    imejiFolder.setTitle(folder.getTitle());
                    imejiFolder.setImejiId(folder.id);
                    imejiFolder.setCoverItemUrl(folder.getCoverItemUrl());
                    imejiFolder.setModifiedDate(folder.getModifiedDate());
                    imejiFolder.save();
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

            try {
                collectionListLocal.clear();
                collectionListLocal = new Select().from(ImejiFolder.class).execute();
                Log.v(LOG_TAG,collectionListLocal.size()+"");

            }catch (Exception e){
                Log.v(LOG_TAG,e.getMessage());
            }
//            Log.v(LOG_TAG,collectionListLocal.get(0).getTitle());
//            Log.v(LOG_TAG, collectionListLocal.get(0).getModifiedDate());


//
            adapter = new SettingsListAdapter(activity, collectionListLocal,ie);
            listView.setAdapter(adapter);
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
        userId = mPrefs.getString("userId","");
        APIkey = mPrefs.getString("apiKey", "");
        email = mPrefs.getString("email", "");



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
        RetrofitClient.getGrantCollectionMessage(callback, APIkey);
    }

    private void saveCollection(){
        Button saveButton = (Button) rootView.findViewById(R.id.save_collection);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //create task if collection is selected
                if (!collectionID.equals("") && !collectionID.equals(null)) {
                    Log.i("~collectionID", collectionID);


                    /**
                     * delete all AU Task if finished
                     * */
                    DeviceStatus.deleteFinishedTasks();

                    /**create Task**/
                    createTask(collectionID);

                } else {
                    Toast.makeText(context, "collection setting not changed", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void createTask(String collectionID){

        // get AU task
        latestTask = new Select()
                .from(Task.class)
                .where("uploadMode = ?","AU")
                .orderBy("startDate DESC")
                .executeSingle();

        //   case 1: create first task
        if(latestTask==null){
            Log.v("create Task", "no task in database");
            Task task = new Task();

            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionID);
            task.setCollectionName(collectionName);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(username);
            task.setUserId(userId);
            task.setTotalItems(0);
            task.setFinishedItems(0);

//            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));

            task.save();

            finish();

        }else if(!latestTask.getCollectionId().equals(collectionID)) {
            // already have auto task
            if(latestTask!=null){
            Log.v("latestTask", latestTask.getCollectionId());
                // make sure task is not finished
                  if(latestTask.getTotalItems()>latestTask.getFinishedItems())
                  {
                      String oldCollectionName = latestTask.getCollectionName();
                      // stop latestTask, change mode/name, save
                      latestTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                      latestTask.setUploadMode("MU");
                      latestTask.save();

                      // Num of photo
//                      int photoNum = latestTask.getTotalItems() - latestTask.getFinishedItems();
                      // show dialog
                      dialog(oldCollectionName);
                  }
            }
        }else {
           // save task
            finish();
        }
    }


    @Override
    public void setCollectionId(int Id) {
        collectionID = collectionListLocal.get(Id).getImejiId();
        collectionName = collectionListLocal.get(Id).getTitle();
    }


    //checkbox dialog
    private void dialog(String oldCollectionName){

        final String[] arrayCollection = new String[] { oldCollectionName, collectionName };

        final AlertDialog alertDialog =
                new AlertDialog.Builder(context)
                        .setTitle("There are some photos waiting for uploading, Upload them to the ")
                        .setSingleChoiceItems(arrayCollection, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i ==0){
                                    //oldCollectionName selected
                                    // yes continue latestTask
                                    String taskId = latestTask.getTaskId();


                                    // change state of old task
                                    latestTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    latestTask.save();

                                    Intent manualUploadServiceIntent = new Intent(activity, ManualUploadService.class);
                                    manualUploadServiceIntent.putExtra("currentTaskId", taskId);
                                    startService(manualUploadServiceIntent);

                                    //create new task
                                    Log.v("collectionID", collectionID);
                                    Task task = new Task();
                                    task.setTotalItems(0);
                                    task.setFinishedItems(0);

                                    String uniqueID = UUID.randomUUID().toString();
                                    task.setTaskId(uniqueID);
                                    task.setUploadMode("AU");
                                    task.setCollectionId(collectionID);
                                    task.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    task.setUserName(username);
                                    task.setUserId(userId);

                                    Long now = new Date().getTime();
                                    task.setStartDate(String.valueOf(now));
                                    task.setCollectionName(collectionName);

                                    task.save();

                                    //start auto upload service
                                    Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                                    activity.startService(uploadIntent);

                                    finish();
                                }else if(i == 1){
                                    // new collection selected
                                    // change totalNum of old task
                                    latestTask.setTotalItems(latestTask.getFinishedItems());
                                    latestTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    latestTask.save();

                                    // stop/delete old upload process
                                    Intent oldUploadIntent = new Intent(activity, TaskUploadService.class);
                                    stopService(oldUploadIntent);

                                    // move picture to new repository
                                    // get remain Images
                                    List<Image> remainImages = new Select().from(Image.class)
                                            .where("taskId = ?", latestTask.getTaskId())
                                            .where("state = ?", String.valueOf(DeviceStatus.state.WAITING))
                                            .execute();

                                    //uniqueID as new AU taskId
                                    String uniqueID = UUID.randomUUID().toString();

                                    //set remainImage taskId
                                    ActiveAndroid.beginTransaction();
                                    try {
                                        for (Image image:remainImages) {

                                            image.setTaskId(uniqueID);
                                            image.save();
                                        }
                                        ActiveAndroid.setTransactionSuccessful();
                                    }
                                    finally {
                                        ActiveAndroid.endTransaction();
                                    }

                                    //delete old taskImage
                                    new Delete().from(Image.class)
                                            .where("taskId = ?", latestTask.getTaskId())
                                            .where("state = ?", String.valueOf(DeviceStatus.state.FINISHED))
                                            .execute();

                                    //create new task
                                    Log.v("collectionID",collectionID);
                                    Task task = new Task();
                                    task.setTotalItems(remainImages.size());
                                    task.setFinishedItems(0);

                                    task.setTaskId(uniqueID);
                                    task.setUploadMode("AU");
                                    task.setCollectionId(collectionID);
                                    task.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    task.setUserName(username);
                                    task.setUserId(userId);

                                    Long now = new Date().getTime();
                                    task.setStartDate(String.valueOf(now));
                                    task.setCollectionName(collectionName);

                                    task.save();

                                    //start service
                                    Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                                    startService(uploadIntent);

                                    finish();
                                }
                            }
                        })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

        // not dismiss by wrong click
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener(){

            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alertDialog.dismiss();
                    }
                });
            }
        } );
    }
}
