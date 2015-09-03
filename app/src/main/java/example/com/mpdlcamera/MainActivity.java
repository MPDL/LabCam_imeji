package example.com.mpdlcamera;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Folder.FolderGridAdapter;
import example.com.mpdlcamera.Items.ItemsActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity {

    private View rootView;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String username;
    private String password;
    private SharedPreferences mPrefs;

    private ProgressDialog pDialog;
    private Activity activity = this;
    private FolderGridAdapter adapter;
    private GridView gridview;
    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();
    private ImejiFolder collectionLocal = new ImejiFolder();

    Callback<List<ImejiFolder>> callback = new Callback<List<ImejiFolder>>() {
        @Override
        public void success(List<ImejiFolder> folderList, Response response) {
            ActiveAndroid.beginTransaction();
            try {
                collectionListLocal.clear();
                for(ImejiFolder folder : folderList){
                    Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                    Log.v(LOG_TAG, "collection id: " + String.valueOf(folder.id));

                    //TODO fetch items
                    getFolderItems(folder.id);
                    collectionLocal = folder;

                    collectionListLocal.add(folder);
                    //folder.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();
                adapter.notifyDataSetChanged();
            }

            if(pDialog != null) {
                pDialog.hide();
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
            DeviceStatus.showSnackbar(rootView, "update data failed");
        }
    };

    Callback<List<DataItem>> callbackItems = new Callback<List<DataItem>>() {
        @Override
        public void success(List<DataItem> dataList, Response response) {
            List<DataItem> dataListLocal = new ArrayList<DataItem>();
            if(dataList != null) {
                ActiveAndroid.beginTransaction();
                try {
                    for (DataItem item : dataList) {
                        dataListLocal.add(item);
                        item.save();
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();

                    collectionLocal.setItems(dataList);
                    collectionLocal.save();

                    //adapter.notifyDataSetChanged();
                    adapter = new FolderGridAdapter(activity, collectionListLocal);
                    gridview.setAdapter(adapter);
                }
            }else{
                DeviceStatus.showToast(activity, "no items");
                Log.v(LOG_TAG, "no items");

            }

           Log.v(LOG_TAG, "get list OK");

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
            DeviceStatus.showToast(activity, "update data failed");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");


        collectionListLocal = new Select()
                .from(ImejiFolder.class)
                .execute();
        Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");


        adapter = new FolderGridAdapter(activity, collectionListLocal);

        gridview = (GridView) findViewById(R.id.folder_gridView);
        registerForContextMenu(gridview);

        gridview.setAdapter(adapter);



        // Set OnItemClickListener so we can be notified on item clicks
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ImejiFolder folder = (ImejiFolder) adapter.getItem(position);
                DeviceStatus.showSnackbar(rootView, folder.getTitle()
                        + "\n" + "Long press to delete.");

                //TODO show Items inside the folder
                Intent showItemsIntent = new Intent(activity, ItemsActivity.class);
                showItemsIntent.putExtra(Intent.EXTRA_TEXT, folder.id);
                startActivity(showItemsIntent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        hidePDialog();
    }

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


    private void updateFolder(){
        pDialog = new ProgressDialog(activity);
        pDialog.setMessage("Loading...");
        pDialog.show();
        RetrofitClient.getCollections(callback, username, password);
    }

    private void getFolderItems(String collectionId){
        RetrofitClient.getCollectionItems(collectionId, callbackItems, username, password);
    }


}
