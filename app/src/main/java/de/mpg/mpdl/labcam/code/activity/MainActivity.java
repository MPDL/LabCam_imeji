package de.mpg.mpdl.labcam.code.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
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
import com.google.gson.JsonObject;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import de.mpg.mpdl.labcam.Model.ImejiFolder;
import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.Model.MessageModel.CollectionMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseActivity;
import de.mpg.mpdl.labcam.code.base.BaseMvpActivity;
import de.mpg.mpdl.labcam.code.common.adapter.TitleFragmentPagerAdapter;
import de.mpg.mpdl.labcam.code.common.fragment.CollectionViewNewFragment;
import de.mpg.mpdl.labcam.code.common.fragment.LocalFragment;
import de.mpg.mpdl.labcam.code.common.observer.NetChangeObserver;
import de.mpg.mpdl.labcam.code.common.observer.NetWorkStateReceiver;
import de.mpg.mpdl.labcam.code.common.service.ManualUploadService;
import de.mpg.mpdl.labcam.code.common.service.MediaContentJobService;
import de.mpg.mpdl.labcam.code.common.service.TaskUploadService;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.data.model.ImejiFolderModel;
import de.mpg.mpdl.labcam.code.injection.component.DaggerCollectionComponent;
import de.mpg.mpdl.labcam.code.injection.module.CollectionMessageModule;
import de.mpg.mpdl.labcam.code.mvp.presenter.MainPresenter;
import de.mpg.mpdl.labcam.code.mvp.view.MainView;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;

/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends BaseMvpActivity<MainPresenter> implements MainView, NetChangeObserver {

    @BindView(R.id.layout_choose_collection)
    RelativeLayout chooseCollectionLayout;
    @BindView(R.id.switch_auto_upload)
    Switch autoUploadSwitch;
    @BindView(R.id.switch_ocr)
    Switch ocrSwitch;
    @BindView(R.id.tv_choose_collection)
    TextView chooseCollectionLabel;
    @BindView(R.id.collection_name)
    TextView collectionNameTextView;
    @BindView(R.id.camera_icon)
    ImageView cameraImageView;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;  //drawer
    ActionBarDrawerToggle drawerToggle;

    public static final int PICK_COLLECTION_REQUEST = 1997;
    private static final int TASK_CODE = 2016;
    public static final int MY_BACKGROUND_JOB = 0;

    private String email;
    private String username;
    private String userId;
    private String serverUrl;
    boolean isQRLogin = false;  //login with qr
    boolean isLoginCall = true;  //from onCreate callback
    boolean isDestroyByCamera = false;   //Camera destroy activity

    private BaseActivity activity = this;
    private ProgressDialog pDialog;
    private List<Task> manualTasks = new ArrayList<>();
    static Task autoTask;

    //uri register
    private static UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        matcher.addURI("de.mpg.mpdl.labcam", "tasks", TASK_CODE);
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // Start service and provide it a way to communicate with this class.
            Intent startServiceIntent = new Intent(this, MediaContentJobService.class);
            startService(startServiceIntent);
        }
    }

    @Override
    protected void onStop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // A service can be "started" and/or "bound". In this case, it's "started" by this Activity
            // and "bound" to the JobScheduler (also called "Scheduled" by the JobScheduler). This call
            // to stopService() won't prevent scheduled jobs to be processed. However, failing
            // to call stopService() would keep it alive indefinitely.
            stopService(new Intent(this, MediaContentJobService.class));
        }
        super.onStop();
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
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        hidePDialog();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        NetWorkStateReceiver.unRegisterNetStateObserver(this); //deRegister NetStateObserver

        new Delete().from(ImejiFolder.class).execute();
        hidePDialog();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        drawerToggle.syncState();
        super.onPostCreate(savedInstanceState);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {  // when rotate，not destroy activity
        drawerToggle.onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        //open camera
        cameraImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
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

    boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
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

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("isDestroyByCamera")) {
                isDestroyByCamera = savedInstanceState.getBoolean("isDestroyByCamera");
            }
        }

        //user info
        email = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.EMAIL, "");
        username = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.FAMILY_NAME, "")
                +" "+ PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.GIVEN_NAME, "");
        userId = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverUrl = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        Bundle args = this.getIntent().getExtras();         // get isQRLogin from extra
        try {
            isQRLogin = args.getBoolean("isQRLogin", false);
        } catch(NullPointerException e) {
            e.printStackTrace();
        }

        // register NetStateObserver
        NetWorkStateReceiver.registerNetStateObserver(this);

        //init drawer toggle
        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(MainActivity.this, drawerLayout, R.string.hello_world, R.string.hello_world);
        drawerLayout.setDrawerListener(drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initInstances();

        chooseCollection(); //choose collection

        checkRecent();

        checkRecentNote();

        checkRecentVoice();

        setUserInfoText();    // exception in

        initAutoSwitch();

        initOcrSwitch();

        if(isQRLogin) { // login with qr
            setAutoUploadStatus(isQRLogin, true);
        }else { // normal login
            isLoginCall = true;
            updateFolder();  // show alert if no collection available
        }

        setLogout();

        /**************************************************  check upload not finished task ***************************************************/

        List<Task> activeTaskList = DBConnector.getActiveTasks(userId, serverUrl);
        boolean isFinished = true;

        if(activeTaskList.size()>0){
            for (Task task : activeTaskList) {
                if(task.getUploadMode().equalsIgnoreCase("AU") && task.getTotalItems() == 0){
                    isFinished = true;
                }else {
                    isFinished = false;
                }
            }
        }

        if(!isDestroyByCamera && !isFinished) {

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

    @Override
    public void onSaveInstanceState(Bundle outState) {

        outState.putBoolean("isDestroyByCamera", true);
        super.onSaveInstanceState(outState);
    }

    private void initInstances() {

        List<Fragment> fragments = new ArrayList<Fragment>(2);
        fragments.add(new LocalFragment());
        fragments.add(new CollectionViewNewFragment());

        TitleFragmentPagerAdapter adapter = new TitleFragmentPagerAdapter(getSupportFragmentManager(), fragments, this);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(adapter.getCount());
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < adapter.getCount(); i++) {
            tabLayout.getTabAt(i).setCustomView(adapter.getTabView(i));
        }
        setCurrentItem(0);
    }

    public void setCurrentItem(int index) {
        viewPager.setCurrentItem(index);
    }

    //choose collection
    private void chooseCollection(){

        // whole layout onclick
        chooseCollectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent settingsIntent = new Intent(activity, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
            }
        });
        Task task = DBConnector.getAuTask(userId,serverUrl);
        if(task!=null && task.getCollectionName()!=null){
            collectionNameTextView.setText(task.getCollectionName());
        }
    }

    private void checkRecent(){
        RelativeLayout chooseCollectionLayout = (RelativeLayout) findViewById(R.id.layout_recent_processes);
        chooseCollectionLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recentIntent = new Intent(activity, RecentProcessActivity.class);
                startActivity(recentIntent);
            }
        });
    }

    private void checkRecentNote(){
        RelativeLayout chooseRecentNoteLayout = (RelativeLayout) findViewById(R.id.layout_recent_text_notes);
        chooseRecentNoteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recentNoteIntent = new Intent(activity, RecentNoteActivity.class);
                startActivity(recentNoteIntent);
            }
        });
    }

    private void checkRecentVoice(){
        RelativeLayout chooseRecentVoiceLayout = (RelativeLayout) findViewById(R.id.layout_recent_voice_notes);
        chooseRecentVoiceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent recentVoiceIntent = new Intent(activity, RecentVoiceActivity.class);
                startActivity(recentVoiceIntent);
            }
        });
    }

    private void setAutoUploadStatus(boolean isQRLogin, boolean isAUOn){
        Settings settings = DBConnector.getSettingsByUserId(userId);
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
            autoUploadSwitch.setChecked(settings.isAutoUpload());

            chooseCollectionLayout.setEnabled(false);
            chooseCollectionLabel.setTextColor(getResources().getColor(R.color.grayDivider));
            collectionNameTextView.setTextColor(getResources().getColor(R.color.grayDivider));
        }
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
                updateFolder();
                isLoginCall = true;

            }
        });


        autoUploadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Settings settings = DBConnector.getSettingsByUserId(userId);
                //init settings
                if (settings == null) {
                    settings = new Settings();
                }

                if (compoundButton.isChecked()) {
                    settings.setUserId(userId);
                    settings.setIsAutoUpload(true);
                    settings.save();
                    chooseCollectionLayout.setEnabled(true);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.dark_text));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.dark_text));

                } else {
                    settings.setUserId(userId);
                    settings.setIsAutoUpload(false);
                    settings.save();
                    chooseCollectionLayout.setEnabled(false);
                    chooseCollectionLabel.setTextColor(getResources().getColor(R.color.grayDivider));
                    collectionNameTextView.setTextColor(getResources().getColor(R.color.grayDivider));
                }

                // pop up to display switch on/off the automatic upload option
                if(compoundButton.isChecked()) {
                    Toast.makeText(activity,"Automatic upload is active!",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(activity,"Automatic upload is inactive!",Toast.LENGTH_SHORT).show();
                }
            }


        });

        Settings settings = DBConnector.getSettingsByUserId(userId);  // get old settings
        if(settings!=null && settings.isAutoUpload()){
            Toast.makeText(activity,"Automatic upload is active!",Toast.LENGTH_SHORT).show();
        }
    }

    private void initOcrSwitch(){
        ocrSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(buttonView.isChecked()){
                    PreferenceUtil.setBoolean(activity, Constants.SHARED_PREFERENCES, Constants.OCR_IS_ON, true);
                }else {
                    PreferenceUtil.setBoolean(activity, Constants.SHARED_PREFERENCES, Constants.OCR_IS_ON, false);
                }
            }
        });
    }

    private void updateFolder(){
        String q = "role=edit";
        mPresenter.getGrantedCollectionMessage(q, activity);
    }

    //logout
    private void setLogout(){
        ImageView logout = (ImageView) findViewById(R.id.tv_logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<Task> taskList = DBConnector.getUserTasks(userId, serverUrl);
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
                    new AlertDialog.Builder(activity)
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
                                        task.setUploadMode("AU");
                                        task.setCollectionId(autoTask.getCollectionId());
                                        task.setCollectionName(autoTask.getCollectionName());
                                        task.setState(String.valueOf(DeviceStatus.state.WAITING));
                                        task.setUserName(username);
                                        task.setUserId(userId);
                                        task.setTotalItems(0);
                                        task.setFinishedItems(0);
                                        task.setServerName(serverUrl);

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
                                    Intent AUIntent = new Intent(activity, TaskUploadService.class);
                                    stopService(AUIntent);

                                    Intent MUIntent = new Intent(activity, ManualUploadService.class);
                                    stopService(MUIntent);

                                    //delete sharedPreference(move to logout callback after backend implementation)
                                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.API_KEY);
                                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.USER_ID);
                                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.USER_NAME);
                                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME);
                                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.IS_ALBUM);
                                    Intent logoutIntent = new Intent(activity, LoginActivity.class);
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
                    Intent AUIntent = new Intent(activity, TaskUploadService.class);
                    stopService(AUIntent);

                    Intent MUIntent = new Intent(activity, ManualUploadService.class);
                    stopService(MUIntent);

                    //delete sharedPreference(move to logout callback after backend implementation)
                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.API_KEY);
                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.USER_ID);
                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.USER_NAME);
                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME);
                    PreferenceUtil.clearPrefs(activity, Constants.SHARED_PREFERENCES, Constants.IS_ALBUM);

                    Intent logoutIntent = new Intent(activity, LoginActivity.class);
                    startActivity(logoutIntent);

                    finish();
                }
            }
        });

    }

    //set user info textView(name email)
    private void setUserInfoText(){
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        View headerLayout = navigationView.getHeaderView(0);

        TextView nameTextView = (TextView) headerLayout.findViewById(R.id.tv_username);
        TextView emailTextView = (TextView) headerLayout.findViewById(R.id.tv_user_email);
        TextView serverTextView = (TextView) headerLayout.findViewById(R.id.tv_server_url);
        nameTextView.setText(username);
        emailTextView.setText(email);
        if(serverUrl.length()<25){
            serverTextView.setText(serverUrl);
        }else
            serverTextView.setText(serverUrl.substring(0,25)+"...");
    }

    private void dispatchTakePictureIntent() {

        scheduleJob(this);

        PackageManager packman = getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String pack = intent.resolveActivity(packman).getPackageName();
        String cls = intent.resolveActivity(packman).getClassName();

        Intent mIntent = new Intent();
        mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ComponentName comp = new ComponentName(pack,cls);
        mIntent.setComponent(comp);
        mIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mIntent.setAction("android.intent.action.View");

        startActivity(mIntent);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_COLLECTION_REQUEST) {
            if(resultCode == RemoteCollectionSettingsActivity.INTENT_NONE) {
                // get isNone, collectionId is not valid anymore
                if(data.getBooleanExtra("isNone",false)){
                    // set collection name none
                    if(collectionNameTextView!=null){
                        collectionNameTextView.setText("none");
                    }
                }
            }else if(resultCode == RESULT_OK){

                // prepare settings
                Settings settings = DBConnector.getSettingsByUserId(userId);
                if(settings==null){
                    settings = new Settings();
                }

                Task lastAUTask = DBConnector.getAuTask(userId, serverUrl);

                if(lastAUTask!= null){
                    collectionNameTextView.setText(lastAUTask.getCollectionName());

                    if(settings.isAutoUpload()){
                        autoUploadSwitch.setChecked(true);
                    }else{
                        autoUploadSwitch.setChecked(false);
                    }

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

                    if(settings.isAutoUpload()){
                        autoUploadSwitch.setChecked(true);
                    }else{
                        autoUploadSwitch.setChecked(false);
                    }

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

    private void checkPermission() {
        RxPermissions rxp = new RxPermissions(activity);
        rxp.request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(granted -> {
                    if (granted) {
                        dispatchTakePictureIntent();
                    }
                    else {
                        showToast(R.string.exception_no_photo_permission);
                    }
                });
    }

    public static void scheduleJob(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            JobScheduler js =
                    (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = null;
            builder = new JobInfo.Builder(
                    MY_BACKGROUND_JOB,
                    new ComponentName(context, MediaContentJobService.class));
            builder.addTriggerContentUri(
                    new JobInfo.TriggerContentUri(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            JobInfo.TriggerContentUri.FLAG_NOTIFY_FOR_DESCENDANTS));
            js.schedule(builder.build());
        }
    }

    @Override
    public void getCollectionsSuc(CollectionMessage collectionMessage) {
        List<ImejiFolder> folderList = new ArrayList<>();
        for (ImejiFolderModel imejiFolderModel : collectionMessage.getResults()) {
            ImejiFolder imejiFolder = new ImejiFolder();
            imejiFolder.setImejiId(imejiFolderModel.getId());  // parsed Id is ImejiId
            imejiFolder.setContributors(imejiFolderModel.getContributors());
            imejiFolder.setTitle(imejiFolderModel.getTitle());
            imejiFolder.setDescription(imejiFolderModel.getDescription());
            imejiFolder.setModifiedDate(imejiFolderModel.getModifiedDate());
            imejiFolder.setCreatedDate(imejiFolderModel.getCreatedDate());
            folderList.add(imejiFolder);
        }

        if(folderList.size()==0){
            // first delete AutoTask
            new Delete().from(Task.class).where("uploadMode = ?", "AU").execute();
            collectionNameTextView.setText("none");
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
                            // AU:off col:null
                            setAutoUploadStatus(false,false);

                        }
                    })
                    .setIcon(R.drawable.error_alert)
                    .show();
        }
        else if(folderList.size()==1){

            DBConnector.deleteFinishedAUTasks(userId, serverUrl);             //delete all AU Task if finished

            Task task = new Task();                                 //new a AU Task
            task.setUploadMode("AU");
            task.setCollectionId(folderList.get(0).id);
            task.setState(String.valueOf(DeviceStatus.state.WAITING));
            task.setUserName(username);
            task.setUserId(userId);
            task.setServerName(serverUrl);
            task.setTotalItems(0);
            task.setFinishedItems(0);

            Long now = new Date().getTime();
            task.setStartDate(String.valueOf(now));
            task.setCollectionName(folderList.get(0).getTitle());
            task.save();

            //set selected collection name text
            if(folderList.get(0).getTitle()!=null) {
                collectionNameTextView.setText(folderList.get(0).getTitle());
            }

            Settings settings = DBConnector.getSettingsByUserId(userId);   // get old settings

            //switch on
            if(settings.isAutoUpload()){
                autoUploadSwitch.setChecked(true);
            }else{
                autoUploadSwitch.setChecked(false);
            }

            if(settings!=null && settings.isAutoUpload())  // history AU is on
            {
                setAutoUploadStatus(false,true);
            }else {                                        // history AU is off or not set
                setAutoUploadStatus(false,false);
            }
        }
        else {  // Col > 1

            if(!isLoginCall){   // callback from switch
                Intent settingsIntent = new Intent(activity, RemoteCollectionSettingsActivity.class);
                startActivityForResult(settingsIntent,PICK_COLLECTION_REQUEST);
                return;
            }

            Settings settings = DBConnector.getSettingsByUserId(userId);   // get old settings

            //set collection name
            if(settings!=null && settings.isAutoUpload())  // history AU is on
            {
                boolean isValid = false;
                //col wasValue
                Task auTask = DBConnector.getAuTask(userId,serverUrl);
                if(auTask!=null && auTask.getCollectionId() !=null) {   // col wasValue not null
                    for(int i = 0; i<folderList.size(); i++){           // col wasValue still valid on server
                        if(auTask.getCollectionId().equalsIgnoreCase(folderList.get(i).id)){
                            isValid = true;
                        }
                        collectionNameTextView.setText(auTask.getCollectionName());     // collection name from autoTask
                        setAutoUploadStatus(false,true);
                    }
                }

                if(!isValid){ // col wasValue invalid
                    // dialog lead new user to choose collection

                    // old collection not valid
                    Task task = DBConnector.getAuTask(userId, serverUrl);
                    if(task!=null){
                        task.delete();}

                    new AlertDialog.Builder(activity)
                            .setTitle("Notice")
                            .setMessage("please set a Collection")
                            .setCancelable(false)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // go to set collection
                                    Intent settingsIntent = new Intent(activity, RemoteCollectionSettingsActivity.class);
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
            }else {   // history AU is off or not set
                Task auTask = DBConnector.getAuTask(userId,serverUrl);
                if(auTask!=null) {   // col wasValue not null
                    collectionNameTextView.setText(auTask.getCollectionName());     // collection name from autoTask
                }
                setAutoUploadStatus(false,false);
            }
        }
    }

    @Override
    public void getCollectionsFail(Throwable e) {

    }

    @Override
    public void createCollectionsSuc(ImejiFolderModel imejiFolder) {
        DBConnector.deleteFinishedAUTasks(userId, serverUrl);             //delete all AU Task if finished

        Task task = new Task();                                 //new a AU Task
        task.setUploadMode("AU");
        task.setCollectionId(imejiFolder.getId());
        task.setState(String.valueOf(DeviceStatus.state.WAITING));
        task.setUserName(username);
        task.setUserId(userId);
        task.setServerName(serverUrl);
        task.setTotalItems(0);
        task.setFinishedItems(0);

        Long now = new Date().getTime();
        task.setStartDate(String.valueOf(now));
        task.setCollectionName(imejiFolder.getTitle());
        task.save();

        //set selected collection name text
        collectionNameTextView.setText(imejiFolder.getTitle());

        pDialog.dismiss();
        Settings settings = DBConnector.getSettingsByUserId(userId); // get old settings

        //switch on
        //check the previous status of automatic upload
        if(settings.isAutoUpload()) {
            autoUploadSwitch.setChecked(true);
        }else{
            autoUploadSwitch.setChecked(false);
        }

        if(settings!=null && settings.isAutoUpload())  // history AU is on
        {
            setAutoUploadStatus(false,true);
        }else {                                        // history AU is off or not set
            setAutoUploadStatus(false,false);
        }

    }

    @Override
    public void createCollectionsFail(Throwable e) {

    }
}

