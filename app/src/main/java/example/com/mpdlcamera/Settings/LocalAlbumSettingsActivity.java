package example.com.mpdlcamera.Settings;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.LocalGallery;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Upload.CustomAdapter;
import example.com.mpdlcamera.Utils.DeviceStatus;


/**
 * Created by kiran on 25.09.15.
 */


public class LocalAlbumSettingsActivity extends AppCompatActivity {


    String networkStatus;
    String prefOption;
    ArrayList<String> permFolder;
    CustomAdapter switchAdapter;

    private final String LOG_TAG = LocalAlbumSettingsActivity.class.getSimpleName();
    //TODO set collection dynamically
    private String collectionID = DeviceStatus.collectionID;
    private String username;
    private String password;
    private View rootView;
    Toolbar toolbar;


    private List<DataItem> dataList = new ArrayList<DataItem>();

    SharedPreferences preferences;
    private User user;
    String status;
    Boolean fStatus;



    private SharedPreferences mPrefs;


    private CheckBox checkSyncAll;


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_album_settings);

        //CustomAdapter switchAdapter = new CustomAdapter(this, 1);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");
        if (!mPrefs.getString("collectionID", "").equals("")) {
            collectionID = mPrefs.getString("collectionID", "");
        }


        //Generate listView from ArrayList
        displayListView();

        user = new User();
        user.setCompleteName("Kiran");
        user.save();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        networkStatus = networkInfo.getTypeName();

        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        prefOption = preferences.getString("status", "");


    }

    /*
    Displaying the ListView by using adapter
     */
    private void displayListView() {

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferences = PreferenceManager.getDefaultSharedPreferences(this);


        final ArrayList<String> folders = new ArrayList<String>();

        Cursor cur = getContentResolver().query(images, albums, null, null, null);


        checkSyncAll = (CheckBox) LocalAlbumSettingsActivity.this.findViewById(R.id.syncAllCheck);

        //Logging the image count
        Log.i("ListingImages", " query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String album;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            do {
                album = cur.getString(albumLocation);
                folders.add(album);
                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }

        ArrayList<String> imageFolders = new ArrayList<String>();
        imageFolders = new ArrayList<String>(new LinkedHashSet<String>(folders));

        ArrayList<LocalGallery> folderList = new ArrayList<LocalGallery>();


        Iterator<String> folderIterator = imageFolders.iterator();
        while (folderIterator.hasNext()) {
            String now = folderIterator.next().toString();
            if (preferences.contains(now)) {
                status = preferences.getString(now, "");
            } else {
                status = "Off";
            }
            if (status.equalsIgnoreCase("On")) {
                fStatus = true;
            } else {

                fStatus = false;
            }
            LocalGallery folderOne = new LocalGallery(now, fStatus);
            folderList.add(folderOne);
        }


        switchAdapter = new CustomAdapter(this, R.layout.row, folderList);

        final ListView listViewLocal = (ListView) findViewById(R.id.listviewLocal);
        listViewLocal.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        listViewLocal.setAdapter(switchAdapter);

        // final ListView listView = getListView();

        final int size = listViewLocal.getAdapter().getCount();


        checkSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSyncAll.isChecked()) {


                    for (int i = 0; i < size; i++) {
                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listViewLocal);
                        Switch mSwitch = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        mSwitch.setChecked(true);

                    }
                }
                if (!checkSyncAll.isChecked()) {

                    for (int i = 0; i < size; i++) {
                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listViewLocal);
                        Switch mSwitch = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        mSwitch.setChecked(false);

                    }
                }

            }
        });
    }


    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }
}




