package example.com.mpdlcamera;

import android.animation.ObjectAnimator;
import android.app.ListActivity;
import android.content.Context;

import android.content.SharedPreferences;
import android.database.Cursor;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import android.widget.Toast;



import com.activeandroid.query.Select;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;



import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Model.MetaData;
import example.com.mpdlcamera.Model.User;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
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
    private User user;
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


            //Generate listView from ArrayList
            displayListView();

            user = new User();
            user.setCompleteName("Kiran");
            user.save();
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();

            networkStatus = networkInfo.getTypeName();

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            prefOption = preferences.getString("status", "");
            System.out.println("ille" + prefOption);


        }

        /*
        Displaying the ListView by using adapter
         */
        private void displayListView() {

            String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
            Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


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
                FolderModel folderOne = new FolderModel(folderIterator.next(), false);
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


  /*  private FolderModel getModel(int position) {
        return (((CustomAdapter) getListAdapter()).getItem(position));
    } */





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

      /*  private void upload(DataItem item) {
            String jsonPart1 = "\"collectionId\" : \"" +
                    collectionID +
                    "\"";
            Gson gson = new GsonBuilder()
                    .serializeNulls()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create();
            item.getMetadata().setDeviceID("1");
            typedFile = new TypedFile("multipart/form-data", new File(item.getLocalPath()));


           // json = "{" + jsonPart1 + "}";
            json = "{" + jsonPart1 + "}";

            Log.v(LOG_TAG, json);
            RetrofitClient.uploadItem(typedFile, json, callback, username, password);


        }
*/

    };










  /*  public void switchActivity(View view) {


{"labels":[{"language":"en","value":"title"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW","typeUri":"http://imeji.org/terms/metadata#text","value":{"text":"20150916_141350.wav@Amalienstraße 33, 80799 München, Germany"}}



        System.out.println("coming here");
        Switch sw = (Switch) view;
        sw.setOnCheckedChangeListener(null);
        if (sw.isChecked()) {

        09-16 14:14:03.719: V/ReadyToUploadCollectionActivity(14752): {"collectionId" : "DCQVKA8esikfRTWi","metadata": [{"labels":[{"language":"en","value":"title"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/iNUP1SRHR9OSZGy","typeUri":"http://imeji.org/terms/metadata#text","value":{"text":"20150916_141350.wav@Amalienstraße 33, 80799 München, Germany"}},{"labels":[{"language":"en","value":"author"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/_HTF9UJTnH4SvZRr","typeUri":"http://imeji.org/terms/metadata#text","value":{"text":"Allen"}},{"labels":[{"language":"en","value":"location"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/vxKbKv5mNxKjueid","typeUri":"http://imeji.org/terms/metadata#geolocation","value":{"latitude":48.1480062,"longitude":11.5767977,"name":"Amalienstraße 33, 80799 München, Germany"}},{"labels":[{"language":"en","value":"accuracy"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/DCAGIb3A1pcu1Nmj","typeUri":"http://imeji.org/terms/metadata#number","value":{"number":10.0}},{"labels":[{"language":"en","value":"deviceID"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/ntXvuGrRf_705f_","typeUri":"http://imeji.org/terms/metadata#text","value":{"text":"1"}},{"labels":[{"language":"en","value":"tags"}],"statementUri":"http://dev-faces.mpdl.mpg.de/imeji/statement/VwC_f_NcbS8vQhjV","typeUri":"http://imeji.org/terms/metadata#text","value":{"text":"test"}}]}




            RelativeLayout linearOne = (RelativeLayout) sw.getParent();
            TextView tv = (TextView) linearOne.findViewById(R.id.folder);

            String folder = tv.getText().toString();

            Toast.makeText(getApplicationContext()," you have selected  " + folder, Toast.LENGTH_LONG).show();


            // Perform actions when the switch is on
        } else {
            sw.setOnCheckedChangeListener(null);

            // Perform actions when the switch is off
        }


"\"metadata\":[{\"labels\":[{\"language\":\"en\",\"value\":\"title\"}],\"statementUri\":\"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW\",\"typeUri\":\"http://imeji.org/terms/metadata#text\",\"value\":{\"text\":\"20150916_141350.wav@Amalienstraße 33, 80799 München, Germany\"}},{\"labels\":[{\"language\":\"en\",\"value\":\"author\"}],\"statementUri\":\"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW\",\"typeUri\":\"http://imeji.org/terms/metadata#text\",\"value\":{\"text\":\"Allen\"}},{\"labels\":[{\"language\":\"en\",\"value\":\"location\"}],\"statementUri\":\"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW\",\"typeUri\":\"http://imeji.org/terms/metadata#text\", \"value\":{\"text\":\"20150916_141350.wav@Amalienstraße 33, 80799 München, Germany\"}},{\"labels\":[{\"language\":\"en\",\"value\":\"accuracy\"}],\"statementUri\":\"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW\",\"typeUri\":\"http://imeji.org/terms/metadata#text\",\"value\":{\"text\":\"10.0\"}},{\"labels\":[{\"language\":\"en\",\"value\":\"deviceID\"}],\"statementUri\":\"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW\",\"typeUri\":\"http://imeji.org/terms/metadata#text\",\"value\":{\"text\":\"1\"}},{\"labels\":[{\"language\":\"en\",\"value\":\"tags\"}],\"statementUri\":\"http://dev-faces.mpdl.mpg.de/imeji/statement/IJNOnHLthFNIYWMW\",\"typeUri\":\"http://imeji.org/terms/metadata#text\",\"value\":{\"text\":\"test\"}}] ";
    }*/




