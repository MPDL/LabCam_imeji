package de.mpg.mpdl.labcam.code.common.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.activity.ActiveTaskActivity;
import de.mpg.mpdl.labcam.code.activity.DetailActivity;
import de.mpg.mpdl.labcam.code.common.adapter.AlbumRecyclerAdapter;
import de.mpg.mpdl.labcam.code.common.adapter.SectionedGridRecyclerViewAdapter;
import de.mpg.mpdl.labcam.code.common.adapter.SimpleAdapter;
import de.mpg.mpdl.labcam.code.common.observer.DatabaseObserver;
import de.mpg.mpdl.labcam.code.common.widget.CircleProgressBar;
import de.mpg.mpdl.labcam.code.common.widget.Constants;
import de.mpg.mpdl.labcam.code.common.widget.DBConnector;
import de.mpg.mpdl.labcam.code.rxbus.EventSubscriber;
import de.mpg.mpdl.labcam.code.rxbus.RxBus;
import de.mpg.mpdl.labcam.code.rxbus.event.NoteRefreshEvent;
import de.mpg.mpdl.labcam.code.rxbus.event.VoiceRefreshEvent;
import de.mpg.mpdl.labcam.code.utils.DeviceStatus;
import de.mpg.mpdl.labcam.code.utils.PreferenceUtil;
import de.mpg.mpdl.labcam.code.utils.ToastUtils;
import rx.Subscription;

import static de.mpg.mpdl.labcam.code.utils.BatchOperationUtils.noteDialogNewInstance;
import static de.mpg.mpdl.labcam.code.utils.BatchOperationUtils.voiceDialogNewInstance;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link LocalFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class LocalFragment extends Fragment implements android.support.v7.view.ActionMode.Callback{

    private static final String LOG_TAG = LocalFragment.class.getSimpleName();
    private android.support.v7.view.ActionMode actionMode;
    public Set<Integer> positionSet = new HashSet<>();
    public Set<Integer> albumPositionSet = new HashSet<>();
    private View rootView;

    //ui elements
    //active task bar
    private TextView titleTaskTextView = null;
    private TextView numActiveTextView = null;
    private CircleProgressBar mCircleProgressBar = null;
    private RelativeLayout activeTaskLayout = null;
    private TextView percentTextView = null;

    android.support.v7.app.ActionBar actionBar;

    RecyclerView albumRecyclerView;
    RecyclerView dateRecyclerView;

    AlbumRecyclerAdapter adapter;
    SectionedGridRecyclerViewAdapter mSectionedAdapter;
    SimpleAdapter simpleAdapter;

    android.support.v7.view.ActionMode.Callback actionModeCallback = this;

    ArrayList<String> sortedImageNameList;
    ArrayList<List<String[]>> imagePathListAllAlbums = new ArrayList<>();
    List<String> albumNameArrayList = new ArrayList<>();
    List<String> imageNameArrayList = new ArrayList<>();
    List<Long> imageDateArrayList = new ArrayList<>();

    private String userId;
    private String serverName;

    private TextView dateLabel = null;
    private TextView albumLabel = null;
    private boolean isAlbum = false;

    private OnFragmentInteractionListener mListener;


    // db observer handler
    static ContentResolver resolver;
    static Handler mHandler;
    static DatabaseObserver databaseObserver;
    static Uri uri;

    private Subscription mNoteRefreshEventSub;
    private Subscription mVoiceRefreshEventSub;

    public LocalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userId = PreferenceUtil.getString(getActivity(), Constants.SHARED_PREFERENCES, Constants.USER_ID,"");
        serverName = PreferenceUtil.getString(getActivity(), Constants.SHARED_PREFERENCES, Constants.SERVER_NAME,"");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_local, container, false);

        observeNoteRefresh();
        observeVoiceRefresh();

        // folder gridView
        albumRecyclerView = (RecyclerView) rootView.findViewById(R.id.gallery_gridView);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        renderTimeLine();

        //prepare local gallery image data
        checkPermission();    // check SD permission first

        //set grid adapter
        loadLocalGallery();

        //set header recycleView adapter
        loadTimeLinePicture();

        //switch
        dateLabel = (TextView) rootView.findViewById(R.id.label_date);
        albumLabel = (TextView) rootView.findViewById(R.id.label_album);

        dateLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMode != null) {
                    // finish actionMode when switch
                    actionMode.finish();
                }
                PreferenceUtil.setBoolean(getActivity(), Constants.SHARED_PREFERENCES, Constants.IS_ALBUM, false);
                isAlbum = false;

                dateLabel.setTextColor(getResources().getColor(R.color.primary));
                albumLabel.setTextColor(getResources().getColor(R.color.no_focus_primary));
                albumRecyclerView.setVisibility(View.GONE);
                dateRecyclerView.setVisibility(View.VISIBLE);
            }
        });

        albumLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMode != null) {
                    // finish actionMode when switch
                    actionMode.finish();
                }
                PreferenceUtil.setBoolean(getActivity(), Constants.SHARED_PREFERENCES, Constants.IS_ALBUM, true);
                isAlbum = true;

                dateLabel.setTextColor(getResources().getColor(R.color.no_focus_primary));
                albumLabel.setTextColor(getResources().getColor(R.color.primary));
                albumRecyclerView.setVisibility(View.VISIBLE);
                dateRecyclerView.setVisibility(View.GONE);
            }
        });

        ImageView arrow = (ImageView) rootView.findViewById(R.id.im_right_arrow);
        arrow.setRotation(-90);

        titleTaskTextView = (TextView) rootView.findViewById(R.id.tv_title_task_info);
        percentTextView = (TextView) rootView.findViewById(R.id.tv_percent);
        numActiveTextView = (TextView) rootView.findViewById(R.id.tv_num_active_task);
        mCircleProgressBar = (CircleProgressBar) rootView.findViewById(R.id.circleProgressBar);


        activeTaskLayout = (RelativeLayout) rootView.findViewById(R.id.layout_active_task);
        activeTaskLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent activeTaskIntent = new Intent(getActivity(), ActiveTaskActivity.class);
                startActivity(activeTaskIntent);
            }
        });
        activeTaskLayout.setVisibility(View.GONE);

        /** handler observer **/
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                if(msg.what==1234){
                    renderStatusBar();
                }

            }
        };

        uri = Uri.parse("content://de.mpg.mpdl.labcam/tasks");
        resolver = getActivity().getContentResolver();
        databaseObserver = new DatabaseObserver(getActivity(),mHandler);
        resolver.registerContentObserver(uri, true, databaseObserver);

        return rootView;

    }

    private void renderStatusBar(){
        List<Task> activeTasks = DBConnector.getActiveTasks(userId, serverName);  // not finished
        List<Task> waitingTasks = DBConnector.getUserWaitingTasks(userId, serverName); // waiting
        List<Task> stoppedTasks = DBConnector.getUserStoppedTasks(userId, serverName); // stopped
        Task mostRecentTask = DBConnector.getLatestFinishedTask(userId, serverName);  // last task

        int numActivate;

        numActivate = activeTasks.size();  // waiting, stop, failed tasks

        if(numActivate > 0){
            // remove always waiting auTask
            Task auTask = DBConnector.getAuTask(userId,serverName);
            if(auTask!=null && String.valueOf(DeviceStatus.state.WAITING).equalsIgnoreCase(auTask.getState()) &&  auTask.getTotalItems()==auTask.getFinishedItems())
                numActivate --;
        }
        if(numActivate == 0)
        {
            if(mostRecentTask!=null && !DeviceStatus.twoDateWithinSecounds(DeviceStatus.longToDate(mostRecentTask.getEndDate()), DeviceStatus.longToDate(DeviceStatus.dateNow()))){
                activeTaskLayout.setVisibility(View.GONE);
                return;
            }
            //execute the task
            numActiveTextView.setText("0");

            percentTextView.setText(100+"%");
            mCircleProgressBar.setProgress(100);

            new Handler().postDelayed(() -> {
                Log.e(LOG_TAG, "no task  is null");
                activeTaskLayout.setVisibility(View.GONE);
                return;

            }, 1000);
        }

        if(!waitingTasks.isEmpty()){
            activeTaskLayout.setVisibility(View.VISIBLE);
            Task task = waitingTasks.get(0);

            //
            if(task.getTotalItems()==0){
                return;
            }

            if(task.getUploadMode().equalsIgnoreCase("AU")){
                // AU
                titleTaskTextView.setText("Automatic upload to " + task.getCollectionName());
            }else {
                titleTaskTextView.setText(task.getTotalItems() + " selected photo(s) uploading to " + task.getCollectionName());
            }

            numActiveTextView.setText(numActivate+"");

            int percent = (task.getFinishedItems()*100)/task.getTotalItems();
            percentTextView.setText(percent+"%");
            mCircleProgressBar.setProgress(percent);
        }else if(!stoppedTasks.isEmpty()){
            activeTaskLayout.setVisibility(View.VISIBLE);

            Task task = stoppedTasks.get(0);

            //
            if(task.getTotalItems()==0){
                return;
            }
            titleTaskTextView.setText(task.getTotalItems() + " selected photo(s) uploading to " + task.getCollectionName());
            numActiveTextView.setText(numActivate+"");

            int percent = (task.getFinishedItems()*100)/task.getTotalItems();
            percentTextView.setText(percent+"%");
            mCircleProgressBar.setProgress(percent);
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    @Override
    public boolean onCreateActionMode(android.support.v7.view.ActionMode mode, Menu menu) {
        if (actionMode == null) {
            actionMode = mode;
            actionBar.hide();
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

        // clear selection Sets
        albumPositionSet.clear();
        positionSet.clear();

        actionBar.show();
        simpleAdapter.notifyDataSetChanged();
        mSectionedAdapter.notifyDataSetChanged();
        Log.e(LOG_TAG,"onDestroyActionMode");
    }

    private void addOrRemove(int position) {
        if (positionSet.contains(position)) {
            // if contain, remove
            positionSet.remove(position);
        } else {
            // if not contain, add
            positionSet.add(position);
        }
        if (positionSet.isEmpty()) {
            // if none is selected, quit choose mode
            Log.e(LOG_TAG, "addOrRemove() is called");
            actionMode.finish();
        } else {
            // set action mode tile
            actionMode.setTitle(positionSet.size() + " selected photos");
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Local Gallery Grid
     * get folder name folder path
     * set adapter for grid
     * handle onclick
     */
    private void loadLocalGallery(){

        adapter = new AlbumRecyclerAdapter(getActivity(), imagePathListAllAlbums );
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        albumRecyclerView.setLayoutManager(llm);
        albumRecyclerView.setAdapter(adapter);
    }


    private void prepareData(){

        // create a cursor to retrieve information for all pictures
        imagePathListAllAlbums.clear();

        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                ,MediaStore.Images.Media.DATA
                , MediaStore.Images.Media.DATE_TAKEN

        };

        String sorting =  MediaStore.Images.Media.DATE_TAKEN + " DESC";
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Cursor cur = getActivity().getContentResolver().query(images, albums,null,null,sorting);


        //loop over the cursor, find all unique albums
        /*
            Listing out all the image folders(Galleries) in the file system.
         */
        if (cur.moveToFirst()) {

            int albumNameColumn = cur.getColumnIndex(MediaStore.Images.
                    Media.BUCKET_DISPLAY_NAME);
            int fileNameColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int fileDateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);

            /**
             * albumName store the albumName of last image
             * compare albumName to detect album changes
             * imagePathListForEachAlbum is created for each album to store image path
             */
            List<String> albumNames = new ArrayList<>();

            String tempAlbumName;

            imageNameArrayList.clear();
            albumNameArrayList.clear();
            imageDateArrayList.clear();
            do{
                imageNameArrayList.add(cur.getString(fileNameColumn));
                albumNameArrayList.add(cur.getString(albumNameColumn));
                imageDateArrayList.add(cur.getLong(fileDateColumn));
            }while (cur.moveToNext());

            albumNames.clear();
            // check unique albums and initialize the structure of imagePathListAllAlbums
            for (int i = 0; i < albumNameArrayList.size(); i++ ){
                tempAlbumName = albumNameArrayList.get(i);
                if(!albumNames.contains(tempAlbumName)) {
                    albumNames.add(tempAlbumName);
                    List<String[]> imagePathListForCurrentAlbum = new ArrayList<>();
                    // add current album hash map
                    imagePathListAllAlbums.add(imagePathListForCurrentAlbum);
                }
            }

            // searching for every album the corresponding index of files in the XXXArrayList
            for (int j = 0; j < albumNameArrayList.size(); j++ ){
                tempAlbumName = albumNameArrayList.get(j);
                for (int i = 0; i < albumNames.size() ; i++) {
                    if (albumNames.get(i).equalsIgnoreCase(tempAlbumName)) {
                        String[] imageStrArray = new String[2];
                        imageStrArray[0] = tempAlbumName;
                        imageStrArray[1] = imageNameArrayList.get(j);
                        imagePathListAllAlbums.get(i).add(imageStrArray);
                        break;
                    }
                }
            }
        }
        cur.close();
    }

    //load timeLinePicture
    private void loadTimeLinePicture(){
        // Your RecyclerView.Adapter
        sortedImageNameList = new ArrayList<>();
        sortedImageNameList.clear();
        // sectionMap key is the date, value is the picture number
        List<String> dateList = new ArrayList<>();

        for (int i = 0; i < imageNameArrayList.size(); i++){
            Date d = new Date(imageDateArrayList.get(i));
            SimpleDateFormat dt = new SimpleDateFormat(" dd.MM.yyyy");
            String dateStr = dt.format(d);
            dateList.add(dateStr);
            sortedImageNameList.add(imageNameArrayList.get(i));
        }

         simpleAdapter = new SimpleAdapter(getActivity(),sortedImageNameList, userId, serverName);
        if(positionSet!=null){
            simpleAdapter.setPositionSet(positionSet);
        }

        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections = new ArrayList<>();

        // section init
        String preStr = "";
        int count = 0;
        for(String dateStr:dateList){
            // count is img position
            if(preStr.equalsIgnoreCase(dateStr)){
                count++;
            }else {
             //init
                sections.add(new SectionedGridRecyclerViewAdapter.Section(count,dateStr));
                count++;
            }
            preStr = dateStr;
        }


        //Add your adapter to the sectionAdapter
        SectionedGridRecyclerViewAdapter.Section[] dummy = new SectionedGridRecyclerViewAdapter.Section[sections.size()];
        mSectionedAdapter = new
                SectionedGridRecyclerViewAdapter(getActivity(),R.layout.header_grid_section,R.id.section_text, dateRecyclerView,simpleAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        dateRecyclerView.setAdapter(mSectionedAdapter);


        simpleAdapter.setOnItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (actionMode != null) {
                    // 如果当前处于多选状态，则进入多选状态的逻辑
                    // 维护当前已选的position
                    addOrRemove(position);
                    simpleAdapter.setPositionSet(positionSet);
                } else {
                    // 如果不是多选状态，则进入点击事件的业务逻辑
                    //  show picture
                    boolean isLocalImage = true;
                    Intent showDetailIntent = new Intent(getActivity(), DetailActivity.class);
                    showDetailIntent.putStringArrayListExtra("itemPathList", sortedImageNameList);
                    showDetailIntent.putExtra("positionInList",position);
                    showDetailIntent.putExtra("isLocalImage", isLocalImage);
                    startActivity(showDetailIntent);

                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (actionMode == null) {
                    actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(actionModeCallback);
                }
            }
        });
    }

    private void renderTimeLine(){
        // get current screen width
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        // calculate spanCount in GridLayoutManager
        int spanCount;
        spanCount = width/235;

        // timeline recycleView
        dateRecyclerView = (RecyclerView) rootView.findViewById(R.id.gallery_recycleView);
        dateRecyclerView.setHasFixedSize(true);
        dateRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
    }

    /**upload methods**/
     /*
            upload the selected files
        */

    private void uploadList(String[] imagePathArray) {
        newInstance(imagePathArray).show(getActivity().getFragmentManager(), "remoteListDialog");
    }

    public static RemoteListDialogFragment newInstance(String[] imagePathArray) {

        RemoteListDialogFragment remoteListDialogFragment = new RemoteListDialogFragment();
        Bundle args = new Bundle();
        args.putStringArray("imagePathArray", imagePathArray);
        remoteListDialogFragment.setArguments(args);
        return remoteListDialogFragment;
    }


    public void showNoteDialog(String[] imagePathArray){
        noteDialogNewInstance(imagePathArray).show(getActivity().getFragmentManager(), "noteDialogFragment");
    }

    public void showVoiceDialog(String[] imagePathArray){
        voiceDialogNewInstance(imagePathArray).show(getActivity().getFragmentManager(), "voiceDialogFragment");
    }

    @Override
    public void onResume() {
        resolver.registerContentObserver(uri, true, databaseObserver);

        isAlbum = PreferenceUtil.getBoolean(getActivity(), Constants.SHARED_PREFERENCES, Constants.IS_ALBUM, false);

        renderTimeLine();

        checkPermission();   //prepare local gallery image data, check SD permission first

        loadLocalGallery();  //set grid adapter

        loadTimeLinePicture();  //set header recycleView adapter

        if(isAlbum) {
            dateLabel.setTextColor(getResources().getColor(R.color.lightGrey));
            albumLabel.setTextColor(getResources().getColor(R.color.primary));
            albumRecyclerView.setVisibility(View.VISIBLE);
            dateRecyclerView.setVisibility(View.GONE);
        }
        renderStatusBar();
        super.onResume();

    }

    /** screen orientation **/
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        renderTimeLine();
        loadTimeLinePicture();
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPause() {
        resolver.unregisterContentObserver(databaseObserver);

        checkPermission();
        loadTimeLinePicture();
        simpleAdapter.notifyDataSetChanged();
        mSectionedAdapter.notifyDataSetChanged();

        super.onPause();

    }

    private void batchOperation(int operationType){
        if(!positionSet.isEmpty()) {
            Log.v(LOG_TAG, " "+positionSet.size());
            List imagePathList = new ArrayList();
            for (Integer i : positionSet) {
                imagePathList.add(sortedImageNameList.get(i));
            }
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
                default:
                    break;
            }
            imagePathList.clear();

        }else if(!albumPositionSet.isEmpty()){
            Toast.makeText(getActivity(),"albums upload",Toast.LENGTH_SHORT).show();
            // upload albums
            ArrayList<String> imagePathListForAlbumTask = new ArrayList<>();
            // add selected albums
            for (Integer i: albumPositionSet){
                // add path by album
                for(String[] imageStrArray: imagePathListAllAlbums.get(i)){
                    imagePathListForAlbumTask.add(imageStrArray[1]);
                }
            }
            String[] imagePathArray = imagePathListForAlbumTask.toArray(new String[imagePathListForAlbumTask.size()]);

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
                default:
                    break;
                }
            imagePathListForAlbumTask.clear();
        }
    }

    /***********************************   permissions   ****************************************/

    private static final int CHECK_PERMISSION = 1;

    @TargetApi(Build.VERSION_CODES.M)
    private void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHECK_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CHECK_PERMISSION && grantResults.length >= 2) {
            int firstGrantResult = grantResults[0];
            int secondGrantResult = grantResults[1];
            boolean granted = (firstGrantResult == PackageManager.PERMISSION_GRANTED) && (secondGrantResult == PackageManager.PERMISSION_GRANTED);
            Log.i("permission", "onRequestPermissionsResult granted=" + granted);

            if(granted) {
                prepareData();
            }else{
                ToastUtils.showShortMessage(getActivity(), "please grant CAMERA and WRITE_EXTERNAL_STORAGE permissions");
            }
        }
    }

    /**
     * Open image intent
     */
    private void checkPermission() {
        // check permission for android > 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED ) {
                requestCameraPermission();

                return;
            }
        }
        prepareData();
    }

    private void observeNoteRefresh() {
        mNoteRefreshEventSub = RxBus.getDefault()
                .observe(NoteRefreshEvent.class)
                .subscribe(new EventSubscriber<NoteRefreshEvent>() {
                    @Override
                    public void onEvent(NoteRefreshEvent event) {
                        renderTimeLine();
                        loadTimeLinePicture();
                    }
                });
    }

    private void observeVoiceRefresh() {
        mVoiceRefreshEventSub = RxBus.getDefault()
                .observe(VoiceRefreshEvent.class)
                .subscribe(new EventSubscriber<VoiceRefreshEvent>() {
                    @Override
                    public void onEvent(VoiceRefreshEvent event) {
                        renderTimeLine();
                        loadTimeLinePicture();
                    }
                });
    }

}
