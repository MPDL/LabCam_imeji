package example.com.mpdlcamera.Settings;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Folder.MainActivity;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.MessageModel.CollectionMessage;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.UploadFragment.CollectionIdInterface;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class RemoteCollectionSettingsActivity extends AppCompatActivity implements CollectionIdInterface{
    private String username;
    private String password;
    private SharedPreferences mPrefs;

    private ProgressDialog pDialog;

    private SettingsListAdapter adapter;
    private ListView listView;
    private Activity activity = this;
    private final String LOG_TAG = RemoteCollectionSettingsActivity.class.getSimpleName();
    private View rootView;
    private Toolbar toolbar;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();

    private String collectionID;
    private CollectionIdInterface ie;



    Callback<JsonObject> callback = new Callback<JsonObject>() {
        @Override
        public void success(JsonObject jsonObject, Response response) {
            pDialog.hide();

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
            pDialog.hide();
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
        password = mPrefs.getString("password", "");


        collectionListLocal = new Select()
                .from(ImejiFolder.class)
                .execute();
        Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");


        listView = (ListView) findViewById(R.id.settings_remote_listView);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        adapter = new SettingsListAdapter(activity, collectionListLocal,ie);
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
        hidePDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hidePDialog();
    }


    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


    private void updateFolder(){
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Loading...");
        pDialog.show();
        RetrofitClient.getCollectionMessage(callback, username, password);
    }

    private void saveCollection(){
        Button saveButton = (Button) rootView.findViewById(R.id.save_collection);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.i("collectionID",collectionID);
            }
        });

    }

    @Override
    public void setCollectionId(int Id) {
        collectionID = collectionListLocal.get(Id).getImejiId();
    }
}
