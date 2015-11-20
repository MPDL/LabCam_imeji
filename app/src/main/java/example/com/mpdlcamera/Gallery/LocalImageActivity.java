package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.CircularProgressButton;
import com.squareup.otto.Produce;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.Otto.OttoSingleton;
import example.com.mpdlcamera.Otto.UploadEvent;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import example.com.mpdlcamera.SQLite.FileId;
import example.com.mpdlcamera.SQLite.MySQLiteHelper;
import example.com.mpdlcamera.Utils.DeviceStatus;
import example.com.mpdlcamera.Utils.ImageFileFilter;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;

/**
 * Created by allen on 03/09/15.
 */
public class LocalImageActivity extends AppCompatActivity {

    private List<String> dataPathList = new ArrayList<String>();
    private List<String> selectedDataPathList = new ArrayList<String>();

    public LocalImageAdapter adapter;
//    public  ImagesGridAdapter adapter;
    private GridView gridView;
    private View rootView;
    private String dataCollectionId;
    private Activity activity = this;
    private final String LOG_TAG = LocalImageActivity.class.getSimpleName();
    private SharedPreferences mPrefs;
    private String username;
    private String password;
    private TypedFile typedFile;
    private String json;

    private Toolbar toolbar;
    private String folderPath;
    private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener;

    private CircularProgressButton circularButton;


    Callback<DataItem> callback = new Callback<DataItem>() {
        @Override
        @Produce
        public void success(DataItem dataItem, Response response) {

            Toast.makeText(activity, "Uploaded Successfully", Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG, dataItem.getCollectionId() + ":" + dataItem.getFilename());

            MySQLiteHelper db = new MySQLiteHelper(activity);
            String fileNamePlusId = dataItem.getFilename() + dataCollectionId;
            FileId fileId = new FileId(fileNamePlusId,"yes");
            db.insertFile(fileId);

            mPrefs = PreferenceManager.getDefaultSharedPreferences(activity);

            /*
                Delete the file if the setting "Remove the photos after upload" is On
             */
            if(mPrefs.contains("RemovePhotosAfterUpload")) {
                if(mPrefs.getBoolean("RemovePhotosAfterUpload",true)) {

                    File file = typedFile.file();
                    Boolean deleted = file.delete();
                    Log.v(LOG_TAG, "deleted:" +deleted);
                }
            }
            adapter.notifyDataSetChanged();

            circularButton.setProgress(100);

        }


        @Override
        public void failure(RetrofitError error) {

            if (error == null || error.getResponse() == null) {
                OttoSingleton.getInstance().post(new UploadEvent(null));
                if(error.getKind().name().equalsIgnoreCase("NETWORK")) {
                    Toast.makeText(activity, "Please Check your Network Connection", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            } else {
                OttoSingleton.getInstance().post(
                        new UploadEvent(error.getResponse().getStatus()));
                String jsonBody = new String(((TypedByteArray) error.getResponse().getBody()).getBytes());
                if (jsonBody.contains("already exists")) {
                    Toast.makeText(activity, "Photo already exists", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(activity, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            }
            circularButton.setProgress(-1);

            Log.v(LOG_TAG, String.valueOf(error));

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_gallery_gridview);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");
        dataCollectionId = mPrefs.getString("collectionID", DeviceStatus.collectionID);

        Intent intent = activity.getIntent();
        if (intent != null) {
            folderPath = intent.getStringExtra("galleryTitle");
            if(folderPath != null) {
                ///storage/emulated/0/DCIM/Screenshots
                titleView.setText(folderPath.split("\\/")[folderPath.split("\\/").length -1]);
                Log.v("title from Kiran", folderPath);
            }
        }

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        for(int i = 0; i<albums.length ;i++){
            Log.v("albums",  albums[i]);
        }

        Log.v("Images",  images.toString());

        //final String CompleteCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + title;
        File folder = new File(folderPath);
        File[] folderFiles = folder.listFiles();

        Log.v("camera folder",folderPath);

        for (File imageFile : folderFiles) {
            Log.v("file",imageFile.toURI().toString() );

            if(new ImageFileFilter(imageFile).accept(imageFile)) {
                dataPathList.add(imageFile.getAbsolutePath());
            }
        }

        adapter = new LocalImageAdapter(activity, dataPathList, false);

        //rootView = inflater.inflate(R.layout.fragment_section_list_swipe, container, false);
        gridView = (GridView) findViewById(R.id.image_gridView);
        //listView = (SwipeMenuListView) rootView.findViewById(R.id.listView);
        gridView.setAdapter(adapter);
        //registerForContextMenu(gridView);

        circularButton = (CircularProgressButton) findViewById(R.id.circularButton);

        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int selectedCount = 0;

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // TODO Auto-generated method stub
                adapter.clearSelection();
                toolbar.setVisibility(View.VISIBLE);
                circularButton.setVisibility(View.GONE);

                //adapter.getCheckBox().setVisibility(View.GONE);

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                selectedCount = 0;

                toolbar.setVisibility(View.GONE);

                //adapter.getCheckBox().setVisibility(View.VISIBLE);

                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.contextual_menu_local, menu);
//                menu.add(Menu.NONE, R.id.item_delete_local, Menu.NONE, "Delete");
//                menu.add(Menu.NONE, R.id.item_upload_local, Menu.NONE, "Upload");

                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                int id = item.getItemId();
                Log.v(LOG_TAG, ""+ id);

                switch (item.getItemId()) {
                    case R.id.item_delete_local:
                        selectedCount = 0;
                        Log.v(LOG_TAG,"##delete");
                        if(selectedDataPathList != null) {
                            delete(selectedDataPathList);
                            for(String str: selectedDataPathList){
                                dataPathList.remove(str);
                            }
                            adapter.notifyDataSetChanged();

                        }
                        selectedDataPathList.clear();
                        adapter.clearSelection();

                        mode.finish();
                        break;

                    case R.id.item_upload_local:
//                        nr = 0;
//                        adapter.clearSelection();
                        Log.v(LOG_TAG,"upload");

                        Log.v(LOG_TAG, " "+selectedDataPathList.size());

                        Log.v(LOG_TAG, selectedDataPathList.get(0));

                        circularButton.setVisibility(View.VISIBLE);
                        circularButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
                        circularButton.setProgress(50); // set progress > 0 & < 100 to display indeterminate progress

                        if(selectedDataPathList != null) {
                            upload(selectedDataPathList);
                        }
                        selectedDataPathList.clear();
                        adapter.clearSelection();

                        mode.finish();
                        break;

                }

                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position,
                                                  long id,
                                                  boolean checked) {
                if (checked) {
                    selectedCount++;
                    adapter.setNewSelection(position, checked);
                    selectedDataPathList.add(dataPathList.get(position));
                    //adapter.getCheckBox().setChecked(true);

                } else {
                    selectedCount--;
                    adapter.removeSelection(position);
                    selectedDataPathList.remove(dataPathList.get(position));
                    //adapter.getCheckBox().setChecked(false);

                }
                mode.setTitle(selectedCount + " selected");

            }
        });

        gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                gridView.setItemChecked(position, !adapter.isPositionChecked(position));
                return false;
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_local, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_select) {
            Log.v(LOG_TAG, "selected");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }



    /*
        upload the selected files
     */
    private void upload(List<String> fileList) {
        circularButton.setVisibility(View.VISIBLE);
        circularButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
        circularButton.setProgress(50);

        String jsonPart1 = "\"collectionId\" : \"" +
                dataCollectionId +
                "\"";

        for (String filePath : fileList) {
            typedFile = new TypedFile("multipart/form-data", new File(filePath));

            json ="{" + jsonPart1  +"}";

            Log.v(LOG_TAG, json);
            RetrofitClient.uploadItem(typedFile, json, callback, username, password);
        }
    }


    /*
        delete the selected files
     */
    private void delete(List<String> toBeDeleteImagePathList) {
        for(String imagePath : toBeDeleteImagePathList) {

            File file = new File(imagePath);
            Boolean deleted = file.delete();
            Log.v(LOG_TAG, "deleted:" + deleted);
            //  adapter.notifyDataSetChanged();

        }
    }



}
