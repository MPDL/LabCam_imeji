package example.com.mpdlcamera.Folder;

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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import example.com.mpdlcamera.Auth.LoginActivity;
import example.com.mpdlcamera.AutoRun.ManualUploadService;
import example.com.mpdlcamera.AutoRun.TaskUploadService;
import example.com.mpdlcamera.ImejiFragment.ImejiFragment;
import example.com.mpdlcamera.LocalFragment.LocalFragment;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Settings;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.Model.MessageModel.CollectionMessage;
import example.com.mpdlcamera.NetChangeManager.NetChangeObserver;
import example.com.mpdlcamera.NetChangeManager.NetWorkStateReceiver;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.Settings.RemoteCollectionSettingsActivity;
import example.com.mpdlcamera.TaskManager.TaskFragment;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity implements UploadResultReceiver.Receiver,NetChangeObserver {

    private final String LOG_TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;

    //drawer
    private String TAG = "drawer";
    android.support.v7.widget.Toolbar toolbar;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle drawerToggle;

    private List<Task> manualTasks = new ArrayList<>();

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    static final int PICK_COLLECTION_REQUEST = 1997;

    private String email;
    private String username;
    private String userId;
    private String apiKey;
    private SharedPreferences mPrefs;

    //current tab
    private int currentTab = 0;

    //create collection
    private List<ImejiFolder>folderList = null;
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

    //
    private boolean isTaskFragment = false;

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

        try{
        Bundle args = this.getIntent().getExtras();
        isTaskFragment= args.getBoolean("isTaskFragment", false);}
        catch (Exception e){
//            Log.v(LOG_TAG,e.getMessage());
        }

        /** show alert if no collection available **/
        RetrofitClient.getGrantCollectionMessage(callback, apiKey);
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


        Log.e(LOG_TAG,apiKey);
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


        //init radioButton group

        //choose collection
        chooseCollection();
        setUserInfoText();
        setAutoUpload();
        setLogout();

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
        viewPager.setCurrentItem(currentTab);

        //set selected collection name
        TextView collectionNameTextView = (TextView) findViewById(R.id.collection_name);

            Task lastAUTask = new Select().from(Task.class).where("userId = ?",userId).where("uploadMode = ?","AU").executeSingle();
            if(lastAUTask!= null){
            collectionNameTextView.setText(lastAUTask.getCollectionName());}
            else{
                collectionNameTextView.setText("none");
            }
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

        if(isTaskFragment){
            currentTab = 2;
        viewPager.setCurrentItem(currentTab);
        }

        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setCustomView(R.layout.tab_local);
        tabLayout.getTabAt(1).setCustomView(R.layout.tab_imeji);
        tabLayout.getTabAt(2).setCustomView(R.layout.tab_upload);

        if(isTaskFragment){
            TextView taskTextView = (TextView)tabLayout.findViewById(R.id.tabicon_upload);
            taskTextView.setTextColor(getResources().getColor(R.color.primary));
            TextView fotoTextView = (TextView)tabLayout.findViewById(R.id.tabicon_local);
            fotoTextView.setTextColor(getResources().getColor(R.color.tabUnselect));
        }

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
                if (2 != position && 2 != selectedIndex) {
                    TextView iconText2 = (TextView) tabLayout.findViewById(backgroundIconId[2]);
                    iconText2.setTextColor(getResources().getColor(R.color.tabUnselect));
                }

                //background icon
                TextView iconText = (TextView) tabLayout.findViewById(backgroundIconId[position]);
                iconText.setTextColor(getResources().getColor(R.color.primary));
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

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    //choose collection
    private void chooseCollection(){
        TextView chooseCollectionTextView = (TextView) findViewById(R.id.tv_choose_collection);
        chooseCollectionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });

        TextView choosedCollectionTextView = (TextView) findViewById(R.id.collection_name);
        choosedCollectionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(context, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });
    }

    //auto upload switch
    private void setAutoUpload(){
        Switch autoUploadSwitch = (Switch) findViewById(R.id.switch_auto_upload);
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
        if(settings!=null){
            autoUploadSwitch.setChecked(settings.isAutoUpload());
        }

        autoUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();
                TextView chooseCollectionLabel = (TextView) findViewById(R.id.tv_choose_collection);
                TextView collectionNameTextView = (TextView) findViewById(R.id.collection_name);
                //init settings
                if (settings == null) {
                    settings = new Settings();
                }

                if (compoundButton.isChecked()) {
                    Log.e(TAG, "userId" + userId);
                    settings.setUserId(userId);
                    settings.setIsAutoUpload(true);
                    settings.save();
                    chooseCollectionLabel.setEnabled(true);
                    collectionNameTextView.setEnabled(true);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.dark_text));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.dark_text));

                } else {
                    Log.e(TAG, "userId" + userId);
                    settings.setUserId(userId);
                    settings.setIsAutoUpload(false);
                    settings.save();
                    chooseCollectionLabel.setEnabled(false);
                    collectionNameTextView.setEnabled(false);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.grayDivider));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.grayDivider));
                }
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

                                    Intent logoutIntent = new Intent(context, LoginActivity.class);
                                    startActivity(logoutIntent);
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

                Intent logoutIntent = new Intent(context, LoginActivity.class);
                startActivity(logoutIntent);
                }
            }
        });

    }

    //set user info textView(name email)
    private void setUserInfoText(){
        TextView nameTextView = (TextView) findViewById(R.id.tv_username);
        TextView emailTextView = (TextView) findViewById(R.id.tv_user_email);
        nameTextView.setText(username);
        emailTextView.setText(email);
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

        if(pack.equalsIgnoreCase("com.sec.android.app.camera")){
            Toast.makeText(context,"this device not support open camera here",Toast.LENGTH_SHORT).show();
        }else {

            Intent mIntent = new Intent();
            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName(pack,cls);
            mIntent.setComponent(comp);
            mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mIntent.setAction("android.intent.action.View");

            startActivity(mIntent);
        }

    }

    Callback<ImejiFolder> createCollection_callback = new Callback<ImejiFolder>() {
        @Override
        public void success(ImejiFolder imejiFolder, Response response) {
            Log.v(LOG_TAG, "createCollection_callback success");
//            imejiFolder.setImejiId(imejiFolder.id);
//            collectionListLocal.add(imejiFolder);
//            adapter.notifyDataSetChanged();

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
                                // continue with delete
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

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get list failed");
            Log.v(LOG_TAG, error.toString());
        }
    };


    //get latest task
    public static Task getTask() {
        return new Select()
                .from(Task.class)
                .orderBy("startDate DESC")
                .executeSingle();
    }

    //get latest image
    public static Image getImage() {
        return new Select()
                .from(Image.class)
                .orderBy("createTime DESC")
                .executeSingle();
    }
}
