package de.mpg.mpdl.labcam.LocalFragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import de.mpg.mpdl.labcam.AutoRun.dbObserver;
import de.mpg.mpdl.labcam.Gallery.AlbumRecyclerAdapter;
import de.mpg.mpdl.labcam.Gallery.LocalImageActivity;
import de.mpg.mpdl.labcam.Gallery.RemoteListDialogFragment;
import de.mpg.mpdl.labcam.Gallery.SectionedGridView.SectionedGridRecyclerViewAdapter;
import de.mpg.mpdl.labcam.Gallery.SectionedGridView.SimpleAdapter;
import de.mpg.mpdl.labcam.ItemDetails.DetailActivity;
import de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment.MicrophoneDialogFragment;
import de.mpg.mpdl.labcam.LocalFragment.DialogsInLocalFragment.NoteDialogFragment;
import de.mpg.mpdl.labcam.Model.Gallery;
import de.mpg.mpdl.labcam.Model.LocalModel.Image;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.TaskManager.ActiveTaskActivity;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.Utils.DeviceStatus;
import de.mpg.mpdl.labcam.Utils.ToastUtil;
import de.mpg.mpdl.labcam.Utils.UiElements.CircleProgressBar;

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
    private static TextView titleTaskTextView = null;
    private static TextView numActiveTextView = null;
    private static CircleProgressBar mCircleProgressBar = null;
    private static RelativeLayout activeTaskLayout = null;
    private static TextView percentTextView = null;

    android.support.v7.app.ActionBar actionBar;

    RecyclerView albumRecyclerView;
    RecyclerView recyclerView;
    SharedPreferences preferences;

    AlbumRecyclerAdapter adapter;
    SectionedGridRecyclerViewAdapter mSectionedAdapter;
    SimpleAdapter simpleAdapter;

    android.support.v7.view.ActionMode.Callback ActionModeCallback = this;

    //
    final ArrayList<Gallery> folders = new ArrayList<Gallery>();

    /**
     * imageList store all images date and path
     * treeMap auto sort it by date (prepare data for timeline view)
     * <imageDate,imagePath> **/
    TreeMap<Long, String> imageList = new TreeMap<Long, String>();
    ArrayList<String> sortedImageNameList;
    /**
     *
     * store the imagePathList of each album
     */
    ArrayList<List<String[]>> imagePathListAllAlbums = new ArrayList<>();

    //user info
    private SharedPreferences mPrefs;
    private String username;
    private String userId;
    private String serverName;

    //UI flag(timeLine/Album)
    private static TextView dateLabel = null;
    private static TextView albumLabel = null;
    private boolean isAlbum = false;

    private OnFragmentInteractionListener mListener;


    // db observer handler
    static ContentResolver resolver;
    static Handler mHandler;
    static de.mpg.mpdl.labcam.AutoRun.dbObserver dbObserver;
    static Uri uri;

    public LocalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        userId = mPrefs.getString("userId","");
        serverName = mPrefs.getString("server","");



    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_local, container, false);
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

                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putBoolean("isAlbum", false).apply();
                mEditor.commit();
                isAlbum = false;

                dateLabel.setTextColor(getResources().getColor(R.color.primary));
                albumLabel.setTextColor(getResources().getColor(R.color.no_focus_primary));
                albumRecyclerView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        albumLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (actionMode != null) {
                    // finish actionMode when switch
                    actionMode.finish();
                }

                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putBoolean("isAlbum", true).apply();
                mEditor.commit();
                isAlbum = true;

                dateLabel.setTextColor(getResources().getColor(R.color.no_focus_primary));
                albumLabel.setTextColor(getResources().getColor(R.color.primary));
                albumRecyclerView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
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
        dbObserver = new dbObserver(getActivity(),mHandler);
        resolver.registerContentObserver(uri, true, dbObserver);

        return rootView;

    }

    private void renderStatusBar(){
        {

            List<Task> activeTasks = DBConnector.getActiveTasks(userId, serverName);  // not finished
            List<Task> waitingTasks = DBConnector.getUserWaitingTasks(userId, serverName); // waiting
            List<Task> stoppedTasks = DBConnector.getUserStoppedTasks(userId, serverName); // stopped
            Task mostRecentTask = DBConnector.getLatestFinishedTask(userId, serverName);  // last task

            int num_activate = 0;

            num_activate = activeTasks.size();  // waiting, stop, failed tasks

            if(num_activate > 0){
                // remove always waiting auTask
                Task auTask = DBConnector.getAuTask(userId,serverName);
                if(auTask!=null && String.valueOf(DeviceStatus.state.WAITING).equalsIgnoreCase(auTask.getState()) &&  auTask.getTotalItems()==auTask.getFinishedItems())
                    num_activate --;
            }
            if(num_activate == 0)
            {
                if(mostRecentTask!=null && !DeviceStatus.twoDateWithinSecounds(DeviceStatus.longToDate(mostRecentTask.getEndDate()), DeviceStatus.longToDate(DeviceStatus.dateNow()))){
                    activeTaskLayout.setVisibility(View.GONE);
                    return;
                }
                //execute the task
                numActiveTextView.setText("0");

                percentTextView.setText(100+"%");
                mCircleProgressBar.setProgress(100);

                new Handler().postDelayed(new Runnable() {

                    public void run() {
                        Log.e(LOG_TAG, "no task  is null");
                        activeTaskLayout.setVisibility(View.GONE);
                        return;

                    }

                }, 1000);
            }

            for(Task task:waitingTasks){
                Log.e(LOG_TAG,"waitingTasks~~~~~~~");
                Log.e(LOG_TAG,"mode:"+task.getUploadMode());
                Log.e(LOG_TAG,"state:"+task.getState());
                Log.e(LOG_TAG,"getFinishedItems:"+task.getFinishedItems());
                Log.e(LOG_TAG,"getTotalItems:"+task.getTotalItems());
            }
            for(Task task:stoppedTasks){
                Log.e(LOG_TAG,"stoppedTasks~~~~~~~");
                Log.e(LOG_TAG,"mode:"+task.getUploadMode());
                Log.e(LOG_TAG,"state:"+task.getState());
                Log.e(LOG_TAG,"getFinishedItems:"+task.getFinishedItems());
                Log.e(LOG_TAG,"getTotalItems:"+task.getTotalItems());
            }


            if(waitingTasks.size()>0){
                activeTaskLayout.setVisibility(View.VISIBLE);
                Task task = waitingTasks.get(0);

                //
                if(task.getTotalItems()==0){
                    return;
                }
//                        String titleTaskInfo = task.getTotalItems() + " selected photo(s) uploading to " + task.getCollectionName();


                if(task.getUploadMode().equalsIgnoreCase("AU")){
                    // AU
                    titleTaskTextView.setText("Automatic upload to " + task.getCollectionName());
                }else {
                    titleTaskTextView.setText(task.getTotalItems() + " selected photo(s) uploading to " + task.getCollectionName());
                }

                numActiveTextView.setText(num_activate+"");

                int percent = (task.getFinishedItems()*100)/task.getTotalItems();
                percentTextView.setText(percent+"%");
                mCircleProgressBar.setProgress(percent);
            }else if(stoppedTasks.size()>0){
                activeTaskLayout.setVisibility(View.VISIBLE);
                Task task = stoppedTasks.get(0);

                //
                if(task.getTotalItems()==0){
                    return;
                }
                titleTaskTextView.setText(task.getTotalItems() + " selected photo(s) uploading to " + task.getCollectionName());
                numActiveTextView.setText(num_activate+"");

                int percent = (task.getFinishedItems()*100)/task.getTotalItems();
                percentTextView.setText(percent+"%");
                mCircleProgressBar.setProgress(percent);
            }
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
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
        if (positionSet.size() == 0) {
            // if none is selected, quit choose mode
            Log.e(LOG_TAG, "addOrRemove() is called");
            actionMode.finish();
        } else {
            // set action mode tile
            actionMode.setTitle(positionSet.size() + " selected photos");
        }
    }


    private void addOrRemoveAlbum(int position) {
        if (albumPositionSet.contains(position)) {
            // if contain, remove
            albumPositionSet.remove(position);
        } else {
            // if not contain, add
            albumPositionSet.add(position);
        }
        if (albumPositionSet.size() == 0) {
            // if none is selected, quit choose mode
            Log.e(LOG_TAG,"addOrRemoveAlbum() is called");
            actionMode.finish();
        } else {
            // set action mode tile
            actionMode.setTitle(albumPositionSet.size() + " selected albums");
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
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    /**
     * Local Gallery Grid
     * get folder name folder path
     * set adapter for grid
     * handle onclick
     */
    private void loadLocalGallery(){

        ArrayList<Gallery> imageFolders = new ArrayList<Gallery>();

//        imageFolders = new ArrayList<Gallery>(new LinkedHashSet<Gallery>(folders));
        adapter = new AlbumRecyclerAdapter(getActivity(), imagePathListAllAlbums );
        for (List<String[]> imagePathListAllAlbum : imagePathListAllAlbums) {
            Log.i(LOG_TAG, "gallery size:"+ imagePathListAllAlbum.size());
        }

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        albumRecyclerView.setLayoutManager(llm);
        albumRecyclerView.setAdapter(adapter);
    }


    private void prepareData(){
        folders.clear();
        imagePathListAllAlbums.clear();
        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME
                ,MediaStore.Images.Media.DATA
                , MediaStore.Images.Media.DATE_TAKEN

        };
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        preferences = getActivity().getSharedPreferences("folder", Context.MODE_PRIVATE);

        Cursor cur = getActivity().getContentResolver().query(images, albums,null,null,null);

        imageList.clear();
        /*
            Listing out all the image folders(Galleries) in the file system.
         */
        if (cur.moveToFirst()) {

            int albumLocation = cur.getColumnIndex(MediaStore.Images.
                    Media.BUCKET_DISPLAY_NAME);
            int nameColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATA);
            int dateColumn = cur.getColumnIndex(
                    MediaStore.Images.Media.DATE_TAKEN);

            /**
             * albumName store the albumName of last image
             * compare albumName to detect album changes
             * imagePathListForEachAlbum is created for each album to store image path
             */
            List<String> albumNames = new ArrayList<>();
//            HashMap<String,String> imagePathListForEachAlbum = new HashMap<>();
            do {
                /** for timeline view **/
                //get all image and date
                Long imageDate =  cur.getLong(dateColumn);
//                Date d = new Date(imageDate);
                String imagePath = cur.getString(nameColumn);
                imageList.put(imageDate,imagePath);

                /** for gallery view **/
                /**
                 * imagePathListByAlbum
                 * create List<String> imagePathList for each Album
                 * need to be same order with folder
                 */

                // existing code, need to rewrite gallery view
                Gallery album = new Gallery();
                album.setGalleryName(cur.getString(albumLocation));

                String currentAlbum = cur.getString(albumLocation);
                if(!albumNames.contains(currentAlbum)) {
                    // new album
                    folders.add(album);
                    albumNames.add(currentAlbum);

                    List<String[]> imagePathListForCurrentAlbum = new ArrayList<>();
                    // add picture to current album
                    String[] imageStrArray = new String[2];
                    imageStrArray[0] = currentAlbum;
                    imageStrArray[1] = cur.getString(nameColumn);
                    imagePathListForCurrentAlbum.add(imageStrArray);
                    // add current album hash map
                    imagePathListAllAlbums.add(imagePathListForCurrentAlbum);

                }else {
                    for (List<String[]> albumList :imagePathListAllAlbums){
                        // if exist, get first imageStrArray, compare
                        if(albumList.get(0)[0].equalsIgnoreCase(currentAlbum)){
                            boolean isDuplicate = false;
                            for (String[] strings : albumList) {
                                if (strings[1] == cur.getString(nameColumn)){
                                    isDuplicate = true;
                                    break;
                                }
                            }

                            if(!isDuplicate) {
                                String[] imageStrArray = new String[2];
                                imageStrArray[0] = currentAlbum;
                                imageStrArray[1] = cur.getString(nameColumn);
                                albumList.add(imageStrArray);
                            }
                        }
                    }
                }
            } while (cur.moveToNext());
        }

        //try close cursor here
        cur.close();

//        imagePathListAllAlbums.size();

    }

    //load timeLinePicture
    private void loadTimeLinePicture(){
        // Your RecyclerView.Adapter
        // sortedImageNameList is imagePath list
        ArrayList<String> ImageNameList = new ArrayList<>();
        sortedImageNameList = new ArrayList<>();
        sortedImageNameList.clear();
        // sectionMap key is the date, value is the picture number
        List<String> dateAseList = new ArrayList<String>();
        List<String> dateList = new ArrayList<String>();
        HashMap<String,Integer> sectionMap = new HashMap<String,Integer>();

        for(Map.Entry<Long,String> entry: imageList.entrySet()){
            Date d = new Date(entry.getKey());
            SimpleDateFormat dt = new SimpleDateFormat(" dd.MM.yyyy");
            String dateStr = dt.format(d);
            dateAseList.add(dateStr);
            ImageNameList.add(entry.getValue());
//            Log.v(LOG_TAG,dateStr+" -> "+entry.getValue());
        }


        // treeMap order is ASE, need DSE, so reverse
        for(int i=dateAseList.size()-1;i>=0;i--){
                dateList.add(dateAseList.get(i));
        }

        for(int i=ImageNameList.size()-1;i>=0;i--){
            sortedImageNameList.add(ImageNameList.get(i));
        }

        Log.e(LOG_TAG,sortedImageNameList.size()+"");

         simpleAdapter = new SimpleAdapter(getActivity(),sortedImageNameList);
        if(positionSet!=null){
            simpleAdapter.setPositionSet(positionSet);
        }


        //This is the code to provide a sectioned grid
        List<SectionedGridRecyclerViewAdapter.Section> sections =
                new ArrayList<SectionedGridRecyclerViewAdapter.Section>();


        // section init
        String preStr = "";
        int count = 0;
//        int imgPosition = 0;
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
                SectionedGridRecyclerViewAdapter(getActivity(),R.layout.header_grid_section,R.id.section_text,recyclerView,simpleAdapter);
        mSectionedAdapter.setSections(sections.toArray(dummy));

        //Apply this adapter to the RecyclerView
        recyclerView.setAdapter(mSectionedAdapter);


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
                    actionMode = ((AppCompatActivity) getActivity()).startSupportActionMode(ActionModeCallback);
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
        recyclerView = (RecyclerView) rootView.findViewById(R.id.gallery_recycleView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
    }

    /**upload methods**/
     /*
            upload the selected files
        */
    private void uploadList(List<String> fileList) {
        String currentTaskId = createTask(fileList);

        remoteListNewInstance(currentTaskId).show(getActivity().getFragmentManager(), "remoteListDialog");
    }

    public static RemoteListDialogFragment remoteListNewInstance(String taskId)
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
        task.setSeverName(serverName);
        task.setStartDate(String.valueOf(now));
        task.save();
        int num = addImages(fileList, task.getTaskId()).size();
        task.setTotalItems(num);
        task.save();
        Log.v(LOG_TAG,"MU task"+task.getTaskId() );
        Log.v(LOG_TAG, "setTotalItems:" + num);

        return task.getTaskId();
    }

    private static List<Image> addImages(List<String> fileList, String taskId){

        List<Image> imageList = new ArrayList<>();

        for (String filePath: fileList) {
            File file = new File(filePath);
            File imageFile = file.getAbsoluteFile();
            String imageName = filePath.substring(filePath.lastIndexOf('/') + 1);
            Image image = DBConnector.getImageByPath(filePath);
            if(image!=null){  // image already exist
                imageList.add(image);
                continue;
            }

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
            imageList.add(photo);
        }
        return imageList;
    }

    /**** take notes ****/
    public static NoteDialogFragment noteDialogNewInstance(ArrayList<String> imagePathList)
    {
        NoteDialogFragment noteDialogFragment = new NoteDialogFragment();
        Bundle args = new Bundle();

        Log.d("LY", "size: "+imagePathList.size());
        List<Image> list = addImages(imagePathList, "");  // task id set empty, init images

        String[] imagePathArray = new String[imagePathList.size()];  // fragment to fragment can only pass Array
        for(int i=0; i<imagePathList.size(); i++){
            imagePathArray[i] = imagePathList.get(i);
        }

        noteDialogFragment.setArguments(args);    // pass imagePathArray to NoteDialogFragment
        args.putStringArray("imagePathArray", imagePathArray);
        return noteDialogFragment;
    }

    public void showNoteDialog(ArrayList<String> imagePathList){
        noteDialogNewInstance(imagePathList).show(getActivity().getFragmentManager(), "noteDialogFragment");
    }

    /**** record voice ****/
    public static MicrophoneDialogFragment voiceDialogNewInstance(ArrayList<String> imagePathList)
    {
        MicrophoneDialogFragment microphoneDialogFragment = new MicrophoneDialogFragment();

//        ImageGroup imageGroup = new ImageGroup(String.valueOf(UUID.randomUUID()),imagePathList);
        Log.d("LY", "size: "+imagePathList.size());

        Bundle args = new Bundle();
        args.putStringArrayList("imagePathList", imagePathList);
//        args.putParcelable("imageList", imageGroup);

        microphoneDialogFragment.setArguments(args);
        return microphoneDialogFragment;
    }

    public void showVoiceDialog(ArrayList<String> imagePathList){
        voiceDialogNewInstance(imagePathList).show(getActivity().getFragmentManager(), "voiceDialogFragment");
    }

    @Override
    public void onResume() {
        resolver.registerContentObserver(uri, true, dbObserver);

        /** remember the date/timeLine option **/
        isAlbum = mPrefs.getBoolean("isAlbum",isAlbum);

        renderTimeLine();
        loadTimeLinePicture();

        if(isAlbum) {
            dateLabel.setTextColor(getResources().getColor(R.color.lightGrey));
            albumLabel.setTextColor(getResources().getColor(R.color.primary));
            albumRecyclerView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
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
        resolver.unregisterContentObserver(dbObserver);

//        prepareData();
        checkPermission();
//        loadTimeLinePicture();
        simpleAdapter.notifyDataSetChanged();
        mSectionedAdapter.notifyDataSetChanged();

        super.onPause();

    }

    private void batchOperation(int operationType){
        if(positionSet.size()!=0) {
            Log.v(LOG_TAG, " "+positionSet.size());
            ArrayList imagePathList = new ArrayList();
            for (Integer i : positionSet) {
                imagePathList.add(sortedImageNameList.get(i));
            }

            if (imagePathList != null) {
                switch (operationType){
                    case R.id.item_upload_local:
                        uploadList(imagePathList);
                        break;
                    case R.id.item_microphone_local:
                        showVoiceDialog(imagePathList);
                        break;
                    case R.id.item_notes_local:
                        showNoteDialog(imagePathList);
                        break;
                }
            }
            imagePathList.clear();

        }else if(albumPositionSet.size()!=0){
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
            if (imagePathListForAlbumTask != null) {
                switch (operationType){
                    case R.id.item_upload_local:
                        uploadList(imagePathListForAlbumTask);
                        break;
                    case R.id.item_microphone_local:
                        showVoiceDialog(imagePathListForAlbumTask);
                        break;
                    case R.id.item_notes_local:
                        showNoteDialog(imagePathListForAlbumTask);
                        break;
                }
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
                ToastUtil.showShortToast(getActivity(), "please grant CAMERA and WRITE_EXTERNAL_STORAGE permissions");
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

}
