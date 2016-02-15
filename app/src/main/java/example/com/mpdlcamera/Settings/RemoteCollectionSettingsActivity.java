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
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.LocalUser;
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
    private CollectionIdInterface ie;

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
        email = mPrefs.getString("email", "");

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
                    deleteFinishedTasks();

                        /**create Task**/
                    createTask(collectionID);

                    Log.v("task.size", "" + DeviceStatus.getTasks().size());
                    for (Task task : DeviceStatus.getTasks()) {
                        if (task.getTaskName() != null) {
                            Log.v("~taskName", task.getTaskName());
                        }
                    }
                    //TODO: set collection id for local user
                    if (DeviceStatus.is_newUser(email)) {
                        LocalUser user = new LocalUser(email, collectionID, collectionName);
                        user.save();
                        Toast.makeText(context, "user collectionID saved", Toast.LENGTH_LONG).show();
                    } else {
                        LocalUser user = new Select().from(LocalUser.class).where("email = ?", email).executeSingle();
                        user.setCollectionId(collectionID);
                        user.setCollectionName(collectionName);
                        user.save();
                        Toast.makeText(context, "user collectionID updated", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(context, "collection setting not changed", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });

    }

    private void createTask(String collectionID){

        // get AU task
        latestTask = getAUTask();

        //  create first task
        if(latestTask==null){
            Log.v("create Task", "no task in database");
            Task task = new Task();

            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(collectionID);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(username);
            task.setTotalItems(0);
            task.setFinishedItems(0);

//            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));
            task.setTaskName("Auto upload to" + collectionName);

            task.save();

        }else if(!latestTask.getCollectionId().equals(collectionID)) {
            // create second Auto task
            if(latestTask!=null){
            Log.v("latestTask", latestTask.getCollectionId());
                // make sure task is not finished (finished tasks already deleted anyway)
                  if(latestTask.getTotalItems()>latestTask.getFinishedItems())
                  {
                      // stop latestTask, change mode/name, save
                      latestTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                      latestTask.setUploadMode("MU");
                      latestTask.setTaskName("Manual upload to" + collectionName);
                      latestTask.save();

                      // Num of photo
                      int photoNum = latestTask.getTotalItems() - latestTask.getFinishedItems();
                      // show dialog
                      dialog(photoNum);
                  }
            }
        }
    }

    //get latest task
    // TODO: move away for reuse
    public static Task getAUTask() {
        String mode = "AU";
        return new Select()
                .from(Task.class)
                .where("uploadMode = ?",mode)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    @Override
    public void setCollectionId(int Id) {
        collectionID = collectionListLocal.get(Id).getImejiId();
        collectionName = collectionListLocal.get(Id).getTitle();
    }

    //delete finished tasks
    public void deleteFinishedTasks(){

        // get All au tasks first
        List<Task> finishedTasks = new Select()
                .from(Task.class)
                .execute();
        Log.v(LOG_TAG,finishedTasks.size()+"_all");

        // remove unfinished tasks form list
        for(Task task:finishedTasks){
            if(task.getFinishedItems() == task.getTotalItems()){
                task.setState(String.valueOf(DeviceStatus.state.FINISHED));
                task.save();
            }
        }

        new Delete().from(Task.class).where("state = ?", String.valueOf(DeviceStatus.state.FINISHED)).execute();

        int num = (new Select()
                .from(Task.class)
                .execute()).size();
        Log.v(LOG_TAG,num +"_finished");
    }

    //checkbox dialog
    private void dialog(int num){

        new AlertDialog.Builder(context)
                .setTitle("Delete entry")
                .setMessage("There are "+num+"photos not uploaded yes, please select")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // yes continue latestTask
                        String taskId = latestTask.getTaskId();
                        Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
                        manualUploadServiceIntent.putExtra("currentTaskId", taskId);
                        startService(manualUploadServiceIntent);

                        //create new task
                        Log.v("collectionID",collectionID);
                        Task task = new Task();
                        task.setTotalItems(0);
                        task.setFinishedItems(0);

                        String uniqueID = UUID.randomUUID().toString();
                        task.setTaskId(uniqueID);
                        task.setUploadMode("AU");
                        task.setCollectionId(collectionID);
                        task.setState(String.valueOf(DeviceStatus.state.WAITING));
                        task.setUserName(username);

                        Long now = new Date().getTime();
                        task.setStartDate(String.valueOf(now));
                        task.setTaskName("Auto upload to " + collectionName);

                        task.save();

                        //task
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // no move picture to new repository

                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
