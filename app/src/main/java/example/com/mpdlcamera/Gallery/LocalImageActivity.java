package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
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

import com.dd.CircularProgressButton;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;
import example.com.mpdlcamera.Utils.ImageFileFilter;

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
    private Activity activity = this;
    private final String LOG_TAG = LocalImageActivity.class.getSimpleName();
    private SharedPreferences mPrefs;
    private String username;
    private String userId;



    private Toolbar toolbar;
    private String folderPath;
    private AbsListView.MultiChoiceModeListener mMultiChoiceModeListener;

    private CircularProgressButton circularButton;

    //Bind Manual uploading service
    private Service myService;
    private boolean bound;



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
        userId = mPrefs.getString("userId","");

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
        //listing all the files
        File[] folderFiles = folder.listFiles();

        Log.v("camera folder",folderPath);

        for (File imageFile : folderFiles) {
            Log.v("file",imageFile.toURI().toString() );

            if(new ImageFileFilter(imageFile).accept(imageFile)) {
                //filtering img files
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
//            case R.id.item_delete_local:
//                selectedCount = 0;
//                Log.v(LOG_TAG,"##delete");
//                if(selectedDataPathList != null) {
//                    delete(selectedDataPathList);
//                    for(String str: selectedDataPathList){
//                        dataPathList.remove(str);
//                    }
//                    adapter.notifyDataSetChanged();
//
//                }
//                selectedDataPathList.clear();
//                adapter.clearSelection();
//
//                mode.finish();
//                break;

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
                    uploadList(selectedDataPathList);
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

    public static RemoteListDialogFragment newInstance(String taskId)
    {
        RemoteListDialogFragment remoteListDialogFragment = new RemoteListDialogFragment();
        Bundle args = new Bundle();
        args.putString("taskId", taskId);
        remoteListDialogFragment.setArguments(args);
        return remoteListDialogFragment;
    }

    /*
            upload the selected files
        */
    private void uploadList(List<String> fileList) {
        circularButton.setVisibility(View.VISIBLE);
        circularButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
        circularButton.setProgress(50);


        mPrefs = activity.getSharedPreferences("myPref", 0);
        String currentTaskId = "";


        currentTaskId = createTask(fileList);

        // go to RemoteCollectionSettings
//        Intent remoteCollectionSettingIntent = new Intent(this, RemoteCollectionSettingsActivity.class);
//        remoteCollectionSettingIntent.putExtra("Manual",)

        newInstance(currentTaskId).show(getFragmentManager(), "remoteListDialog");
//
//        Intent manualUploadServiceIntent = new Intent(this,ManualUploadService.class);
//        manualUploadServiceIntent.putExtra("currentTaskId", currentTaskId);
//        startService(manualUploadServiceIntent);
    }


    private String createTask(List<String> fileList){

        String uniqueID = UUID.randomUUID().toString();
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        Long now = new Date().getTime();

        Task task = new Task();
        task.setTotalItems(fileList.size());
        task.setFinishedItems(0);
        task.setTaskId(uniqueID);
        task.setUploadMode("MU");
        task.setState(String.valueOf(DeviceStatus.state.WAITING));
        task.setUserName(username);
        task.setUserId(userId);
        task.setStartDate(String.valueOf(now));
        task.save();
        int num = addImages(fileList, task.getTaskId());
        task.setTotalItems(num);
        task.save();
        Log.v(LOG_TAG,"setTotalItems:"+num);

        return task.getTaskId();
    }

    private int addImages(List<String> fileList,String taskId){

        int imageNum = 0;
        for (String filePath: fileList) {
            File file = new File(filePath);
            File imageFile = file.getAbsoluteFile();
            String imageName = filePath.substring(filePath.lastIndexOf('/') + 1);

            //imageSize
            String fileSize = String.valueOf(file.length() / 1024);


            ExifInterface exif = null;
            try {
                exif = new ExifInterface(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }


            //createTime
            String createTime = exif.getAttribute(ExifInterface.TAG_DATETIME);

            //latitude
            String latitude = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);

            //longitude
            String longitude = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);

            //state
            String imageState = String.valueOf(DeviceStatus.state.WAITING);



            try {

                String imageId = UUID.randomUUID().toString();
                //store image in local database
                Image photo = new Image();
                photo.setImageId(imageId);
                photo.setImageName(imageName);
                photo.setImagePath(filePath);
                photo.setLongitude(longitude);
                photo.setLatitude(latitude);
                photo.setCreateTime(createTime);
                photo.setSize(fileSize);
                photo.setState(imageState);
                photo.setTaskId(taskId);
                photo.save();
                imageNum = imageNum + 1;

            } catch (Exception e) {
            }

        }
        return imageNum;
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
