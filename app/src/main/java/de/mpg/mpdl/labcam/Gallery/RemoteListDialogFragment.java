package de.mpg.mpdl.labcam.Gallery;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Auth.QRScannerActivity;
import de.mpg.mpdl.labcam.AutoRun.ManualUploadService;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.Settings.SettingsListAdapter;
import de.mpg.mpdl.labcam.UploadActivity.CollectionIdInterface;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by yingli on 2/15/16.
 */
public class RemoteListDialogFragment extends DialogFragment implements CollectionIdInterface {

    private static final String LOG_TAG = RemoteListDialogFragment.class.getSimpleName();
    private RemoteListDialogFragment remoteListDialogFragment = this;
    private static final int INTENT_QR = 1001;

    //user
    private String username;
    private String apiKey;
    private String email;
    private SharedPreferences mPrefs;

    //interface
    private CollectionIdInterface ie = this;


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

        this.setCancelable(false);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_fragment_remote_list, null);
        activity = this.getActivity();
        currentTaskId = getArguments().getString("taskId");

        mPrefs = activity.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        apiKey = mPrefs.getString("apiKey", "");
        email = mPrefs.getString("email", "");

        /** scan QR **/
        Button qrCodeImageView = (Button) view.findViewById(R.id.btn_qr_scan);
        qrCodeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, QRScannerActivity.class);
                startActivityForResult(intent, INTENT_QR);
            }
        });

        // builder
        AlertDialog.Builder b=  new  AlertDialog.Builder(getActivity())
                .setTitle("Choose collection")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                if(collectionId==null || collectionId.equalsIgnoreCase("")){
                                    Log.e(LOG_TAG,"collectionId is null or empty");
                                    //delete task (created earlier)
                                    new Delete().from(Task.class).where("taskId = ?", currentTaskId).execute();
                                    dialog.dismiss();
                                    return;
                                }
                                setMUCollection();

                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //delete task (created earlier)
                                new Delete().from(Task.class).where("taskId = ?", currentTaskId).execute();
                                dialog.dismiss();
                            }
                        }
                );


        //remote folder list
        listView = (ListView) view.findViewById(R.id.settings_remote_listView);
        adapter = new SettingsListAdapter(activity, collectionList,this);
        listView.setAdapter(adapter);


        //updateFolder
        RetrofitClient.getGrantCollectionMessage(callback, apiKey);

        b.setView(view);
        return b.create();
    }

    private void setMUCollection(){
        // set collectionId and save
        if(collectionId!=null){
            getCollectionNameById(collectionId);

            Task manualTask = new Select().from(Task.class).where("taskId = ?", currentTaskId).executeSingle();
            manualTask.setCollectionId(collectionId);
            manualTask.setTaskName("Manual  " + collectionId);
            manualTask.setCollectionName(collectionName);
            manualTask.save();

            Log.e(LOG_TAG,manualTask.getTaskId());
            Log.e(LOG_TAG,manualTask.getCollectionName());
            Intent manualUploadServiceIntent = new Intent(activity,ManualUploadService.class);
            manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
            activity.startService(manualUploadServiceIntent);
            getDialog().dismiss();
        }
    }

    private void getCollectionNameById(String collectionID){
        //get collection Name form Id
        for (ImejiFolder imejiFolder:collectionList){
            if (imejiFolder.getImejiId().equalsIgnoreCase(collectionID) ){
                collectionName = imejiFolder.getTitle();
                Log.v(LOG_TAG,"getCollectionName:"+collectionName);
                return;
            }
        }
    }

    @Override
    public void setCollectionId(int Id, boolean isSet) {
        collectionId = collectionList.get(Id).getImejiId();
        collectionName = collectionList.get(Id).getTitle();
    }


    //callbacks
    private ProgressDialog pDialog = null;
    Callback<CollectionMessage> callback = new Callback<CollectionMessage>() {
        @Override
        public void success(CollectionMessage collectionMessage, Response response) {

            Log.e(LOG_TAG,"collectionMessage success");
            /** get collections **/
            List<ImejiFolder> folderList = new ArrayList<>();
            folderList = collectionMessage.getResults();

            if(folderList.size()==0) {
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
                                if (String.valueOf(input.getText()).equalsIgnoreCase("")) {
                                    Toast.makeText(activity, "canceled create collection", Toast.LENGTH_SHORT).show();
                                    pDialog.dismiss();
                                    return;
                                }
                                RetrofitClient.createCollection(String.valueOf(input.getText()), "no description yet", createCollection_callback, apiKey);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                                new Delete().from(Task.class).where("taskId = ?", currentTaskId).execute();
                                remoteListDialogFragment.dismiss();
                            }
                        })
                        .setIcon(R.drawable.error_alert)
                        .show();
            }

            new Delete().from(ImejiFolder.class).execute();

            ActiveAndroid.beginTransaction();
            try {
                collectionList.clear();
                for(ImejiFolder folder : folderList){
                    folder.setImejiId(folder.id);

                    collectionList.add(folder);

                    ImejiFolder imejiFolder = new ImejiFolder();
                    imejiFolder.setTitle(folder.getTitle());
                    imejiFolder.setImejiId(folder.id);
                    imejiFolder.setContributors(folder.getContributors());
                    imejiFolder.setCoverItemUrl(folder.getCoverItemUrl());
                    imejiFolder.setModifiedDate(folder.getModifiedDate());
                    imejiFolder.save();
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

            try {
                collectionList.clear();
                collectionList = new Select().from(ImejiFolder.class).execute();
                Log.v(LOG_TAG, collectionList.size() + "");

            }catch (Exception e){
                Log.v(LOG_TAG,e.getMessage());
            }
//            Log.v(LOG_TAG,collectionList.get(0).getTitle());
//            Log.v(LOG_TAG, collectionList.get(0).getModifiedDate());


//            adapter.notifyDataSetChanged();
            adapter = new SettingsListAdapter(activity, collectionList,ie);
            listView.setAdapter(adapter);
        }
    };

    Callback<ImejiFolder> createCollection_callback = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.v(LOG_TAG, "createCollection_callback success");
//            isFirstCollection = true;

            pDialog.dismiss();
            // set as MU destination
            collectionId = imejiFolder.id;
            collectionName = imejiFolder.getTitle();
            setMUCollection();

//            RetrofitClient.getGrantCollectionMessage(callback, apiKey);
            //get collection list

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, error.getMessage());
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                    Log.v("APIkey",APIkey);
                    if(!apiKey.equals(APIkey)){
                        Toast.makeText(activity,"this folder doesn't look like yours",Toast.LENGTH_LONG).show();
                        return;
                    }
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
                            collectionId = qrCollectionId;
                            //set and save
                            setMUCollection();

                        } else {
                            Toast.makeText(getActivity(), "collection setting not changed", Toast.LENGTH_LONG).show();
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
}
