package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseMvpActivity;
import de.mpg.mpdl.labcam.code.common.adapter.SettingsListAdapter;
import de.mpg.mpdl.labcam.code.common.callback.CollectionIdInterface;
import de.mpg.mpdl.labcam.code.common.service.TaskUploadService;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.injection.component.DaggerCollectionComponent;
import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
import de.mpg.mpdl.labcam.code.mvp.presenter.RemoteCollectionSettingsPresenter;
import de.mpg.mpdl.labcam.code.mvp.view.RemoteCollectionSettingsView;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;
import de.mpg.mpdl.labcam.code.utils.QRUtils;

public class RemoteCollectionSettingsActivity extends BaseMvpActivity<RemoteCollectionSettingsPresenter> implements RemoteCollectionSettingsView, CollectionIdInterface{

    @BindView(R.id.settings_remote_listView)
    ListView listView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private final String LOG_TAG = RemoteCollectionSettingsActivity.class.getSimpleName();
    private BaseMvpActivity activity = this;
    private static final int INTENT_QR = 1001;
    public static final int INTENT_NONE = 7991;

    private String username;
    private String userId;
    private String apiKey;
    private String collectionId = "";
    private String collectionName;
    private String serverUrl;
    boolean isNone = true;

    private SettingsListAdapter adapter;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();

    Task latestTask;
    Task task;
    private ProgressDialog pDialog = null;
    private CollectionIdInterface ie = this;

    @Override
    public void onStart() {
        super.onStart();
        updateFolder();
    }

    private void updateFolder(){
        String q = "role=edit";
        mPresenter.getGrantedCollectionMessage(q, activity);
    }

    private void createTask(String collectionID){

        // get AU task
        latestTask = DBConnector.getAuTask(userId,serverUrl);
        //   case 1: create first task
        if(latestTask==null){
            Log.v("create Task", "no task in database");
            Task task = new Task();

            getCollectionNameById(collectionID);
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

                      // now old task is MU, and stopped
                      latestTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                      latestTask.setUploadMode("MU");
                      latestTask.save();
                      Log.e(LOG_TAG, "stop latest!");

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
                                    // warning: latestTask is a MU task now
                                    latestTask.setUploadMode("AU_FINISHED");
                                    latestTask.setTotalItems(latestTask.getTotalItems() - latestTask.getImagePaths().size());
                                    latestTask.setEndDate(DeviceStatus.dateNow());
                                    latestTask.setState(String.valueOf(DeviceStatus.state.FINISHED));

                                    //create new task
                                    Log.v("collectionID", collectionId);
                                    task = new Task();
                                    task.setTotalItems(latestTask.getImagePaths().size());
                                    task.setFinishedItems(0);
                                    task.setUploadMode("AU");
                                    task.setCollectionId(collectionId);
                                    task.setState(String.valueOf(DeviceStatus.state.WAITING));
                                    task.setUserName(username);
                                    task.setUserId(userId);
                                    task.setServerName(serverUrl);

                                    Long now = new Date().getTime();
                                    task.setStartDate(String.valueOf(now));
                                    task.setCollectionName(collectionName);
                                    task.setImagePaths(latestTask.getImagePaths());
                                    Log.d("Remote", "setImagePaths");
                                    task.save();

                                    // start TaskUploadService here
                                    Intent uploadIntent = new Intent(activity, TaskUploadService.class);

                                    activity.startService(uploadIntent);
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
    protected int getLayoutId() {
        return R.layout.activity_settings_remote;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        username = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_NAME, "");
        userId = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        apiKey = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.API_KEY, "");
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

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new SettingsListAdapter(activity, collectionListLocal,this);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void injectComponent() {
        DaggerCollectionComponent.builder()
                .applicationComponent(getApplicationComponent())
                .collectionMessageModule(new CollectionMessageModule())
                .build()
                .inject(this);
        mPresenter.setView(this);
    }

    @Override
    public void getCollectionsSuc(CollectionMessage collectionMessage) {

        new Delete().from(ImejiFolder.class).execute();
        collectionListLocal.clear();

        for (ImejiFolderModel imejiFolderModel : collectionMessage.getResults()) {   // imejiFolderModel to imejiFolder (active android)
            ImejiFolder imejiFolder = new ImejiFolder();
            imejiFolder.setImejiId(imejiFolderModel.getId());  // parsed Id is ImejiId
            imejiFolder.setContributors(imejiFolderModel.getContributors());
            imejiFolder.setTitle(imejiFolderModel.getTitle());
            imejiFolder.setDescription(imejiFolderModel.getDescription());
            imejiFolder.setModifiedDate(imejiFolderModel.getModifiedDate());
            imejiFolder.setCreatedDate(imejiFolderModel.getCreatedDate());
            collectionListLocal.add(imejiFolder);
        }

        if(collectionListLocal.size()==0){
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
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty("title",String.valueOf(input.getText()));
                            jsonObject.addProperty("description","no description yet");

                            mPresenter.createCollection(jsonObject, activity);
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


        ActiveAndroid.beginTransaction();
        try {
            for(ImejiFolder folder : collectionListLocal){
                //check if origin AU collection still exist
                if(folder.getImejiId() == collectionId){
                    isNone = false;
                }
                folder.save();
            }
            ActiveAndroid.setTransactionSuccessful();
        } finally{
            ActiveAndroid.endTransaction();
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void noInternet() {
        getLocalCollections();
    }

    @Override
    public void getCollectionsFail(Throwable e) {
        getLocalCollections();
    }

    private void getLocalCollections(){
        collectionListLocal = DBConnector.getUserFolders();

        adapter = new SettingsListAdapter(activity, collectionListLocal,ie);
        listView.setAdapter(adapter);
    }

    @Override
    public void createCollectionsSuc(ImejiFolderModel model) {
        Log.v(LOG_TAG, "createCollection_callback success");

        pDialog.dismiss();
        updateFolder();
    }

    @Override
    public void createCollectionsFail(Throwable e) {
    }
}
