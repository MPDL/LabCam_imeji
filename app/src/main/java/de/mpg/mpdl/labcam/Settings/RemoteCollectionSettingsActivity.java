package de.mpg.mpdl.labcam.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.mpg.mpdl.labcam.Auth.QRScannerActivity;
import de.mpg.mpdl.labcam.AutoRun.ManualUploadService;
import de.mpg.mpdl.labcam.AutoRun.TaskUploadService;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.UploadActivity.CollectionIdInterface;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RemoteCollectionSettingsActivity extends AppCompatActivity implements CollectionIdInterface{

    private final String LOG_TAG = RemoteCollectionSettingsActivity.class.getSimpleName();
    private static final int INTENT_QR = 1001;
    public static final int INTENT_NONE = 7991;
    //user info
    private String username;
    private String userId;
    private String apiKey;
    private String email;
    private SharedPreferences mPrefs;

    private SettingsListAdapter adapter;
    private ListView listView;
    private Activity activity = this;
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

    //flag
    boolean isNone = true;

    Handler handler;
    Task task;

    private ProgressDialog pDialog = null;

    private String serverUrl;



    Callback<CollectionMessage> callback = new Callback<CollectionMessage>() {
        @Override
        public void success(CollectionMessage collectionMessage, Response response) {

            List<ImejiFolder> folderList = new ArrayList<>();
            folderList = collectionMessage.getResults();

            if(folderList.size()==0){
                // first delete AutoTask
                new Delete().from(Task.class).where("uploadMode = ?", "AU").execute();
                // create dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);
                builder.setTitle("Create Collection")
                        .setMessage("There is no collection available, create one by giving a name")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // create dialog and create collection
                                pDialog = new ProgressDialog(activity);
                                pDialog.setMessage("Loading...");
                                pDialog.show();
                                if(String.valueOf(input.getText()).equalsIgnoreCase("")){
                                    Toast.makeText(activity,"canceled create collection",Toast.LENGTH_SHORT).show();
                                    pDialog.dismiss();
                                    return;
                                }
                                RetrofitClient.createCollection(String.valueOf(input.getText()),"no description yet",createCollection_callback,apiKey);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.error_alert)
                        .show();
            }


            new Delete().from(ImejiFolder.class).execute();



            ActiveAndroid.beginTransaction();
            try {
                collectionListLocal.clear();
                for(ImejiFolder folder : folderList){
                    Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                    Log.v(LOG_TAG, "collection id: " + String.valueOf(folder.id));

                    //check if origin AU collection still exist
                    if(folder.getImejiId() == collectionID){
                        isNone = false;
                    }

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

    Callback<ImejiFolder> createCollection_callback = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.v(LOG_TAG, "createCollection_callback success");
//            isFirstCollection = true;

            pDialog.dismiss();
            updateFolder();
            //get collection list

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, error.getMessage());
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
        apiKey = mPrefs.getString("apiKey", "");
        email = mPrefs.getString("email", "");
        serverUrl = mPrefs.getString("server","");

        /** scan QR **/
        Button qrCodeImageView = (Button) findViewById(R.id.im_qr_scan);
        qrCodeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, QRScannerActivity.class);
                startActivityForResult(intent, INTENT_QR);
            }
        });

        listView = (ListView) findViewById(R.id.settings_remote_listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new SettingsListAdapter(activity, collectionListLocal,this);
        listView.setAdapter(adapter);

        //save collection folder
//        saveCollection();
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
        RetrofitClient.getGrantCollectionMessage(callback, apiKey);
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

            getCollectionNameById(collectionID);

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
            task.setSeverName(serverUrl);

//            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));

            task.save();
            Log.v(LOG_TAG, "finish");

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();

        }else if(!latestTask.getCollectionId().equals(collectionID)) {

            // case2: already have auto task
            if(latestTask!=null){
            Log.v("latestTask", latestTask.getCollectionId());
                // make sure task is not finished
                  if(latestTask.getTotalItems()>latestTask.getFinishedItems())
                  {
                      String oldCollectionName = latestTask.getCollectionName();
                      // stop latestTask, change mode/name, save
                      Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                      activity.stopService(uploadIntent);
                      Log.e(LOG_TAG, "stop you service!");

                      List<Image> allImage = new Select().from(Image.class).execute();

                      //log
                      Log.e(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                      for(Image image:allImage){
                          Log.e(LOG_TAG, "imageName: "+image.getImageName());
                          Log.e(LOG_TAG,"getState:" +image.getState());
                      }
                      Log.e(LOG_TAG, "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

                      // now old task is MU, and stopped
                      latestTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                      latestTask.setUploadMode("MU");
                      latestTask.save();
                      Log.e(LOG_TAG, "stop latest!");


                      // Num of photo
//                      int photoNum = latestTask.getTotalItems() - latestTask.getFinishedItems();
                      // show dialog
                      dialog(oldCollectionName);
                  }
            }
        }else {
           // save task
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }
    private void getCollectionNameById(String collectionID){
        //get collection Name form Id
        for (ImejiFolder imejiFolder:collectionListLocal){
            if (imejiFolder.getImejiId().equalsIgnoreCase(collectionID) ){
                collectionName = imejiFolder.getTitle();
                Log.v(LOG_TAG,"getCollectionName:"+collectionName);
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_QR) {

            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                String QRText = bundle.getString("QRText");
                Log.v(LOG_TAG, QRText);
                String APIkey = "";
                String url = "";
                try {
                    JSONObject jsonObject = new JSONObject(QRText);
                    APIkey = jsonObject.getString("key");
                    if(!apiKey.equals(APIkey)){
                        Toast.makeText(activity,"this folder doesn't look like yours",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.v("APIkey",APIkey);
                    url = jsonObject.getString("col");
                    Log.v("col",url);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(activity,"qrCode not legal",Toast.LENGTH_LONG).show();
                    return;
                }


                try {

                    URL u = new URL(url);

                    String path = u.getPath();
                    String qrCollectionId = "";
                    if (path != null) {
                        try {
                            qrCollectionId = path.substring(path.lastIndexOf("/") + 1);
                            Log.i(LOG_TAG,qrCollectionId);
                        }catch (Exception e){
                            Toast.makeText(activity,"qrCode not legal",Toast.LENGTH_LONG).show();
                            return;
                        }

                        /** set choose **/
                        //create task if collection is selected
                        if (!qrCollectionId.equals("") && !qrCollectionId.equals(null)) {
                            Log.i("~qrCollectionId", qrCollectionId);

                            /**
                             * delete all AU Task if finished
                             * */
                            DeviceStatus.deleteFinishedAUTasks();
                            collectionID = qrCollectionId;
                            /**create Task**/
                            createTask(qrCollectionId);

                        } else {
                            Toast.makeText(context, "collection setting not changed", Toast.LENGTH_LONG).show();
                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the photo picking
            }
        }
    }

    @Override
    public void setCollectionId(int Id,boolean isSet) {
        collectionID = collectionListLocal.get(Id).getImejiId();
        collectionName = collectionListLocal.get(Id).getTitle();

        // collection not changed
        if(isSet){
            return;
        }

        if (!collectionID.equals("") && !collectionID.equals(null)) {
            Log.i("~collectionID", collectionID);

            /**
             * delete all AU Task if finished
             * */
            DeviceStatus.deleteFinishedAUTasks();

            /**create Task**/
            createTask(collectionID);

        }
    }


    //checkbox dialog
    private void dialog(String oldCollectionName){

        getCollectionNameById(collectionID);
        final String[] arrayCollection = new String[] { oldCollectionName, collectionName };

        final AlertDialog alertDialog =
                new AlertDialog.Builder(context)
                        .setTitle("There are some photos waiting for uploading. Upload them to ")
                        .setSingleChoiceItems(arrayCollection, 0, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if(i ==0){
                                    //oldCollectionName selected
                                    // yes continue latestTask
                                    String taskId = latestTask.getTaskId();

                                    // change state of old task
                                    // MU, STOPPED to WAITING
                                    latestTask.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    latestTask.save();

                                    // continue upload old task as "MU"
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
                                    task.setSeverName(serverUrl);

                                    Long now = new Date().getTime();
                                    task.setStartDate(String.valueOf(now));
                                    task.setCollectionName(collectionName);

                                    task.save();

                                    //start auto upload service
                                    Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                                    activity.startService(uploadIntent);

                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();

                                }else if(i == 1){
                                    // new collection selected

                                    // latest task already stopped
                                    // get remain Images



                                    List<Image> remainImages = null;
                                     remainImages = new Select().from(Image.class)
                                            .where("taskId = ?", latestTask.getTaskId())
                                            .where("state != ?", String.valueOf(DeviceStatus.state.FINISHED))
                                            .where("state != ?", String.valueOf(DeviceStatus.state.STARTED))
                                            .execute();

                                    // change totalNum of old task
                                    // handle change folder during uploading
                                    int remainImageNum = 0;
                                    if(remainImages!=null){
                                        remainImageNum = remainImages.size();
                                    }

                                    // warning: latestTask is a MU task now
                                    latestTask.setTotalItems(latestTask.getTotalItems() - remainImageNum);
                                    task.setEndDate(DeviceStatus.dateNow());
                                    latestTask.setState(String.valueOf(DeviceStatus.state.FINISHED));
                                    latestTask.save();

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

//                                    //delete old taskImage
//                                    new Delete().from(Image.class)
//                                            .where("taskId = ?", latestTask.getTaskId())
//                                            .where("state = ?", String.valueOf(DeviceStatus.state.FINISHED))
//                                            .execute();

                                    //create new task
                                    Log.v("collectionID", collectionID);
                                    task = new Task();
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


                                    /**
                                     * latest
                                     */

                                    List<Image> fsfasdfasf = new Select().from(Image.class)
                                            .where("taskId = ?", latestTask.getTaskId())
                                            .execute();

                                    Log.e(LOG_TAG, "now print the latestTask,set!");
                                    Log.e(LOG_TAG, "first:" + latestTask.getState());
                                    Log.e(LOG_TAG, "getTotalItems:  " + latestTask.getTotalItems());
                                    Log.e(LOG_TAG, "getFinishedItems:  " + latestTask.getFinishedItems());

                                    for(Image image:fsfasdfasf){
                                        Log.e(LOG_TAG, "imageName: "+image.getImageName());
                                        Log.e(LOG_TAG,"getState:" +image.getState());
                                    }

                                    List<Image> dddd = new Select().from(Image.class)
                                            .where("taskId = ?", task.getTaskId())
                                            .execute();

                                    Log.e(LOG_TAG, "now print the task!");
                                    Log.e(LOG_TAG, "first:" + task.getState());
                                    Log.e(LOG_TAG, "getTotalItems:  " + task.getTotalItems());
                                    Log.e(LOG_TAG, "getFinishedItems:  " + task.getFinishedItems());

                                    for(Image image:dddd){
                                        Log.e(LOG_TAG, "imageName: "+image.getImageName());
                                        Log.e(LOG_TAG,"getState:" +image.getState());
                                    }

                                    Log.e(LOG_TAG,"+++++++++++++++++++++");

                                    //start service
                                    Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                                    startService(uploadIntent);


                                    Intent intent = new Intent();
                                    setResult(RESULT_OK, intent);
                                    finish();                                        }
                            }
                        })
                        .setIcon(R.drawable.error_alert)
                        .show();

        // not dismiss by wrong click
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

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
//
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                Intent intent = new Intent();
//                Task auTask = DeviceStatus.getAuTask(userId);
//                if(auTask==null){
//                    intent.putExtra("isNone", isNone);
//                }
////                setResult(INTENT_NONE,intent);
//                finish();

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }
}
