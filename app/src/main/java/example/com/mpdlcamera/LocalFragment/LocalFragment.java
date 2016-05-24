package example.com.mpdlcamera.LocalFragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Point;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

import example.com.mpdlcamera.Gallery.GalleryListAdapter;
import example.com.mpdlcamera.Gallery.LocalImageActivity;
import example.com.mpdlcamera.Gallery.RemoteListDialogFragment;
import example.com.mpdlcamera.Gallery.SectionedGridView.SectionedGridRecyclerViewAdapter;
import example.com.mpdlcamera.Gallery.SectionedGridView.SimpleAdapter;
import example.com.mpdlcamera.ItemDetails.DetailActivity;
import example.com.mpdlcamera.Model.Gallery;
import example.com.mpdlcamera.Model.LocalModel.Image;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;

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
    android.support.v7.app.ActionBar actionBar;

    GridView gridView;
    RecyclerView recyclerView;
    SharedPreferences preferences;

    GalleryListAdapter adapter;
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

    //UI flag(timeLine/Album)
    private static TextView dateLabel = null;
    private static TextView albumLabel = null;
    private boolean isAlbum = false;

    private OnFragmentInteractionListener mListener;

    public LocalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        userId = mPrefs.getString("userId","");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_local, container, false);
        // folder gridView
        gridView = (GridView) rootView.findViewById(R.id.gallery_gridView);

        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        renderTimeLine();

        //prepare local gallery image data
        prepareData();

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
                if(actionMode != null){
                    // finish actionMode when switch
                    actionMode.finish();
                }

                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putBoolean("isAlbum",false).apply();
                mEditor.commit();
                isAlbum = false;

                dateLabel.setTextColor(getResources().getColor(R.color.primary));
                albumLabel.setTextColor(getResources().getColor(R.color.lightGrey));
                gridView.setVisibility(View.GONE);
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
                mEditor.putBoolean("isAlbum",true).apply();
                mEditor.commit();
                isAlbum = true;

                dateLabel.setTextColor(getResources().getColor(R.color.lightGrey));
                albumLabel.setTextColor(getResources().getColor(R.color.primary));
                gridView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });


        return rootView;

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

                Log.v(LOG_TAG, "upload");

                if(positionSet.size()!=0) {
                    Log.v(LOG_TAG, " "+positionSet.size());
                    List uploadPathList = new ArrayList();
                    for (Integer i : positionSet) {
                        uploadPathList.add(sortedImageNameList.get(i));
                    }

                    if (uploadPathList != null) {
                        uploadList(uploadPathList);
                    }
                    uploadPathList.clear();

                }else if(albumPositionSet.size()!=0){
                    Toast.makeText(getActivity(),"albums upload",Toast.LENGTH_SHORT).show();
                    // upload albums
                    List<String> imagePathListForAlbumTask = new ArrayList<>();
                    // add selected albums
                    for (Integer i: albumPositionSet){
                        // add path by album
                            for(String[] imageStrArray: imagePathListAllAlbums.get(i)){
                            imagePathListForAlbumTask.add(imageStrArray[1]);
                        }
                    }
                    Log.e(LOG_TAG,"imagePathListForAlbumTask: " +imagePathListForAlbumTask.size());
                    uploadList(imagePathListForAlbumTask);
                    imagePathListForAlbumTask.clear();
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
        albumPositionSet.clear();
        positionSet.clear();

        actionBar.show();
        simpleAdapter.notifyDataSetChanged();
        mSectionedAdapter.notifyDataSetChanged();
        Log.e(LOG_TAG,"onDestroyActionMode");
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
            Log.e(LOG_TAG, "addOrRemove() is called");
            actionMode.finish();
        } else {
            // 设置ActionMode标题
            actionMode.setTitle(positionSet.size() + " selected photos");
            // 更新列表界面，否则无法显示已选的item
//            simpleAdapter.notifyDataSetChanged();
//            mSectionedAdapter.notifyDataSetChanged();
        }
    }


    private void addOrRemoveAlbum(int position) {
        if (albumPositionSet.contains(position)) {
            // 如果包含，则撤销选择
            albumPositionSet.remove(position);
        } else {
            // 如果不包含，则添加
            albumPositionSet.add(position);
        }
        if (albumPositionSet.size() == 0) {
            // 如果没有选中任何的item，则退出多选模式
            Log.e(LOG_TAG,"addOrRemoveAlbum() is called");
            actionMode.finish();
        } else {
            // 设置ActionMode标题
            actionMode.setTitle(albumPositionSet.size() + " selected albums");
            // 更新列表界面，否则无法显示已选的item
//            simpleAdapter.notifyDataSetChanged();
//            mSectionedAdapter.notifyDataSetChanged();
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
        imageFolders = new ArrayList<Gallery>(new LinkedHashSet<Gallery>(folders));

        adapter = new GalleryListAdapter(getActivity(), imageFolders );

        gridView.setAdapter(adapter);

        adapter.setOnItemClickListener(new GalleryListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                if (actionMode != null) {
                    // 如果当前处于多选状态，则进入多选状态的逻辑
                    // 维护当前已选的position
                    addOrRemoveAlbum(position);
                    adapter.setPositionSet(albumPositionSet);
                } else {
                    // 如果不是多选状态，则进入点击事件的业务逻辑
                    //  show picture
                    //  go to album detail
                    Gallery gallery = (Gallery) adapter.getItem(position);
                    Intent galleryImagesIntent = new Intent(getActivity(), LocalImageActivity.class);
                    galleryImagesIntent.putExtra("galleryTitle", gallery.getGalleryPath());

                    startActivity(galleryImagesIntent);
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
                    folders.add(album); // adapter rewrite later
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
                            String[] imageStrArray = new String[2];
                            imageStrArray[0] = currentAlbum;
                            imageStrArray[1] = cur.getString(nameColumn);
                            albumList.add(imageStrArray);
                        }
                    }
                }
            } while (cur.moveToNext());
        }

        //try close cursor here
        cur.close();
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

        newInstance(currentTaskId).show(getActivity().getFragmentManager(), "remoteListDialog");
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
        }
        return imageNum;
    }

    @Override
    public void onResume() {
        super.onResume();
        /** remember the date/timeLine option **/
        isAlbum = mPrefs.getBoolean("isAlbum",isAlbum);

        renderTimeLine();
        loadTimeLinePicture();

        if(isAlbum) {
            dateLabel.setTextColor(getResources().getColor(R.color.lightGrey));
            albumLabel.setTextColor(getResources().getColor(R.color.primary));
            gridView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
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
        super.onPause();
        prepareData();
//        loadTimeLinePicture();
        simpleAdapter.notifyDataSetChanged();
        mSectionedAdapter.notifyDataSetChanged();
    }
}
