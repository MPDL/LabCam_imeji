package example.com.mpdlcamera.Folder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.ImejiFragment.ImejiFragment;
import example.com.mpdlcamera.LocalFragment.LocalFragment;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.LocalUser;
import example.com.mpdlcamera.NetChangeManager.NetChangeObserver;
import example.com.mpdlcamera.NetChangeManager.NetWorkStateReceiver;
import example.com.mpdlcamera.R;

import example.com.mpdlcamera.Settings.RemoteCollectionSettingsActivity;
import example.com.mpdlcamera.TaskManager.TaskFragment;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.UploadActivity.UploadFragment;
import example.com.mpdlcamera.Utils.DeviceStatus;

/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity implements UploadResultReceiver.Receiver,NetChangeObserver {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;

    //drawer
    private String TAG = "drawer";
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    static final int PICK_COLLECTION_REQUEST = 1997;
    private RadioGroup radioGroup;

    private String email;
    private SharedPreferences mPrefs;

    //TESTING DB
    private boolean isAdd = false;

    //uri register
    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TASK_CODE = 2016;

    static {
        matcher.addURI("example.com.mpdlcamera", "tasks", TASK_CODE);
    }


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

    //activity
    private Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
        setContentView(R.layout.activity_main);
        Log.v("Main activity", "started");

        //
        getLocalCamFolder();
        // register NetStateObserver
        NetWorkStateReceiver.registerNetStateObserver(this);


        initInstances();
                /*
                    file system
                 */
//                UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
//                mReceiver.setReceiver(this);
//                Intent intent = new Intent(this, UploadService.class);
//                intent.putExtra("receiver", mReceiver);
//                this.startService(intent);


                Handler handler = new Handler();

            /*
                camera
             */
//                NewFileObserver newFileObserver = new NewFileObserver(handler,this);
//                getApplicationContext().getContentResolver().registerContentObserver(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,false, newFileObserver);

        //create drawer view
        mPrefs = this.getSharedPreferences("myPref", 0);
        email = mPrefs.getString("email", "");

        //init radioButton group
        initRadioButtonGroup();

        //choose collection
        chooseCollection();

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();

        //set selected collection name
        TextView collectionNameTextView = (TextView) findViewById(R.id.collection_name);

        if(!DeviceStatus.is_newUser(email)){
            LocalUser user = new Select().from(LocalUser.class).where("email = ?", email).executeSingle();
            collectionNameTextView.setText(user.getCollectionName());
            List<LocalUser> userList = new Select().from(LocalUser.class).where("email = ?", email).execute();
            Log.i(TAG,userList.size()+"");
        }else {
            collectionNameTextView.setText("unset");
        }
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

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }


    public void getLocalCamFolder(){

       String path_DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
       Log.i("DCIM path_DCIM:",path_DCIM);


    }

    public void getLocalFolders(){
        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        for(int i = 0; i<albums.length ;i++){
            Log.i("albums",  albums[i]);
        }
        Log.i("Images",  images.toString());

        final ArrayList<String> folders = new ArrayList<String>();

        Cursor cur = getContentResolver().query(images, albums, null, null, null);

        if (cur.moveToFirst()) {
            String album;
            String filePath;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int path = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                album = cur.getString(albumLocation);
                Log.i("album",album);
                filePath = cur.getString(path);
                Log.i("filePath",filePath);
                File file = new File(filePath);
                String directory = file.getParent();
                Log.i("filePath/directory", directory);
//                SharedPreferences.Editor editor = preferencesFiles.edit();
//                editor.putString(album, directory);
//                editor.commit();
                folders.add(album);
                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }
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

//        UploadResultReceiver mReceiver = new UploadResultReceiver(new Handler());
//        mReceiver.setReceiver(this);
//        Intent intent = new Intent(this, UploadService.class);
//        intent.putExtra("receiver", mReceiver);
//
//        this.startService(intent);
    }

    // monitor network when DisConnect
    @Override
    public void OnDisConnect() {
        Toast.makeText(this, "network disconnect...", Toast.LENGTH_LONG).show();
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

        //tab style change on page change
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            float scale = getResources().getDisplayMetrics().density;
            int selectedIndex = -1;

            // tab icon images
            int[] unselectedTabResource = {
                    R.drawable.localfolder_inactive,
                    R.drawable.imeji_logo_inactive,
                    R.drawable.settings_inactive,
            };
            int[] selectedTabResource = {
                    R.drawable.localfolder_active,
                    R.drawable.imeji_logo_active,
                    R.drawable.settings_active,
            };

            //tab Icon and Text id in layout files
            int[] backgroundIconId = {R.id.tabicon_local, R.id.tabicon_imeji, R.id.tabicon_upload};

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
                return 3;
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
                    fragment = new TaskFragment();
                    return fragment;
                default:
                    return new LocalFragment();
            }

        }
    }

    //drawer layout(settings)
    public void initRadioButtonGroup(){
        radioGroup = (RadioGroup) findViewById(R.id.radio_group);
//        radioGroup.clearCheck();
    }

    public void chooseCollection(){
        TextView chooseCollectionTextView = (TextView) findViewById(R.id.tv_choose_collection);
        chooseCollectionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });
    }

}
