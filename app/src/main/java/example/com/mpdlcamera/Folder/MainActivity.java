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
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Gallery.GalleryListActivity;
import example.com.mpdlcamera.ImejiFragment.ImejiFragment;
import example.com.mpdlcamera.Items.ItemsActivity;
import example.com.mpdlcamera.LocalFragment.LocalFragment;
import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.NetChangeManager.NetWorkStateReceiver;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Settings.SettingsActivity;
import example.com.mpdlcamera.Upload.NewFileObserver;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.UploadFragment.UploadFragment;
import example.com.mpdlcamera.UserFragment.UserFragment;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import example.com.mpdlcamera.NetChangeManager.NetChangeObserver;
/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity implements UploadResultReceiver.Receiver,NetChangeObserver {

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

    private List<ImejiFolder> collectionListLocal = new ArrayList<ImejiFolder>();
    private ImejiFolder currentCollectionLocal = new ImejiFolder();

    SharedPreferences preferencesFiles;

    //TESTING DB
    private boolean isAdd = false;



    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    //new ui
    TabLayout tabLayout;
    ViewPager viewPager;


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

                    /**
                     * TODO: temp disable
                     */
//                    adapter.notifyDataSetChanged();
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
//        setContentView(R.layout.main);
        setContentView(R.layout.activity_main);
        Log.v("Main activity", "started");

        //init DB
        initDB();

        // register NetStateObserver
        NetWorkStateReceiver.registerNetStateObserver(this);

        initInstances();

        /**
         * DO NOT DELETE
         * use tab layout instead
         */
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
//        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.hello_world, R.string.hello_world);
//        drawerLayout.setDrawerListener(drawerToggle);
//
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);
//
//        rootLayout = (CoordinatorLayout) findViewById(R.id.rootLayout);
//
//        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
//
//
//
//        navigation = (NavigationView) findViewById(R.id.navigation);
//        navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
//            @Override
//            public boolean onNavigationItemSelected(MenuItem menuItem) {
//                int id = menuItem.getItemId();
//                switch (id) {
//                    case R.id.navItem1:
//                        Intent showLocalImageIntent = new Intent(activity, GalleryListActivity.class);
//                        startActivity(showLocalImageIntent);
//
//                        break;
//                    case R.id.navItem2:
//                        Intent showMainIntent = new Intent(activity, MainActivity.class);
//                        startActivity(showMainIntent);
//
//                        break;
//                    case R.id.navItem3:
//                        Intent showSettingIntent = new Intent(activity, SettingsActivity.class);
//                        startActivity(showSettingIntent);
//                        break;
//                    case R.id.navItem4:
//
//                        break;
//                }
//                return false;
//            }
//        });


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

        Cursor cur = getContentResolver().query(images, albums, null, null, null);

       // final ArrayList<String> folders = new ArrayList<String>();

        /*
            set the folder path and folder names in shared preferences
         */
        if(cur != null) {
            if (cur.moveToFirst()) {
                String album;
                String folderPath;
                int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int path = cur.getColumnIndex(MediaStore.Images.Media.DATA);

                do {
                    // here store filename and filepath
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
        }







        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");


        collectionListLocal = new Select()
                .from(ImejiFolder.class)
                .execute();
        Log.v(LOG_TAG, "size: " + collectionListLocal.size() + "");


        /**
         * DO NOT DELETE
         * hide listView for now
         */
//        adapter = new FolderListAdapter(activity, collectionListLocal);
//
//
//        listView = (ListView) findViewById(R.id.folder_listView);
//
//
//
//        listView.setAdapter(adapter);
//
//
//
//        // Set OnItemClickListener so we can be notified on item clicks
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
//                ImejiFolder folder = (ImejiFolder) adapter.getItem(position);
//
//                Intent showItemsIntent = new Intent(activity, ItemsActivity.class);
//                showItemsIntent.putExtra(Intent.EXTRA_TEXT, folder.id);
//                showItemsIntent.putExtra("folderTitle", folder.getTitle());
//                startActivity(showItemsIntent);
//            }
//        });


                /*
                    file system
                 */
                UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
                mReceiver.setReceiver(this);
                Intent intent = new Intent(this, UploadService.class);
                intent.putExtra("receiver", mReceiver);
                this.startService(intent);


                Handler handler = new Handler();

            /*
                camera
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

        //deRegister NetStateObserver
        NetWorkStateReceiver.unRegisterNetStateObserver(this);

        new Delete().
                from(ImejiFolder.class)
                .execute();
        hidePDialog();
    }


    @Override
    public void onPostCreate(Bundle savedInstanceState) {
//        drawerToggle.syncState();
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
//        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    /**
     * DO NOT DELETE
     * navigation for drawerLayout, need it later
     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//

//        int id = item.getItemId();
//
//        if (id == R.id.action_settings) {
//            Intent showSettingIntent = new Intent(activity, SettingsActivity.class);
//            startActivity(showSettingIntent);
//
//            return true;
//        }
//        if (id == R.id.homeAsUp) {
//            drawerLayout.openDrawer(GravityCompat.START);
//            return true;
//        }
//
//        if (drawerToggle.onOptionsItemSelected(item)) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

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

                mPrefs = PreferenceManager.getDefaultSharedPreferences(this);

                SharedPreferences.Editor e = mPrefs.edit();
                e.putString("UploadStatus","true");
                e.commit();

                break;
            case 2:
                /* Handle the error */
                String error = resultData.getString(Intent.EXTRA_TEXT);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    // monitor network when Connect
    @Override
    public void OnConnect() {
        Toast.makeText(this, "network Connect...", Toast.LENGTH_LONG).show();

        UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        Intent intent = new Intent(this, UploadService.class);
        intent.putExtra("receiver", mReceiver);

        this.startService(intent);
    }

    // monitor network when DisConnect
    @Override
    public void OnDisConnect() {
        Toast.makeText(this, "network disconnect...", Toast.LENGTH_LONG).show();
    }

    //TODO: DELET TESTING CODE // FIXME: 12/17/15
    private void initDB(){
        if(isAdd){
            Log.v("initDB","isAdded");
        }else {

            isAdd = true;
            //init task
            String now = "12/17/15";
            String tomorrow = "12/18/15";
            Task task1 = new Task();
            task1.setTaskId("01");
            task1.setUserName("Ina");
            task1.setFinishTime(now);
            task1.setStartTime(now);
            task1.setUploadMode("AU");
            task1.save();

            //init image
            Image einImage = new Image();
            einImage.setImageId("001");
            einImage.setImageName("eins");
            einImage.setCreateTime(now);
            einImage.setTask(task1);
            einImage.save();

            Image zwiImage = new Image();
            zwiImage.setImageId("002");
            zwiImage.setImageName("zwi");
            zwiImage.setCreateTime(now);
            zwiImage.setTask(task1);
            zwiImage.save();


        }
    }

    /**
     * init Fragments
     */
    private void initInstances() {
        // Setup tabs
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        SectionsPagerAdapter tabAdapter= new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);


        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_local);
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_imeji);
        tabLayout.getTabAt(2).setCustomView(R.layout.tab_upload);
        tabLayout.getTabAt(3).setCustomView(R.layout.tab_user);

        //tab style change on page change
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            float scale = getResources().getDisplayMetrics().density;
            int selectedIndex = -1;

            // tab icon images
            int[] unselectedTabResource = {
                    R.drawable.user_inactive,
                    R.drawable.user_inactive,
                    R.drawable.user_inactive,
                    R.drawable.user_inactive
            };
            int[] selectedTabResource = {
                    R.drawable.user_active,
                    R.drawable.user_active,
                    R.drawable.user_active,
                    R.drawable.user_active
            };

            //tab Icon and Text id in layout files
            int[] backgroundIconId = {R.id.tabicon_local, R.id.tabicon_imeji, R.id.tabicon_upload, R.id.tabicon_user};

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                //if the tab is not the selected one, set its text and icon style as inactive

                if (0 != position && 0 != selectedIndex) {
                    tabLayout.findViewById(backgroundIconId[0]).setBackgroundResource(unselectedTabResource[0]);
                }
                if (1 != position && 1 != selectedIndex) {
                    tabLayout.findViewById(backgroundIconId[1]).setBackgroundResource(unselectedTabResource[1]);
                }
                if (2 != position && 2 != selectedIndex) {
                    tabLayout.findViewById(backgroundIconId[2]).setBackgroundResource(unselectedTabResource[2]);
                }
                if (3 != position && 3 != selectedIndex) {
                    tabLayout.findViewById(backgroundIconId[3]).setBackgroundResource(unselectedTabResource[3]);
                }

                //activate the selected tab icon and text
                tabLayout.setBackgroundResource(R.color.primary);
                //background icon
                ImageView imageView = (ImageView) tabLayout.findViewById(backgroundIconId[position]);
                imageView.setBackgroundResource(selectedTabResource[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }


        @Override
        public int getCount() {
                return 4;
        }

        @Override
        public Fragment getItem(int position) {

            android.support.v4.app.Fragment fragment;

            Log.v(LOG_TAG, position + "");
            //try with exception

            switch (position) {
                case 0:
                    fragment = new LocalFragment();
                    return fragment;

                case 1:
                    fragment = new ImejiFragment();
                    return fragment;
                case 2:
                    fragment = new UploadFragment();
                    return fragment;
                case 3:
                    fragment = new UserFragment();
                    return fragment;

                default:
                    return new LocalFragment();
            }

        }
    }

}
