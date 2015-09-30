package example.com.mpdlcamera;

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
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.squareup.otto.Produce;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.FolderModel;
import example.com.mpdlcamera.Model.MetaData;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.Utils.DeviceStatus;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;


/**
 * Created by kiran on 25.09.15.
 */


public class SettingsActivity extends ListActivity {


    String networkStatus;
    String prefOption;
    ArrayList<String> permFolder;
    CustomAdapter switchAdapter;

    private final String LOG_TAG = SettingsActivity.class.getSimpleName();
    //TODO set collection dynamically
    private String collectionID = DeviceStatus.collectionID;
    private String username;
    private String password;

    private List<DataItem> dataList = new ArrayList<DataItem>();
    private DataItem item = new DataItem();
    private MetaData meta = new MetaData();
    SharedPreferences preferences;
    private User user;
    String status;
    Boolean fStatus;
    String json;
    // Boolean n;


    private SharedPreferences mPrefs;
    public TypedFile typedFile;


    private CheckBox checkSyncAll;





    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //CustomAdapter switchAdapter = new CustomAdapter(this, 1);

        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");
        if(!mPrefs.getString("collectionID", "").equals("")){
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

        //  final ArrayList<FolderModel> folders1 = new ArrayList<FolderModel>();
        Cursor cur = getContentResolver().query(images, albums, null, null, null);


        checkSyncAll = (CheckBox) SettingsActivity.this.findViewById(R.id.syncAllCheck);

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

        ArrayList<FolderModel> folderList = new ArrayList<FolderModel>();


        Iterator<String> folderIterator = imageFolders.iterator();
        while (folderIterator.hasNext()) {
            String now = folderIterator.next().toString();
                     if(preferences.contains(now)) {
                            status = preferences.getString(now,"");
                       }
                   else {
                               status = "Off";
                           }
                       if(status.equalsIgnoreCase("On")) {
                                fStatus = true;
                           }
                      else {

                                               fStatus = false;
                           }
                       FolderModel folderOne = new FolderModel(now, fStatus);
            folderList.add(folderOne);
        }


        switchAdapter = new CustomAdapter(this, R.layout.row, folderList);
        setListAdapter(switchAdapter);

        final ListView listView = getListView();

        final int size = getListAdapter().getCount();


        checkSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSyncAll.isChecked()) {


                    for (int i = 0; i < size; i++) {
                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listView);
                        Switch mSwitch = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        mSwitch.setChecked(true);

                    }
                }
                if (!checkSyncAll.isChecked()) {

                    for (int i = 0; i < size; i++) {
                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listView);
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




