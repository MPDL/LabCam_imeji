package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.sch.rfview.AnimRFRecyclerView;
import com.sch.rfview.decoration.DividerGridItemDecoration;
import com.sch.rfview.manager.AnimRFGridLayoutManager;

import de.mpg.mpdl.labcam.code.common.fragment.RemoteListDialogFragment;
import de.mpg.mpdl.labcam.code.common.adapter.LocalAlbumAdapter;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.utils.ImageFileFilter;
import de.mpg.mpdl.labcam.code.base.BaseCompatActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;

import static de.mpg.mpdl.labcam.code.utils.BatchOperationUtils.noteDialogNewInstance;
import static de.mpg.mpdl.labcam.code.utils.BatchOperationUtils.voiceDialogNewInstance;

/**
 * Created by allen on 03/09/15.
 * it is the album pictures view (for a single album)
 */
public class LocalImageActivity extends BaseCompatActivity implements android.support.v7.view.ActionMode.Callback {


    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.title)
    TextView titleView;

    private final String LOG_TAG = LocalImageActivity.class.getSimpleName();

    private String folderPath;
    private int dataCounter = 6; // initialize this value as 6, in order to correct display items

    private ArrayList<String> itemPathList = new ArrayList<String>();
    private ArrayList<String> datas = new ArrayList<>();
    public Set<Integer> positionSet = new HashSet<>();

    private Activity activity = this;

    private View rootView;
    private View headerView;
    private View footerView;

    //actionMode
    private android.support.v7.view.ActionMode actionMode;
    android.support.v7.view.ActionMode.Callback ActionModeCallback = this;
    private AnimRFRecyclerView recyclerView;
    public LocalAlbumAdapter localAlbumAdapter;

    private Handler mHandler = new Handler();

    @Override
    protected int getLayoutId() {
        return R.layout.active_gallery_gridview;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        rootView = findViewById(android.R.id.content);
        // load more and refresh
        headerView = LayoutInflater.from(activity).inflate(R.layout.header_view, null);
        footerView = LayoutInflater.from(activity).inflate(R.layout.footer_view, null);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Kiran's title
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

        //final String CompleteCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + title;
        File folder = new File(folderPath);
        //listing all the files
        File[] folderFiles = folder.listFiles();

        Log.v("camera folder",folderPath);

        for (File imageFile : folderFiles) {
            Log.v("file",imageFile.toURI().toString() );

            if(new ImageFileFilter(imageFile).accept(imageFile)) {
                //filtering img files
                itemPathList.add(imageFile.getAbsolutePath());
            }
        }

        int size = 6;
        if(itemPathList.size()<=6){
            size = itemPathList.size();
        }
        for (int i = 0; i < size; i++) {
            datas.add(itemPathList.get(i));
        }

        // replace with album
        localAlbumAdapter = new LocalAlbumAdapter(activity, datas);

        recyclerView = (AnimRFRecyclerView) findViewById(R.id.album_detail_recycle_view);
        recyclerView.setLayoutManager(new AnimRFGridLayoutManager(activity, 2));
        recyclerView.addItemDecoration(new DividerGridItemDecoration(activity, true));
        recyclerView.addFootView(footerView);
        recyclerView.setColor(Color.BLUE, Color.GREEN);
        recyclerView.setHeaderImageDurationMillis(300);
        recyclerView.setHeaderImageMinAlpha(0.6f);
        recyclerView.setLoadDataListener(new AnimRFRecyclerView.LoadDataListener() {
            @Override
            public void onRefresh() {
                new Thread(new MyRunnable(true)).start();

                Log.e("~~", "onRefresh()");
            }

            @Override
            public void onLoadMore() {
                new Thread(new MyRunnable(false)).start();
            }
        });

        recyclerView.setRefreshEnable(false);

        recyclerView.setAdapter(localAlbumAdapter);

        localAlbumAdapter.setOnItemClickListener(new LocalAlbumAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (actionMode != null) {
                    addOrRemove(position);
                    localAlbumAdapter.setPositionSet(positionSet);
                } else {
                    //  show picture
                    boolean isLocalImage = true;
                    Intent showDetailIntent = new Intent(activity, DetailActivity.class);
                    showDetailIntent.putStringArrayListExtra("itemPathList", itemPathList);
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
                for(int i = 0; i<itemPathList.size(); i++){
                    positionSet.add(i);
                }
                if (positionSet.size() == 0) {
                    Log.e(LOG_TAG, "addOrRemove() is called");
                    actionMode.finish();
                } else {
                    actionMode.setTitle(positionSet.size() + " selected photos");
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
            positionSet.remove(position);
        } else {
            positionSet.add(position);
        }
        if (positionSet.size() == 0) {
            Log.e(LOG_TAG, "addOrRemove() is called");
            actionMode.finish();
        } else {
            actionMode.setTitle(positionSet.size() + " selected photos");
        }
    }


    public static RemoteListDialogFragment newInstance(String[] imagePathArray)
    {
        RemoteListDialogFragment remoteListDialogFragment = new RemoteListDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray("imagePathArray", imagePathArray);
        remoteListDialogFragment.setArguments(args);
        return remoteListDialogFragment;
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
                Log.i(LOG_TAG, "upload");
                batchOperation(R.id.item_upload_local);
                mode.finish();
                return true;
            case R.id.item_microphone_local:
                Log.i(LOG_TAG, "microphone");
                batchOperation(R.id.item_microphone_local);
                mode.finish();
                return true;
            case R.id.item_notes_local:
                Log.i(LOG_TAG, "notes");
                batchOperation(R.id.item_notes_local);
                mode.finish();
                return true;
            default:
                return false;
        }
    }

    private void batchOperation(int operationType){
        if(positionSet.size()!=0) {
            Log.v(LOG_TAG, " "+positionSet.size());
            List imagePathList = new ArrayList();
            //TODO: Question why convert itemPathList to imagePathList
            for (Integer i : positionSet) {
                imagePathList.add(itemPathList.get(i));
            }
            if (imagePathList != null) {
                String[] imagePathArray = (String[]) imagePathList.toArray(new String[imagePathList.size()]);
                switch (operationType){
                    case R.id.item_upload_local:
                        uploadList(imagePathArray);
                        break;
                    case R.id.item_microphone_local:
                        showVoiceDialog(imagePathArray);
                        break;
                    case R.id.item_notes_local:
                        showNoteDialog(imagePathArray);
                        break;
                }
            }
            imagePathList.clear();
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

    public void refreshComplete() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void loadMoreComplete() {
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    /**
     * 添加数据
     */
    private void addData() {
        if (datas == null) {
            datas = new ArrayList<>();
            dataCounter = 0;
        }

        for (int i = 0; i < 6; i++) {
            if(datas.size() >=  itemPathList.size()){
                return;
            }
            datas.add(itemPathList.get(dataCounter));
            dataCounter = dataCounter +1;
        }
    }

    public void newData() {
        datas.clear();
        for (int i = 0; i < 6; i++) {
            datas.add(itemPathList.get(i));
        }
    }

    class MyRunnable implements Runnable {

        boolean isRefresh;

        public MyRunnable(boolean isRefresh) {
            this.isRefresh = isRefresh;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (isRefresh) {
                        newData();
                        Log.e(LOG_TAG, "refreshComplete:"+datas.size());
                        refreshComplete();
                        recyclerView.refreshComplate();
                    } else {
                        addData();
                        Log.e(LOG_TAG, "loadMoreComplete:"+ datas.size());
                        loadMoreComplete();
                        recyclerView.loadMoreComplate();
                    }
                }
            });
        }
    }


    /**upload methods**/
     /*
            upload the selected files
        */
    private void uploadList(String[] imagePathArray) {
        newInstance( imagePathArray).show(this.getFragmentManager(), "remoteListDialog");
    }

    public void showNoteDialog(String[] imagePathArray){
        noteDialogNewInstance(imagePathArray).show(getFragmentManager(), "noteDialogFragment");
    }

    public void showVoiceDialog(String[] imagePathArray){
        voiceDialogNewInstance(imagePathArray).show(getFragmentManager(), "voiceDialogFragment");
    }
}
