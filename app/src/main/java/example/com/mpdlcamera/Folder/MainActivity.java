package example.com.mpdlcamera.Folder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Gallery.GalleryListActivity;
import example.com.mpdlcamera.Items.ItemsActivity;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Settings.SettingsActivity;
import example.com.mpdlcamera.Upload.NewFileObserver;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity implements UploadResultReceiver.Receiver {

    private View rootView;
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String username;
    private String password;
    private SharedPreferences mPrefs;

    private ProgressDialog pDialog;
    private Activity activity = this;
    //private FolderGridAdapter adapter;
    //private GridView gridview;
    private NavigationView navigation;

    private FolderListAdapter adapter;
    private ListView listView;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter reAdapter;
    private RecyclerView.LayoutManager reLayoutManager;

    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();
    private ImejiFolder currentCollectionLocal = new ImejiFolder();

    SharedPreferences preferencesFiles;


    Toolbar toolbar;
    CollapsingToolbarLayout collapsingToolbarLayout;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    CoordinatorLayout rootLayout;



    Callback<List<ImejiFolder>> callback = new Callback<List<ImejiFolder>>() {
        @Override
        public void success(List<ImejiFolder> folderList, Response response) {
            ActiveAndroid.beginTransaction();
            try {
                collectionListLocal.clear();
                for(ImejiFolder folder : folderList){
                    Log.v(LOG_TAG, "collection title: " + String.valueOf(folder.getTitle()));
                    Log.v(LOG_TAG, "collection id: " + String.valueOf(folder.id));

                    getFolderItems(folder.id);

                    //TODO Here is a bug, collectionLocal will be random one collection
                    //collectionLocal = folder;

                    collectionListLocal.add(folder);
                    //folder.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();
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
        public void success(List<DataItem> dataList , Response response) {

            if(dataList != null) {
                ActiveAndroid.beginTransaction();
                try {
                    for (ImejiFolder folder : collectionListLocal) {

                        if(dataList.size()>0) {
                            DataItem coverItem = dataList.get(0);
                            //check for each folder, if the current items belongs to the current folder
                            if(coverItem.getCollectionId().equals(folder.id)){
                                folder.setItems(dataList);

                                folder.setCoverItemUrl(coverItem.getWebResolutionUrlUrl());
                                folder.setImejiId(folder.id);
                                folder.save();

                            }
                        }
                    }
                    ActiveAndroid.setTransactionSuccessful();
                } finally {
                    ActiveAndroid.endTransaction();

                    adapter.notifyDataSetChanged();
//                    adapter = new FolderListAdapter(activity, collectionListLocal);
//                    listView.setAdapter(adapter);


                    if(pDialog != null) {
                        pDialog.hide();
                    }
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
          //  DeviceStatus.showToast(activity, "update data failed");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.hello_world, R.string.hello_world);
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);



        navigation = (NavigationView) findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();
                switch (id) {
                    case R.id.navItem1:
                        Intent showLocalImageIntent = new Intent(activity, GalleryListActivity.class);
                        startActivity(showLocalImageIntent);

                        break;
                    case R.id.navItem2:
                        Intent showMainIntent = new Intent(activity, MainActivity.class);
                        startActivity(showMainIntent);

                        break;
                    case R.id.navItem3:
                        Intent showSettingIntent = new Intent(activity, SettingsActivity.class);
                        startActivity(showSettingIntent);
                        break;
                    case R.id.navItem4:

                        break;
                }
                return false;
            }
        });


        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        if(sharedPreferences.getString("status","").isEmpty()) {
            SharedPreferences.Editor editorS = sharedPreferences.edit();
            editorS.putString("status","wifi");
            editorS.commit();

        }
        SharedPreferences.Editor editor = sharedPreferences.edit();


        editor.putString("UploadStatus", "false");
        //editor.putString("status", "wifi");
        editor.commit();

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferencesFiles = getSharedPreferences("gallery", Context.MODE_PRIVATE);

        for(int i = 0; i<albums.length ;i++){
            Log.i("albums",  albums[i]);
        }

        Cursor cur = getContentResolver().query(images, albums, null, null, null);

       // final ArrayList<String> folders = new ArrayList<String>();

        /*
            set the folder path and folder names in shared preferences
         */
        if (cur.moveToFirst()) {
            String album;
            String folderPath;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int path = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                album = cur.getString(albumLocation);
                folderPath = cur.getString(path);
                File file = new File(folderPath);
                String dir = file.getParent();
                SharedPreferences.Editor ed = preferencesFiles.edit();
                ed.putString(album, dir);
                ed.commit();
               // folders.add(album);
                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }







        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");


        collectionListLocal = new Select()
                .from(ImejiFolder.class)
                .execute();
        Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");


        adapter = new FolderListAdapter(activity, collectionListLocal);

        //gridview = (GridView) findViewById(R.id.folder_gridView);
        //registerForContextMenu(gridview);

        listView = (ListView) findViewById(R.id.folder_listView);

//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        recyclerView.setHasFixedSize(true);
//
//        // use a linear layout manager
//        reLayoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(reLayoutManager);
//
//        // specify an adapter (see also next example)
//        reAdapter = new ReAdaptor(myDataset);

        listView.setAdapter(adapter);



        // Set OnItemClickListener so we can be notified on item clicks
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                ImejiFolder folder = (ImejiFolder) adapter.getItem(position);

                Intent showItemsIntent = new Intent(activity, ItemsActivity.class);
                showItemsIntent.putExtra(Intent.EXTRA_TEXT, folder.id);
                showItemsIntent.putExtra("folderTitle", folder.getTitle());
                startActivity(showItemsIntent);
            }
        });


        //TODO why start upload here?

                /*
                    The background service which uploads the files starts here
                 */
                UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
                mReceiver.setReceiver(this);
                Intent intent = new Intent(this, UploadService.class);
                intent.putExtra("receiver", mReceiver);
                this.startService(intent);


                Handler handler = new Handler();

            /*
                NewFile observer which looks for the new file added to the file system starts here
             */
                NewFileObserver newFileObserver = new NewFileObserver(handler,this);
                getApplicationContext().getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,false, newFileObserver);

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
        new Delete().
                from(ImejiFolder.class)
                .execute();
        hidePDialog();
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        drawerToggle.syncState();
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent showSettingIntent = new Intent(activity, SettingsActivity.class);
            startActivity(showSettingIntent);

            return true;
        }
        if (id == R.id.homeAsUp) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }

        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
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
        RetrofitClient.getCollections(callback, username, password);
    }

    private void getFolderItems(String collectionId){
        RetrofitClient.getCollectionItems(collectionId, callbackItems, username, password);
    }



  /*  public class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //acknowledge
            Toast.makeText(MainActivity.this,
                    "Download complete. Download URI: ",
                    Toast.LENGTH_LONG).show();
        }
    }
*/

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 0:

                setProgressBarIndeterminateVisibility(true);
                break;
            case 1:
                /* Hide progress & extract result from bundle */
                setProgressBarIndeterminateVisibility(false);

              //  String[] results = resultData.getStringArray("result");

                /* Update ListView with result */
                //ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_2, results);
                //listView.setAdapter(arrayAdapter);
             //   Toast.makeText(this, "Files are synced", Toast.LENGTH_LONG).show();


                mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor e = mPrefs.edit();
                e.putString("UploadStatus","true");
                e.commit();
//                Intent showLocalImageIntent = new Intent(activity, LocalGalleryActivity.class);
//                startActivity(showLocalImageIntent);



 /*               if(mPrefs.contains("L_A_U")) {

                    if(mPrefs.getBoolean("L_A_U", true)) {

                        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                        View popupView = inflater.inflate(R.layout.logout_confirm, null);
                        final PopupWindow popupWindow = new PopupWindow(
                                popupView,
                                550,
                                300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                        popupWindow.setAnimationStyle(R.style.AnimationPopup);

                        Button yes = (Button) popupView.findViewById(R.id.buttonYes);
                        Button no = (Button) popupView.findViewById(R.id.buttonNo);
                        popupWindow.showAtLocation(findViewById(R.id.navigation), Gravity.CENTER, 0, 0);

                        yes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                finish();
                                Intent loginIntent = new Intent(activity, LoginActivity.class);
                                startActivity(loginIntent);
                            }
                        });

                        no.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                popupWindow.dismiss();
                            }
                        });

                    }
                }*/

                break;
            case 2:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }


}
