package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.ImageFileFilter;

/**
 * Created by allen on 03/09/15.
 */
public class LocalImageActivity extends AppCompatActivity {

    private List<String> dataPathList = new ArrayList<String>();
    public  ImagesGridAdapter adapter;
    private GridView gridView;
    private View rootView;
    private String dataCollectionId;
    private Activity activity = this;
    private final String LOG_TAG = LocalImageActivity.class.getSimpleName();
    private SharedPreferences mPrefs;
    private String username;
    private String password;

    Toolbar toolbar;
    String title;
    AbsListView.MultiChoiceModeListener mMultiChoiceModeListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.images_grid_view);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");

        Intent intent = activity.getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            dataCollectionId = intent.getStringExtra(Intent.EXTRA_TEXT);
            String title = intent.getStringExtra("galleryTitle");
            titleView.setText(title);
        }


        String[] albums = new String[]{MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        for(int i = 0; i<albums.length ;i++){
            Log.v("albums",  albums[i]);
        }

        Log.v("Images",  images.toString());



        //TODO here fetch the image URIs by given CameraDirectory name(title)
        title="Camera";
        File CameraDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
        File[] files = CameraDirectory.listFiles();

        //files are all the image folders
//        10-21 12:36:25.493  21930-21930/example.com.mpdlcamera V/Images﹕ content://media/external/images/media
//        10-21 12:36:25.493  21930-21930/example.com.mpdlcamera V/file﹕ file:/storage/emulated/0/DCIM/Camera/
//        10-21 12:36:25.503  21930-21930/example.com.mpdlcamera V/file﹕ file:/storage/emulated/0/DCIM/.thumbnails/
//        10-21 12:36:25.503  21930-21930/example.com.mpdlcamera V/file﹕ file:/storage/emulated/0/DCIM/Screenshots/

//        for (File CurFile : files) {
//            if (CurFile.isDirectory()) {
//                CameraDirectory=CurFile;
//                break;
//            }
//        }


        final String CompleteCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + title;
        File folder = new File(CompleteCameraFolder);
        File[] folderFiles = folder.listFiles();

        Log.v("camera folder",CompleteCameraFolder);

        for (File imageFile : folderFiles) {
            Log.v("file",imageFile.toURI().toString() );

            if(new ImageFileFilter(imageFile).accept(imageFile)) {
                dataPathList.add(imageFile.getAbsolutePath());
            }
        }

        adapter = new ImagesGridAdapter(activity, dataPathList);


        //rootView = inflater.inflate(R.layout.fragment_section_list_swipe, container, false);
        gridView = (GridView) findViewById(R.id.image_gridView);
        //listView = (SwipeMenuListView) rootView.findViewById(R.id.listView);
        gridView.setAdapter(adapter);
        //registerForContextMenu(gridView);

        gridView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            private int nr = 0;

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
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // TODO Auto-generated method stub
                nr = 0;

                toolbar.setVisibility(View.GONE);
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.contextual_menu, menu);
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                // TODO Auto-generated method stub
                switch (item.getItemId()) {

                    case R.id.item_delete:
                        nr = 0;
                        adapter.clearSelection();
                        mode.finish();
                    case R.id.item_upload:
                        //TODO handle uploading logic
//                        nr = 0;
//                        adapter.clearSelection();
                        mode.finish();
                }

                return false;
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode,
                                                  int position,
                                                  long id,
                                                  boolean checked) {
                // TODO Auto-generated method stub
                if (checked) {
                    nr++;
                    adapter.setNewSelection(position, checked);
                } else {
                    nr--;
                    adapter.removeSelection(position);
                }
                mode.setTitle(nr + " selected");

            }
        });

        gridView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);


        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           int position, long arg3) {
                // TODO Auto-generated method stub

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
            Log.v(LOG_TAG,"slected");
            return true;
        }
        if (id == R.id.item_delete) {
            Log.v(LOG_TAG,"delete");

            return true;
        }

        if (id == R.id.item_upload) {
            Log.v(LOG_TAG,"upload");

            return true;
        }
        return super.onOptionsItemSelected(item);
    }









    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.setHeaderTitle("Are you sure to delete?");
        AdapterView.AdapterContextMenuInfo cmi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.add(1, cmi.position, 0, "Delete");
        menu.add(2, cmi.position, 0, "Cancel");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals("Delete")){
//            new Delete().from(DataItem.class).
//                    where("filename = ?", dataList.get(item.getItemId()).getFilename()).execute();
//            dataList.remove(item.getItemId());
//            adapter.notifyDataSetChanged();
            Log.v("", String.valueOf(item.getItemId()));
        }
        else if(item.getTitle().equals("Cancel")){
            Log.v("", String.valueOf(item.getItemId()));
        }
        else {
            return false;

        }
        // Return false to allow normal context menu processing to proceed,
        //        true to consume it here.
        return true;
    }


}
