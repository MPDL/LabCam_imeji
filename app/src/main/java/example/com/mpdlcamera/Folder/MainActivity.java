package example.com.mpdlcamera.Folder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import com.activeandroid.query.Delete;

import example.com.mpdlcamera.ImejiFragment.ImejiFragment;
import example.com.mpdlcamera.LocalFragment.LocalFragment;
import example.com.mpdlcamera.Model.ImejiFolder;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.NetChangeManager.NetChangeObserver;
import example.com.mpdlcamera.NetChangeManager.NetWorkStateReceiver;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Upload.NewFileObserver;
import example.com.mpdlcamera.Upload.UploadResultReceiver;
import example.com.mpdlcamera.UploadFragment.UploadFragment;
import example.com.mpdlcamera.UserFragment.UserFragment;
/**
 * Created by kiran on 25.08.15.
 */
public class MainActivity extends AppCompatActivity implements UploadResultReceiver.Receiver,NetChangeObserver {

    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private String username;
    private String password;
    private SharedPreferences mPrefs;

    private ProgressDialog pDialog;


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

    private void hidePDialog() {
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
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
