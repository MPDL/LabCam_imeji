package example.com.mpdlcamera.Settings;

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
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.io.File;
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


public class LocalAlbumSettingsActivity extends AppCompatActivity  {


    String networkStatus;
    String prefOption;
    ArrayList<String> permFolder;
    CustomAdapter switchAdapter;
    Boolean checkAll = null;
    ListView listViewLocal;

    private final String LOG_TAG = LocalAlbumSettingsActivity.class.getSimpleName();
    //TODO set collection dynamically
    private String collectionID = DeviceStatus.collectionID;
    private String username;
    private String password;
    private View rootView;
    Toolbar toolbar;


    private List<DataItem> dataList = new ArrayList<DataItem>();

    SharedPreferences preferences;
    Context context = this;
    SharedPreferences preferencesFiles;
    SharedPreferences preferencesFolders;
    private User user;
    String status;
    Boolean fStatus;
    ArrayList<String> imageFolders;



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
        prefOption = preferences.getString("status", ""); //get



    }

    /*
        Displaying the ListView by using adapter
     */
    private void displayListView() {

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME,MediaStore.Images.Media.DATA};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferencesFolders = getSharedPreferences("folder", Context.MODE_PRIVATE);
        preferencesFiles = getSharedPreferences("gallery", Context.MODE_PRIVATE);

        for(int i = 0; i<albums.length ;i++){
            Log.i("albums",  albums[i]);
        }

        Log.i("Images",  images.toString());

        final ArrayList<String> folders = new ArrayList<String>();

        Cursor cur = getContentResolver().query(images, albums, null, null, null);


        checkSyncAll = (CheckBox) LocalAlbumSettingsActivity.this.findViewById(R.id.syncAllCheck);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        //Sync all if the settings for sync all is true in Shared Preferences
        if(preferences.contains("syncall")) {
            if(preferences.getBoolean("syncall",true)) {
                checkSyncAll.setChecked(true);
            }
        }

        //Logging the image count
        Log.i("ListingImages", " query count=" + cur.getCount());

        if (cur.moveToFirst()) {
            String album;
            String filePath;
            int albumLocation = cur.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int path = cur.getColumnIndex(MediaStore.Images.Media.DATA);

            do {
                album = cur.getString(albumLocation);
                filePath = cur.getString(path);
                File file = new File(filePath);
                String directory = file.getParent();
                SharedPreferences.Editor editor = preferencesFiles.edit();
                editor.putString(album, directory);
                editor.commit();
                folders.add(album);
                Log.i("ListingImages", " album=" + album);
            } while (cur.moveToNext());
        }
        imageFolders = new ArrayList<String>();
        imageFolders = new ArrayList<String>(new LinkedHashSet<String>(folders));

        ArrayList<LocalGallery> folderList = new ArrayList<LocalGallery>();


        // check the status of the folder in Shared Preferences and set the flag.
        Iterator<String> folderIterator = imageFolders.iterator();
        while (folderIterator.hasNext()) {
            String now = folderIterator.next().toString();
            if (preferencesFolders.contains(now)) {
                status = preferencesFolders.getString(now, "");
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

        listViewLocal = (ListView) findViewById(R.id.listviewLocal);
        listViewLocal.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        listViewLocal.setAdapter(switchAdapter);

        // final ListView listView = getListView();

        final int size = listViewLocal.getAdapter().getCount();


        //OnCLickListener for the Sync All check button
        checkSyncAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSyncAll.isChecked()) {
                    checkAll = true;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences preferences1 = getSharedPreferences("folder", Context.MODE_PRIVATE); //get the gallery path of the corresponding gallery from the shared preferences
                    SharedPreferences.Editor ed1 = preferences1.edit();
                    SharedPreferences.Editor ed = preferences.edit();
                    ed.putBoolean("syncall",true);
                    ed.commit();
                    ArrayList<LocalGallery> folderList = new ArrayList<LocalGallery>();

                    Iterator<String> folderIterator = imageFolders.iterator();

                    // Iterate through all the folders and set them checked in view as well as shared preferences
                    while (folderIterator.hasNext()) {
                        String now = folderIterator.next().toString();
                        fStatus = true;
                        ed1.putString(now,"On");
                        ed1.commit();
                        LocalGallery folderOne = new LocalGallery(now, fStatus);
                        folderList.add(folderOne);
                    }

                    switchAdapter = new CustomAdapter(context, R.layout.row, folderList);

                    listViewLocal = (ListView) findViewById(R.id.listviewLocal);
                    listViewLocal.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                    listViewLocal.setAdapter(switchAdapter);
                    switchAdapter.notifyDataSetChanged(); //refreshes the adapter if there are any changes



//                    for (int i = 0; i < size; i++) {
//                        checkAll = true;
//                       // switchAdapter.getView()
//                       // ((ArrayAdapter<LocalGallery>) listViewLocal.getAdapter()).notifyDataSetChanged();
//                        //switchAdapter.notifyDataSetChanged();
//
//                       // listViewLocal.setItemChecked(i,true);
//                      //  RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listViewLocal);
//                      //  Switch mSwitch = (Switch) RelativeOne.findViewById(R.id.fswitch);
//                      //  mSwitch.setChecked(true);
//
//                    }
                   // switchAdapter.notifyDataSetChanged();
                }
               // else if (!checkSyncAll.isChecked()) {
                else {
                    checkAll = false;
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    SharedPreferences preferences1 = getSharedPreferences("folder", Context.MODE_PRIVATE);
                    SharedPreferences.Editor ed1 = preferences1.edit();
                    SharedPreferences.Editor ed = preferences.edit();
                    ed.putBoolean("syncall",false);
                    ed.commit();
                    ArrayList<LocalGallery> folderList = new ArrayList<LocalGallery>();

                    Iterator<String> folderIterator = imageFolders.iterator();

                    //Iterate through all the folders and set them unchecked in the view as well as in the shared preferences
                    while (folderIterator.hasNext()) {
                        String now = folderIterator.next().toString();
                        fStatus = false;
                        ed1.putString(now,"Off");
                        ed1.commit();
                        LocalGallery folderOne = new LocalGallery(now, fStatus);
                        folderList.add(folderOne);
                    }

                    switchAdapter = new CustomAdapter(context, R.layout.row, folderList);

                    listViewLocal = (ListView) findViewById(R.id.listviewLocal);
                    listViewLocal.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                    listViewLocal.setAdapter(switchAdapter);
                    switchAdapter.notifyDataSetChanged();


                    // switchAdapter.notifyDataSetChanged();
                }

            }
        });
    }


    //returns the view for the position
    public View getViewByPosition(int position, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (position < firstListItemPosition || position > lastListItemPosition) {
            return listView.getAdapter().getView(position, null, listView);
        } else {
            final int childIndex = position - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }


    //the adapter class which contains all the conents for the listView
    public class CustomAdapter extends ArrayAdapter<LocalGallery> {

        Context mContext;


        private final String LOG_TAG = CustomAdapter.class.getSimpleName();

        String prefOption;


        private ArrayList<LocalGallery> folderList;

        public CustomAdapter(Context context, int textViewResourceId,
                             ArrayList<LocalGallery> folderList) {
            super(context, textViewResourceId, folderList);
            this.mContext = context;
            this.folderList = new ArrayList<LocalGallery>();
            this.folderList.addAll(folderList);
        }

        private class ViewHolder {
            TextView textView;
            Switch fSwitch;

        }


        /*
            creates the view everytime the screen refreshes
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            ViewHolder holder = null;
            Log.v("ConvertView", String.valueOf(position));

            if (true) {
                LayoutInflater vi = (LayoutInflater) mContext.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.row, null);

                holder = new ViewHolder();
                holder.textView = (TextView) convertView.findViewById(R.id.folder);
                holder.fSwitch = (Switch) convertView.findViewById(R.id.fswitch);


                convertView.setTag(holder);

                /*if(checkAll != null && checkAll)
                {
                    for(int i = 0; i<switchAdapter.getCount(); i++) {

                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listViewLocal);
                        Switch sw = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        LocalGallery folder = (LocalGallery) sw.getTag();

                        folder.setSelected(true);
                    }
                }
                if(checkAll != null && !checkAll)
                {
                    for(int i = 0; i<switchAdapter.getCount(); i++) {

                        RelativeLayout RelativeOne = (RelativeLayout) getViewByPosition(i, listViewLocal);
                        Switch sw = (Switch) RelativeOne.findViewById(R.id.fswitch);
                        LocalGallery folder = (LocalGallery) sw.getTag();

                        folder.setSelected(false);
                    }
                }
*/

              //  holder.fSwitch.setOnCheckedChangeListener(switchChangeListener);
                /*
                listener method for the check change of the switch
                 */
        holder.fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                      Switch switchOne = (Switch) buttonView;
                     LocalGallery folder = (LocalGallery) switchOne.getTag();

                        if(checkAll != null) {
                            if (!switchOne.isChecked() && checkAll) {
                                checkSyncAll.setChecked(false);
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor ed = preferences.edit();
                                ed.putBoolean("syncall",false);
                                ed.commit();
                            }
                        }
                        folder.setSelected(switchOne.isChecked());
                    }
                });
                                /*
                listener method for the check change of the switch
                 */
                holder.fSwitch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Switch switchOne = (Switch) v;
                        LocalGallery folder = (LocalGallery) switchOne.getTag();

                        if (checkAll != null) {
                            if (!switchOne.isChecked() && checkAll) {
                                checkSyncAll.setChecked(false);
                                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                                SharedPreferences.Editor ed = preferences.edit();
                                ed.putBoolean("syncall", false);
                                ed.commit();
                            }
                        }
                        folder.setSelected(switchOne.isChecked());
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag(); // View Holder to hold the status for the files
            }

                            /*
                listener method for the check change of the switch
                 */
            holder.fSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (!isChecked) {

                        RelativeLayout relativeLayout = (RelativeLayout) buttonView.getParent();
                        TextView textView = (TextView) relativeLayout.findViewById(R.id.folder);
                        String folder = textView.getText().toString();

                        //    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences settings = mContext.getSharedPreferences("folder", Context.MODE_PRIVATE);

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(folder, "Off");
                        editor.commit();


//                    } else if (isChecked) {
                    } else {

                        // SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences settings = mContext.getSharedPreferences("folder", Context.MODE_PRIVATE);

                        RelativeLayout relativeLayout = (RelativeLayout) buttonView.getParent();
                        TextView textView = (TextView) relativeLayout.findViewById(R.id.folder);
                        String folder = textView.getText().toString();

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(folder, "On");
                        editor.commit();


                    }

                }


            });


            LocalGallery folder = folderList.get(position);
            holder.textView.setText(folder.getGallery());

            holder.fSwitch.setChecked(folder.isSelected());
            holder.fSwitch.setTag(folder);

            return convertView;

        }


    }

}




