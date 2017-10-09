package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.base.BaseCompatActivity;
import de.mpg.mpdl.labcam.code.common.adapter.ViewPagerAdapter;
import de.mpg.mpdl.labcam.code.common.fragment.RemoteListDialogFragment;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.rxbus.EventSubscriber;
import de.mpg.mpdl.labcam.code.rxbus.RxBus;
import de.mpg.mpdl.labcam.code.rxbus.event.NoteRefreshEvent;
import de.mpg.mpdl.labcam.code.rxbus.event.VoiceRefreshEvent;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;
import rx.Subscription;

import static de.mpg.mpdl.labcam.code.utils.BatchOperationUtils.noteDialogNewInstance;
import static de.mpg.mpdl.labcam.code.utils.BatchOperationUtils.voiceDialogNewInstance;


public class DetailActivity extends BaseCompatActivity implements android.support.v7.view.ActionMode.Callback{

    @BindView(R.id.view_pager_detail_image)
    ViewPager viewPager;

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();
    private Activity activity = this;
    private List<String> itemPathList;
    private List<String> completeList;
    private ViewPagerAdapter viewPagerAdapter;
    boolean isLocalImage;
    int positionInList;
    public Set<Integer> positionSet = new HashSet<>();
    private android.support.v7.view.ActionMode actionMode;
    android.support.v7.view.ActionMode.Callback ActionModeCallback = this;

    //user info
    private String serverName;
    private String userId;

    private Subscription mNoteRefreshEventSub;
    private Subscription mVoiceRefreshEventSub;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_detail;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {

        observeNoteRefresh();
        observeVoiceRefresh();

        userId = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.USER_ID, "");
        serverName = PreferenceUtil.getString(this, Constants.SHARED_PREFERENCES, Constants.SERVER_NAME, "");

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            itemPathList = extras.getStringArrayList("itemPathList");
            completeList = extras.getStringArrayList("completeList");
            isLocalImage = extras.getBoolean("isLocalImage");
            positionInList = extras.getInt("positionInList");

            forceRefresh();
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

    @Override
    public void onDestroyActionMode(android.support.v7.view.ActionMode mode) {
        actionMode = null;
        positionSet.clear();
        viewPagerAdapter.notifyDataSetChanged();
    }



    /**upload methods**/
     /*
            upload the selected files
        */
    private void uploadList(String[] imagePathArray) {

        newInstance(imagePathArray).show(this.getFragmentManager(), "remoteListDialog");
    }

    public static RemoteListDialogFragment newInstance(String[] imagePathArray)
    {
        RemoteListDialogFragment remoteListDialogFragment = new RemoteListDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray("imagePathArray", imagePathArray);
        remoteListDialogFragment.setArguments(args);
        return remoteListDialogFragment;
    }



    private void addOrRemove(int position) {

        if (positionSet.contains(position)) {

            positionSet.remove(position);
        } else {

            positionSet.add(position);
        }
        if (positionSet.size() == 0) {

            actionMode.finish();
        } else {

            actionMode.setTitle(positionSet.size() + " selected");

            viewPagerAdapter.notifyDataSetChanged();

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
    public void showVoiceDialog(String[] imagePathArray){
        voiceDialogNewInstance(imagePathArray).show(this.getFragmentManager(), "voiceDialogFragment");
    }

    public void showNoteDialog(String[] imagePathArray){
        noteDialogNewInstance(imagePathArray).show(this.getFragmentManager(),"noteDialogFragment");
    }

    private void observeNoteRefresh() {
        mNoteRefreshEventSub = RxBus.getDefault()
                .observe(NoteRefreshEvent.class)
                .subscribe(new EventSubscriber<NoteRefreshEvent>() {
                    @Override
                    public void onEvent(NoteRefreshEvent event) {
                        String imgPath = event.getImgPath();
                        if(itemPathList.contains(imgPath)){
                            positionInList = itemPathList.indexOf(imgPath);
                        }
                        forceRefresh();
                    }
                });
    }

    private void observeVoiceRefresh() {
        mVoiceRefreshEventSub = RxBus.getDefault()
                .observe(VoiceRefreshEvent.class)
                .subscribe(new EventSubscriber<VoiceRefreshEvent>() {
                    @Override
                    public void onEvent(VoiceRefreshEvent event) {
                        String imgPath = event.getImgPath();
                        if(itemPathList.contains(imgPath)){
                            positionInList = itemPathList.indexOf(imgPath);
                        }
                       forceRefresh();
                    }
                });
    }

    private void forceRefresh(){
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        Point size = new Point();
        display.getSize(size);

        ViewPagerAdapter.OnLoadMoreDetailListener onLoadMoreDetailListener = new ViewPagerAdapter.OnLoadMoreDetailListener() {
            @Override
            public void onLoadMore(int position) {
                if(completeList==null || completeList.size()==0){
                    return;
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int index = position;
                        int end = index+3<=completeList.size()?
                                index+3 : completeList.size();

                        viewPagerAdapter.setDataSet(completeList.subList(0,end));
                        viewPager.setCurrentItem(index+1);
                        viewPagerAdapter.setLoaded();
                    }
                }, 500);
            }
        };

        viewPagerAdapter = new ViewPagerAdapter(this,size,isLocalImage,itemPathList, onLoadMoreDetailListener, userId, serverName);
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
                        actionMode = ((AppCompatActivity) activity).startSupportActionMode(ActionModeCallback);
                    }
                }
            });
        }else {
            viewPagerAdapter.setOnItemClickListener(null);
        }
    }
}