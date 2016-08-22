package de.mpg.mpdl.labcam.Folder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import de.mpg.mpdl.labcam.Auth.LoginActivity;
import de.mpg.mpdl.labcam.AutoRun.ManualUploadService;
import de.mpg.mpdl.labcam.AutoRun.TaskUploadService;
import de.mpg.mpdl.labcam.ImejiFragment.ImejiFragment;
import de.mpg.mpdl.labcam.LocalFragment.LocalFragment;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.NetChangeManager.NetChangeObserver;
import de.mpg.mpdl.labcam.NetChangeManager.NetWorkStateReceiver;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import de.mpg.mpdl.labcam.Settings.RemoteCollectionSettingsActivity;
import de.mpg.mpdl.labcam.TaskManager.ActiveTaskActivity;
import de.mpg.mpdl.labcam.TaskManager.RecentProcessActivity;
import de.mpg.mpdl.labcam.Upload.UploadResultReceiver;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity implements UploadResultReceiver.Receiver,NetChangeObserver {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private Activity activity = this;

    private ProgressDialog pDialog;

    //drawer
    private String TAG = "drawer";
    android.support.v7.widget.Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    private List<Task> manualTasks = new ArrayList<>();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final int PICK_COLLECTION_REQUEST = 1997;
    // flag
    // no collection selected before
//    private boolean isFirstCollection = false;

    private String email;
    private String username;
    private String userId;
    private String apiKey;
    private String serverUrl;
    private SharedPreferences mPrefs;

    //current tab
    private int currentTab = 0;

    //create collection
    private List<ImejiFolder>folderList = null;
    //uri register
    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TASK_CODE = 2016;

    //UI
    static Switch autoUploadSwitch = null;
    static TextView chooseCollectionLabel = null;
    static TextView collectionNameTextView = null;
    static {
        matcher.addURI("de.mpg.mpdl.labcam", "tasks", TASK_CODE);
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

    //login with qr
    boolean isQRLogin = false;

    //
    private RelativeLayout chooseCollectionLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
        setContentView(R.layout.activity_main);
        Log.v("Main activity", "started");

        //user info
        mPrefs = this.getSharedPreferences("myPref", 0);
        email = mPrefs.getString("email", "");
//        username =  mPrefs.getString("username", "");
        username = mPrefs.getString("familyName","")+" "+mPrefs.getString("givenName","");
        userId = mPrefs.getString("userId","");
        apiKey = mPrefs.getString("apiKey","");
        serverUrl = mPrefs.getString("server","");
//        serverUrl = DeviceStatus.parseServerUrl(serverUrl);

        try{
            Bundle args = this.getIntent().getExtras();
            isQRLogin = args.getBoolean("isQRLogin", false);}   // get isQRLogin from extra
        catch (Exception e){}

        getLocalCamFolder();
        // register NetStateObserver
        NetWorkStateReceiver.registerNetStateObserver(this);

        //init drawer toggle
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.hello_world, R.string.hello_world);
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initInstances();

        //choose collection
        chooseCollection();

        checkRecent();

        setUserInfoText();

        initAutoSwitch();

        if(isQRLogin) { // login with qr
            setAutoUploadStatus(isQRLogin, true);
            Toast.makeText(activity,"Automatic upload is active",Toast.LENGTH_SHORT).show();
        }else { // normal login
            RetrofitClient.getGrantCollectionMessage(callback, apiKey);  // show alert if no collection available
        }

        setLogout();

        /**************************************************  check upload not finished task ***************************************************/

        List<Task> taskList = DeviceStatus.getUserTasks(userId);
        boolean isFinished = true;

        for(Task task:taskList){
            if(task.getFinishedItems()<task.getTotalItems()){
                isFinished = false;
            }
        }

        if(!isFinished) {

            new AlertDialog.Builder(this)
                    .setTitle("Welcome")
                    .setMessage("There are some uploading tasks not be compeleted last time, you can restart them in your Task Manager")
                    .setPositiveButton("Go into", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(activity, ActiveTaskActivity.class);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Ignore", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setIcon(R.drawable.error_alert)
                    .show();
        }else if(isFinished){
            //do nothing
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume(){
        super.onResume();
        SectionsPagerAdapter tabAdapter= new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabAdapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.setCurrentItem(currentTab);

        //set selected collection name
        collectionNameTextView = (TextView) findViewById(R.id.collection_name);
    }

    @Override
    public void onPause(){
        super.onPause();
        hidePDialog();
        // set current tab
        currentTab = viewPager.getCurrentItem();
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
        drawerToggle.syncState();
        super.onPostCreate(savedInstanceState);

    }

    // when rotate，not destroy activity
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);

    }

    private static final int REQUEST_CODE = 1;
    private Bitmap bitmap;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //open camera
        ImageView cameraImageView = (ImageView) findViewById(R.id.camera_icon);
        cameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

//        if (id == R.id.action_settings) {
//            Intent showSettingIntent = new Intent(activity, SettingsActivity.class);
//            startActivity(showSettingIntent);
//
//            return true;
//        }

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


    public void getLocalCamFolder(){

       String path_DCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
       Log.i("DCIM path_DCIM:", path_DCIM);

    }

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
//            super.onBackPressed();
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to quit the application", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 3000);
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
                folders.add(album);
                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }
    }

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
//        Toast.makeText(this, "network Connect...", Toast.LENGTH_LONG).show();
    }

    // monitor network when DisConnect
    @Override
    public void OnDisConnect() {
//        Toast.makeText(this, "network disconnect...", Toast.LENGTH_LONG).show();

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
//
//        if(isTaskFragment){
//            currentTab = 2;
//        viewPager.setCurrentItem(currentTab);
//        }

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_local);
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_imeji);
//        tabLayout.getTabAt(2).setCustomView(R.layout.tab_upload);

//        if(isTaskFragment){
//            TextView taskTextView = (TextView)tabLayout.findViewById(R.id.tabicon_upload);
//            taskTextView.setTextColor(getResources().getColor(R.color.primary));
//            TextView fotoTextView = (TextView)tabLayout.findViewById(R.id.tabicon_local);
//            fotoTextView.setTextColor(getResources().getColor(R.color.tabUnselect));
//        }

        //tab style change on page change
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            float scale = getResources().getDisplayMetrics().density;
            int selectedIndex = -1;


            //tab Icon and Text id in layout files
            int[] backgroundIconId = {R.id.tabicon_local, R.id.tabicon_imeji, R.id.tabicon_upload};

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //if the tab is not the selected one, set its text and icon style as inactive

                if (0 != position && 0 != selectedIndex) {
                    TextView iconText0 = (TextView) tabLayout.findViewById(backgroundIconId[0]);
                    iconText0.setTextColor(getResources().getColor(R.color.tabUnselect));
                }
                if (1 != position && 1 != selectedIndex) {
                    TextView iconText1 = (TextView) tabLayout.findViewById(backgroundIconId[1]);
                    iconText1.setTextColor(getResources().getColor(R.color.tabUnselect));
                }
//                if (2 != position && 2 != selectedIndex) {
//                    TextView iconText2 = (TextView) tabLayout.findViewById(backgroundIconId[2]);
//                    iconText2.setTextColor(getResources().getColor(R.color.tabUnselect));
//                }

                //background icon
                TextView iconText = (TextView) tabLayout.findViewById(backgroundIconId[position]);
                iconText.setTextColor(getResources().getColor(R.color.primary));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        //initUI
        autoUploadSwitch = (Switch) findViewById(R.id.switch_auto_upload);
        chooseCollectionLabel = (TextView) findViewById(R.id.tv_choose_collection);
        collectionNameTextView = (TextView) findViewById(R.id.collection_name);

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
                return 2;
        }

        @Override
        public Fragment getItem(int position) {

            android.support.v4.app.Fragment fragment;

            //try with exception
            switch (position) {
                case 0:
                    fragment = new LocalFragment();
                    return fragment;
                case 1:
                    fragment = new ImejiFragment();
                    return fragment;
                default:
                    return new LocalFragment();
            }

        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    //choose collection
    private void chooseCollection(){

        // whole layout onclick
        chooseCollectionLayout = (RelativeLayout) findViewById(R.id.layout_choose_collection);
        chooseCollectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });
    }

    private void checkRecent(){
        RelativeLayout chooseCollectionLayout = (RelativeLayout) findViewById(R.id.layout_recent_processes);
        chooseCollectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recentIntent = new Intent(context, RecentProcessActivity.class);
                startActivity(recentIntent);
            }
        });
    }

    /**
     * set switch on
     * set collection
     * toast
     */
    private void setAutoUploadStatus(boolean isQRLogin, boolean isAUOn){
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();   // get old settings
        if(settings==null){
            settings = new Settings();  // no setting
        }

        if(isAUOn) {  // set AUOn
            // login set true
            settings.setUserId(userId);
            settings.setIsAutoUpload(true);
            settings.save();
            autoUploadSwitch.setChecked(settings.isAutoUpload());

            // if auto is on in settings, enable choose collection
            chooseCollectionLayout.setEnabled(true);
            chooseCollectionLabel.setTextColor(getResources().getColor(R.color.dark_text));
            collectionNameTextView.setTextColor(getResources().getColor(R.color.dark_text));
        }else { // set AUOff
            settings.setUserId(userId);
            settings.setIsAutoUpload(false);
            settings.save();
            autoUploadSwitch.setChecked(false);

            chooseCollectionLayout.setEnabled(false);
            chooseCollectionLabel.setTextColor(getResources().getColor(R.color.grayDivider));
            collectionNameTextView.setTextColor(getResources().getColor(R.color.grayDivider));
        }
        // auto open choose collection

//        Task auTask = DeviceStatus.getAuTask(userId, serverUrl);

//        if(auTask==null){
//            // popup
//            Toast.makeText(activity,"Automatic upload is active\n please set a collection",Toast.LENGTH_SHORT).show();
//        }else
//            Toast.makeText(activity,"Automatic upload is active",Toast.LENGTH_SHORT).show();
    }

    private void initAutoSwitch(){
        autoUploadSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!autoUploadSwitch.isChecked()){
                    //on to off situation
                    return;
                }
                //off to on
                Task auTask = DeviceStatus.getAuTask(userId,serverUrl);

                if(auTask==null){
                    // popup
                    Toast.makeText(activity,"Automatic upload is active\n please set a collection",Toast.LENGTH_SHORT).show();
                    Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                    startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
                }else
                    Toast.makeText(activity,"Automatic upload is active",Toast.LENGTH_SHORT).show();
            }
        });

        autoUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
                //init settings
                if (settings == null) {
                    settings = new Settings();
                }

                if (compoundButton.isChecked()) {
                    Log.e(TAG, "userId" + userId);
                    settings.setUserId(userId);
                    settings.setIsAutoUpload(true);
                    settings.save();
                    chooseCollectionLayout.setEnabled(true);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.dark_text));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.dark_text));

                } else {
                    Log.e(TAG, "userId" + userId);
                    settings.setUserId(userId);
                    settings.setIsAutoUpload(false);
                    settings.save();
                    chooseCollectionLayout.setEnabled(false);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.grayDivider));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.grayDivider));
                }
                Log.e(LOG_TAG, settings.isAutoUpload() + "");
            }
        });
    }

    //logout
    static Task autoTask;
    private void setLogout(){
        ImageView logout = (ImageView) findViewById(R.id.tv_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<Task> taskList = DeviceStatus.getUserTasks(userId);
                boolean isFinished = true;

                for(Task task:taskList){

                   if(task.getFinishedItems()<task.getTotalItems()){
                       isFinished = false;

                       if(task.getUploadMode().equalsIgnoreCase("AU")){
                           autoTask = task;
                       }else if(task.getUploadMode().equalsIgnoreCase("MU")){
                            manualTasks.add(task);
                       }
                   }
                }
                if(!isFinished){
                    new AlertDialog.Builder(context)
                            .setTitle("Logout")
                            .setMessage("There are still pictures waiting for uploading, are you sure you want to logout?")
                            .setPositiveButton("LOGOUT", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // LOGOUT

                                    //for auto task
                                    if(autoTask!=null) {
                                        autoTask.setUploadMode("MU");
                                        autoTask.setState(String.valueOf(DeviceStatus.state.STOPPED));
                                        autoTask.save();

                                        //create auto task (default)
                                        Task task = new Task();

                                        String uniqueID = UUID.randomUUID().toString();
                                        task.setTaskId(uniqueID);
                                        task.setUploadMode("AU");
                                        task.setCollectionId(autoTask.getCollectionId());
                                        task.setCollectionName(autoTask.getCollectionName());
                                        task.setState(String.valueOf(DeviceStatus.state.WAITING));
                                        task.setUserName(username);
                                        task.setUserId(userId);
                                        task.setTotalItems(0);
                                        task.setFinishedItems(0);
                                        task.setSeverName(serverUrl);

                                        Long now = new Date().getTime();
                                        task.setStartDate(String.valueOf(now));

                                        task.save();
                                    }
                                    // for manual task
                                    if(manualTasks.size()>0){
                                        for(Task task:manualTasks){
                                            task.setState(String.valueOf(DeviceStatus.state.STOPPED));
                                            task.save();
                                        }
                                    }


                                    // stop old upload process
                                    Intent AUIntent = new Intent(context, TaskUploadService.class);
                                    stopService(AUIntent);

                                    Intent MUIntent = new Intent(context, ManualUploadService.class);
                                    stopService(MUIntent);

                                    //delete sharedPreference(move to logout callback after backend implementation)
                                    SharedPreferences.Editor mEditor = mPrefs.edit();
                                    mEditor.remove("apiKey").commit();
                                    mEditor.remove("userId").commit();
                                    mEditor.remove("username").commit();
                                    mEditor.remove("isAlbum").commit();
                                    mEditor.remove("server").commit();
                                    Intent logoutIntent = new Intent(context, LoginActivity.class);
                                    startActivity(logoutIntent);

                                    finish();
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do
                                }
                            })
                            .setIcon(R.drawable.error_alert)
                            .show();
                }else {

                    // stop old upload process
                    Intent AUIntent = new Intent(context, TaskUploadService.class);
                    stopService(AUIntent);

                    Intent MUIntent = new Intent(context, ManualUploadService.class);
                    stopService(MUIntent);

                //delete sharedPreference(move to logout callback after backend implementation)
                SharedPreferences.Editor mEditor = mPrefs.edit();
                    mEditor.remove("apiKey").commit();
                    mEditor.remove("userId").commit();
                    mEditor.remove("username").commit();
                    mEditor.remove("isAlbum").commit();

                Intent logoutIntent = new Intent(context, LoginActivity.class);
                startActivity(logoutIntent);

                finish();
                }
            }
        });

    }

    //set user info textView(name email)
    private void setUserInfoText(){
        TextView nameTextView = (TextView) findViewById(R.id.tv_username);
        TextView emailTextView = (TextView) findViewById(R.id.tv_user_email);
        TextView serverTextView = (TextView) findViewById(R.id.tv_server_url);
        nameTextView.setText(username);
        emailTextView.setText(email);
        if(serverUrl.length()<25){
            serverTextView.setText(serverUrl);
        }else
            serverTextView.setText(serverUrl.substring(0,25)+"...");
    }


    //camera
    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {

        PackageManager packman = getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String pack = intent.resolveActivity(packman).getPackageName();
        String cls = intent.resolveActivity(packman).getClassName();

        Log.e("~~~", pack);
        Log.e("~~~", cls);

//        if(pack.equalsIgnoreCase("com.sec.android.app.camera")){
//            Toast.makeText(context,"this device not support open camera here",Toast.LENGTH_SHORT).show();
//        }else {

            Intent mIntent = new Intent();
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName(pack,cls);
            mIntent.setComponent(comp);
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mIntent.setAction("android.intent.action.View");

            startActivity(mIntent);
//        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_COLLECTION_REQUEST) {
            if(resultCode == RemoteCollectionSettingsActivity.INTENT_NONE) {
                // get isNone, collectionId is not valid anymore
                Log.e(TAG,data.getBooleanExtra("isNone",false)+"");
                if(data.getBooleanExtra("isNone",false)){
                    // set collection name none
                    if(collectionNameTextView!=null){
                        collectionNameTextView.setText("none");
                    }
                }
            }else if(resultCode == RESULT_OK){

                // prepare settings
                Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
                if(settings==null){
                    settings = new Settings();
                }

                Task lastAUTask = new Select().from(Task.class).where("userId = ?",userId).where("uploadMode = ?","AU").executeSingle();

                if(lastAUTask!= null){
                    Log.e("blabla", lastAUTask.getCollectionName());
                    collectionNameTextView.setText(lastAUTask.getCollectionName());
                    autoUploadSwitch.setChecked(true);

                    settings.setUserId(userId);
                    settings.setIsAutoUpload(true);
                    settings.save();

                    // UI enable click
                    chooseCollectionLayout.setEnabled(true);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.dark_text));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.dark_text));
                }
                else{
                    collectionNameTextView.setText("none");
                    autoUploadSwitch.setChecked(false);

                    settings.setUserId(userId);
                    settings.setIsAutoUpload(false);
                    settings.save();

                    // UI disable click
                    chooseCollectionLayout.setEnabled(false);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.grayDivider));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.grayDivider));
                }
            }
        }

    }

    Callback<ImejiFolder> createCollection_callback = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.e(LOG_TAG, "createCollection_callback success");

            DeviceStatus.deleteFinishedAUTasks(userId);             //delete all AU Task if finished

            Task task = new Task();                                 //new a AU Task
            String uniqueID = UUID.randomUUID().toString();
            task.setTaskId(uniqueID);
            task.setUploadMode("AU");
            task.setCollectionId(imejiFolder.id);
            Log.e(LOG_TAG,"collectionId: "+imejiFolder.id);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(username);
            task.setUserId(userId);
            task.setSeverName(serverUrl);
            task.setTotalItems(0);
            task.setFinishedItems(0);

            Long now = new Date().getTime();
            Log.v("now", now + "");
            task.setStartDate(String.valueOf(now));
            task.setCollectionName(imejiFolder.getTitle());
            Log.e(LOG_TAG, "collectionName: " + imejiFolder.getTitle());
            task.save();

            //set selected collection name text
            collectionNameTextView.setText(imejiFolder.getTitle());

            //switch on
            autoUploadSwitch.setChecked(true);

            // go to fragment
            SectionsPagerAdapter tabAdapter= new SectionsPagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(tabAdapter);
//            currentTab = 0;
//            viewPager.setCurrentItem(currentTab);

            pDialog.dismiss();
            Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();   // get old settings

            if(settings!=null && settings.isAutoUpload())  // history AU is on
            {
                setAutoUploadStatus(false,true);
                Toast.makeText(activity,"Automatic upload is active",Toast.LENGTH_SHORT).show();
            }else {                                        // history AU is off or not set
                setAutoUploadStatus(false,false);
            }

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, error.getMessage());
        }
    };

    Callback<CollectionMessage> callback = new Callback<CollectionMessage>() {
        @Override
        public void success(CollectionMessage collectionMessage, Response response) {

            List<ImejiFolder> folderList = new ArrayList<>();
            folderList = collectionMessage.getResults();

            if(folderList.size()==0){
                // first delete AutoTask
                new Delete().from(Task.class).where("uploadMode = ?", "AU").execute();
                collectionNameTextView.setText("none");
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
                                // AU:off col:null
                                setAutoUploadStatus(false,false);

                            }
                        })
                        .setIcon(R.drawable.error_alert)
                        .show();
            }
            else if(folderList.size()==1){

                DeviceStatus.deleteFinishedAUTasks(userId);             //delete all AU Task if finished

                Task task = new Task();                                 //new a AU Task
                String uniqueID = UUID.randomUUID().toString();
                task.setTaskId(uniqueID);
                task.setUploadMode("AU");
                task.setCollectionId(folderList.get(0).id);
                Log.e(LOG_TAG,"collectionId: "+folderList.get(0).id);
                task.setState(String.valueOf(DeviceStatus.state.WAITING));
                task.setUserName(username);
                task.setUserId(userId);
                task.setSeverName(serverUrl);
                task.setTotalItems(0);
                task.setFinishedItems(0);

                Long now = new Date().getTime();
                Log.v("now", now + "");
                task.setStartDate(String.valueOf(now));
                task.setCollectionName(folderList.get(0).getTitle());
                Log.e(LOG_TAG, "collectionName: " + folderList.get(0).getTitle());
                task.save();

                //set selected collection name text
                if(folderList.get(0).getTitle()!=null) {
                    collectionNameTextView.setText(folderList.get(0).getTitle());
                }
                //switch on
                autoUploadSwitch.setChecked(true);

                Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();   // get old settings

                if(settings!=null && settings.isAutoUpload())  // history AU is on
                {
                    setAutoUploadStatus(false,true);
                    Toast.makeText(activity,"Automatic upload is active",Toast.LENGTH_SHORT).show();
                }else {                                        // history AU is off or not set
                    setAutoUploadStatus(false,false);
                }
            }
            else {  // Col > 1
                Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();   // get old settings

                //set collection name
                collectionNameTextView = (TextView) findViewById(R.id.collection_name);
                if(settings!=null && settings.isAutoUpload())  // history AU is on
                {

                    //col wasValue
                    Task auTask = DeviceStatus.getAuTask(userId,serverUrl);
                    if(auTask!=null) {   // col wasValue valid
                        collectionNameTextView.setText(auTask.getCollectionName());     // collection name from autoTask
                        setAutoUploadStatus(false,true);
                        Toast.makeText(activity,"Automatic upload is active",Toast.LENGTH_SHORT).show();

                    } else { // col wasValue invalid
                        // dialog lead new user to choose collection
                        new AlertDialog.Builder(context)
                                .setTitle("collection not set yet")
                                .setMessage("Automatic upload is active, please set a Collection")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // go to set collection
                                        Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                                        startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        setAutoUploadStatus(false,false); // AU off
                                        collectionNameTextView.setText("none");
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    }
                }else {                                        // history AU is off or not set
                    Task auTask = DeviceStatus.getAuTask(userId,serverUrl);
                    if(auTask!=null) {   // col wasValue valid
                        collectionNameTextView.setText(auTask.getCollectionName());     // collection name from autoTask
                    }
                        setAutoUploadStatus(false,false);
                }
            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
        }
    };
}
