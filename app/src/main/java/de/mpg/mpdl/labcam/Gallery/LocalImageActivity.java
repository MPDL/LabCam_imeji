package de.mpg.mpdl.labcam.Gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import de.mpg.mpdl.labcam.Gallery.LocalAlbum.LocalAlbumAdapter;
import de.mpg.mpdl.labcam.ItemDetails.DetailActivity;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.Utils.ImageFileFilter;

/**
 * Created by allen on 03/09/15.
 * it is the album pictures view (for a single album)
 */
public class LocalImageActivity extends AppCompatActivity implements android.support.v7.view.ActionMode.Callback {

    private ArrayList<String> dataPathList = new ArrayList<String>();

    public LocalAlbumAdapter localAlbumAdapter;

//    public  ImagesGridAdapter adapter;
    private RecyclerView recyclerView;
    private View rootView;
    private Activity activity = this;
    private final String LOG_TAG = LocalImageActivity.class.getSimpleName();
    private SharedPreferences mPrefs;
    private String username;
    private String userId;


    //actionMode
    private android.support.v7.view.ActionMode actionMode;

    android.support.v7.view.ActionMode.Callback ActionModeCallback = this;
    public Set<Integer> positionSet = new HashSet<>();

    private Toolbar toolbar;
    private String folderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.active_gallery_gridview);

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);


        TextView titleView = (TextView) findViewById(R.id.title);

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

        // replace with album
        localAlbumAdapter = new LocalAlbumAdapter(activity,dataPathList);

        recyclerView = (RecyclerView) findViewById(R.id.album_detail_recycle_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(activity, 2));
        recyclerView.setAdapter(localAlbumAdapter);

        localAlbumAdapter.setOnItemClickListener(new LocalAlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (actionMode != null) {
                    // 如果当前处于多选状态，则进入多选状态的逻辑
                    // 维护当前已选的position
                    addOrRemove(position);
                    localAlbumAdapter.setPositionSet(positionSet);
                } else {
                    // 如果不是多选状态，则进入点击事件的业务逻辑
                    //  show picture
                    boolean isLocalImage = true;
                    Intent showDetailIntent = new Intent(activity, DetailActivity.class);
                    showDetailIntent.putStringArrayListExtra("itemPathList", dataPathList);
                    showDetailIntent.putExtra("positionInList",position);
                    showDetailIntent.putExtra("isLocalImage", isLocalImage);
                    startActivity(showDetailIntent);

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (actionMode == null) {
                    actionMode = ((AppCompatActivity) activity).startSupportActionMode(ActionModeCallback);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            // create
            if(actionMode==null){
                actionMode = ((AppCompatActivity) activity).startSupportActionMode(ActionModeCallback);
                for(int i = 0;i<dataPathList.size();i++){
                    positionSet.add(i);
                }
                if (positionSet.size() == 0) {
                    // 如果没有选中任何的item，则退出多选模式
                    Log.e(LOG_TAG, "addOrRemove() is called");
                    actionMode.finish();
                } else {
                    // 设置ActionMode标题
                    actionMode.setTitle(positionSet.size() + " selected photos");
                    // 更新列表界面，否则无法显示已选的item
                }
                localAlbumAdapter.setPositionSet(positionSet);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** add or remove image in upload task **/
    private void addOrRemove(int position) {
        if (positionSet.contains(position)) {
            // 如果包含，则撤销选择
            positionSet.remove(position);
        } else {
            // 如果不包含，则添加
            positionSet.add(position);
        }
        if (positionSet.size() == 0) {
            // 如果没有选中任何的item，则退出多选模式
            Log.e(LOG_TAG, "addOrRemove() is called");
            actionMode.finish();
        } else {
            // 设置ActionMode标题
            actionMode.setTitle(positionSet.size() + " selected photos");
            // 更新列表界面，否则无法显示已选的item
        }
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

        mPrefs = activity.getSharedPreferences("myPref", 0);
        String currentTaskId = "";

        currentTaskId = createTask(fileList);

        newInstance(currentTaskId).show(getFragmentManager(), "remoteListDialog");

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

    @Override
    public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
        if (actionMode == null) {
            actionMode = mode;
            toolbar.setVisibility(View.GONE);
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.contextual_menu_local, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onPrepareActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(android.support.v7.view.ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_upload_local:

                Log.v(LOG_TAG, "upload");

                if (positionSet.size() != 0) {
                    Log.v(LOG_TAG, " " + positionSet.size());
                    List uploadPathList = new ArrayList();
                    for (Integer i : positionSet) {
                        uploadPathList.add(dataPathList.get(i));
                    }

                    if (uploadPathList != null) {
                        uploadList(uploadPathList);
                    }
                    uploadPathList.clear();

                }
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
        actionMode = null;

        // clear selection Sets
        positionSet.clear();

        toolbar.setVisibility(View.VISIBLE);
        localAlbumAdapter.notifyDataSetChanged();
        Log.e(LOG_TAG,"onDestroyActionMode");
    }
}