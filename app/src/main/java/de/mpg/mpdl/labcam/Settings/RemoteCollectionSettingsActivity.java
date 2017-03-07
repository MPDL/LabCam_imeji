package de.mpg.mpdl.labcam.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
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

import de.mpg.mpdl.labcam.Auth.QRScannerActivity;
import de.mpg.mpdl.labcam.AutoRun.TaskUploadService;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.UploadActivity.CollectionIdInterface;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.Utils.QRUtils;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

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

    private SettingsListAdapter adapter;
    private ListView listView;
    private Activity activity = this;
    private View rootView;
    private Toolbar toolbar;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();

    private String collectionId = "";
    private String collectionName;

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
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                // Set up the input
                final EditText input = new EditText(activity);
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
                    if(folder.getImejiId() == collectionId){
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

            collectionListLocal.clear();
            collectionListLocal = new Select().from(ImejiFolder.class).execute();
            Log.v(LOG_TAG,collectionListLocal.size()+"");

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

        username = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_NAME, "");
        userId = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        apiKey = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
        email = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.EMAIL, "");
        serverUrl = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

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
        latestTask = DBConnector.getAuTask(userId,serverUrl);
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
            task.setServerName(serverUrl);

            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));

            task.save();
            Log.v(LOG_TAG, "finish");

            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();

        }else if(!latestTask.getCollectionId().equals(collectionID)) {

            // case2: already have auto task

            Log.v("latestTask", latestTask.getCollectionId());
                // make sure task is not finished
                  if(latestTask.getTotalItems()>latestTask.getFinishedItems())
                  {
                      String oldCollectionName = latestTask.getCollectionName();
                      // stop latestTask, change mode/name
                      Intent uploadIntent = new Intent(activity, TaskUploadService.class);
                      activity.stopService(uploadIntent);      // AU service stopped

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

//    apiKey  LOG_TAG activity userId serverUrl collectionID

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_QR) {

            if (resultCode == Activity.RESULT_OK) {
                String qrCollectionId = QRUtils.processQRCode(data, activity, LOG_TAG, apiKey).getQrCollectionId();
                /** set choose **/
                //create task if collection is selected
                if (qrCollectionId != null && !qrCollectionId.equals("")) {
                    Log.i("~qrCollectionId", qrCollectionId);

                    DBConnector.deleteFinishedAUTasks(userId, serverUrl);             //delete all AU Task if finished
                    collectionId = qrCollectionId;
                    /**create Task**/
                    createTask(qrCollectionId);

                } else {
                    Toast.makeText(activity, "collection setting not changed", Toast.LENGTH_LONG).show();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // User cancelled the photo picking
            }
        }
    }

    @Override
    public void setCollectionId(int Id,boolean isSet) {
        collectionId = collectionListLocal.get(Id).getImejiId();
        collectionName = collectionListLocal.get(Id).getTitle();

        // collection not changed
        if(isSet){
            return;
        }

        if (!collectionId.equals(null) && !("").equals(collectionId)) {
            Log.i("~collectionID", collectionId);

            DBConnector.deleteFinishedAUTasks(userId, serverUrl);             //delete all AU Task if finished

            /**create Task**/
            createTask(collectionId);

        }
    }


    //checkbox dialog
    private void dialog(String oldCollectionName){

        getCollectionNameById(collectionId);
        final String[] arrayCollection = new String[] { oldCollectionName, collectionName };

        final AlertDialog alertDialog =
                new AlertDialog.Builder(activity)
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
                                    latestTask.setUploadMode("AU");
                                    latestTask.save();

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

                                    for (Image remainImage : remainImages) {
                                        Log.e(LOG_TAG, "========================================================");
                                        Log.e(LOG_TAG, "now print remainImages!");
                                        Log.e(LOG_TAG, "getImageName:  " + remainImage.getImageName());
                                        Log.e(LOG_TAG, "getState:  " + remainImage.getState());
                                        Log.e(LOG_TAG, "========================================================");

                                    }

                                    // change totalNum of old task
                                    // handle change folder during uploading
                                    int remainImageNum = 0;
                                    if(remainImages!=null){
                                        remainImageNum = remainImages.size();
                                    }


                                    // warning: latestTask is a MU task now
                                    latestTask.setUploadMode("AU_FINISHED");
                                    latestTask.setTotalItems(latestTask.getTotalItems() - remainImageNum);
                                    latestTask.setEndDate(DeviceStatus.dateNow());
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

                                    //create new task
                                    Log.v("collectionID", collectionId);
                                    task = new Task();
                                    task.setTotalItems(remainImages.size());
                                    task.setFinishedItems(0);

                                    task.setTaskId(uniqueID);
                                    task.setUploadMode("AU");
                                    task.setCollectionId(collectionId);
                                    task.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    task.setUserName(username);
                                    task.setUserId(userId);
                                    task.setServerName(serverUrl);

                                    Long now = new Date().getTime();
                                    task.setStartDate(String.valueOf(now));
                                    task.setCollectionName(collectionName);

                                    task.save();

                                    // start TaskUploadService here
                                    Intent uploadIntent = new Intent(activity, TaskUploadService.class);

                                    activity.startService(uploadIntent);

                                    List<Image> fsfasdfasf = new Select().from(Image.class)
                                            .where("taskId = ?", latestTask.getTaskId())
                                            .execute();

                                    Log.e(LOG_TAG, "now print the old AU Task,set!");
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

                                    Log.e(LOG_TAG, "now print new AU task!");
                                    Log.e(LOG_TAG, "first:" + task.getState());
                                    Log.e(LOG_TAG, "getTotalItems:  " + task.getTotalItems());
                                    Log.e(LOG_TAG, "getFinishedItems:  " + task.getFinishedItems());

                                    for(Image image:dddd){
                                        Log.e(LOG_TAG, "imageName: "+image.getImageName());
                                        Log.e(LOG_TAG,"getState:" +image.getState());
                                    }

                                    Log.e(LOG_TAG,"+++++++++++++++++++++");

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
