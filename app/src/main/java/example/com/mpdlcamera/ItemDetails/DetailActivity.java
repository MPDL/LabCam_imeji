package example.com.mpdlcamera.ItemDetails;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.media.ExifInterface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import example.com.mpdlcamera.Gallery.RemoteListDialogFragment;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Settings.SettingsActivity;
import example.com.mpdlcamera.Utils.DeviceStatus;
import uk.co.senab.photoview.PhotoViewAttacher;


public class DetailActivity extends AppCompatActivity implements android.support.v7.view.ActionMode.Callback{
    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    private Activity activity = this;
    private View rootView;
    private List<String> itemPathList;
    private PhotoViewAttacher mAttacher;

    // viewPager
    private ViewPager viewPager;
    private  ViewPagerAdapter viewPagerAdapter;

    // positionSet
    public Set<Integer> positionSet = new HashSet<>();

    private android.support.v7.view.ActionMode actionMode;
    android.support.v7.view.ActionMode.Callback ActionModeCallback = this;

    //user info
    private SharedPreferences mPrefs;
    private String username;
    private String userId;
    private ImageView uploadCurrentImageView =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mPrefs = this.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        userId = mPrefs.getString("userId","");

        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            itemPathList = extras.getStringArrayList("itemPathList");
            boolean isLocalImage = extras.getBoolean("isLocalImage");
            int positionInList = extras.getInt("positionInList");

            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            Display display = windowManager.getDefaultDisplay();

            Point size = new Point();
            display.getSize(size);

            viewPager = (ViewPager) rootView.findViewById(R.id.view_pager_detail_image);
            viewPagerAdapter = new ViewPagerAdapter(this,size,isLocalImage,itemPathList);
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setCurrentItem(positionInList);
            if(isLocalImage) {
                viewPagerAdapter.setOnItemClickListener(new ViewPagerAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(actionMode != null) {
                            addOrRemove(position);
                            viewPagerAdapter.setPositionSet(positionSet);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {
                        if (actionMode == null) {
                            uploadCurrentImageView.setVisibility(View.GONE);
                            actionMode = ((AppCompatActivity) activity).startSupportActionMode(ActionModeCallback);
                        }
                    }
                });
            }else {
                viewPagerAdapter.setOnItemClickListener(null);
            }

            // upload current image
            uploadCurrentImageView = (ImageView) findViewById(R.id.icon_upload);

            // hide upload if it is remote image
            if(isLocalImage){
                uploadCurrentImageView.setVisibility(View.VISIBLE);
            }else {
                uploadCurrentImageView.setVisibility(View.GONE);
            }

            uploadCurrentImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // create list for reuse code upload list
                    List<String> currentImageList = new ArrayList<>();
                    String currentImageUrl = itemPathList.get(viewPager.getCurrentItem());
                    currentImageList.add(currentImageUrl);

                    uploadList(currentImageList);
                }
            });

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent showSettingIntent = new Intent(this, SettingsActivity.class);
            startActivity(showSettingIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
        if (actionMode == null) {
            actionMode = mode;
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

                Log.v(LOG_TAG, " "+positionSet.size());
                List uploadPathList = new ArrayList();
                for(Integer i:positionSet){
                    uploadPathList.add(itemPathList.get(i));
                }

                if(uploadPathList != null) {
                    uploadList(uploadPathList);
                }
                uploadPathList.clear();
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
        actionMode = null;
        uploadCurrentImageView.setVisibility(View.VISIBLE);
        positionSet.clear();
        viewPagerAdapter.notifyDataSetChanged();
    }



    /**upload methods**/
     /*
            upload the selected files
        */
    private void uploadList(List<String> fileList) {
        String currentTaskId = createTask(fileList);

        newInstance(currentTaskId).show(this.getFragmentManager(), "remoteListDialog");
    }

    public static RemoteListDialogFragment newInstance(String taskId)
    {
        RemoteListDialogFragment remoteListDialogFragment = new RemoteListDialogFragment();
        Bundle args = new Bundle();
        args.putString("taskId", taskId);
        remoteListDialogFragment.setArguments(args);
        return remoteListDialogFragment;
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
        Log.v(LOG_TAG,"MU task"+task.getTaskId() );
        Log.v(LOG_TAG, "setTotalItems:" + num);

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
            actionMode.finish();
            uploadCurrentImageView.setVisibility(View.VISIBLE);
        } else {
            // 设置ActionMode标题
            actionMode.setTitle(positionSet.size() + " selected");
            // 更新列表界面，否则无法显示已选的item
            viewPagerAdapter.notifyDataSetChanged();
//            mSectionedAdapter.notifyDataSetChanged();
        }
    }



}