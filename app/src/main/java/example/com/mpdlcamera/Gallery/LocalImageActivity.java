package example.com.mpdlcamera.Gallery;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.R;

/**
 * Created by allen on 03/09/15.
 */
public class LocalImageActivity extends AppCompatActivity {

    private List<DataItem> dataList = new ArrayList<DataItem>();
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


            //TODO here fetch the image URIs by given CameraDirectory name(title)
            title="Camera";
            File CameraDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString());
            File[] files = CameraDirectory.listFiles();
//            for (File CurFile : files) {
//                if (CurFile.isDirectory()) {
//                    CameraDirectory=CurFile.getName();
//                    break;
//                }
//            }
            final String CompleteCameraFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString() + "/" + title;



            //TODO pass it to the dataList<String>
            //adapter =  new CustomListAdapter(getActivity(), dataList);
            adapter = new ImagesGridAdapter(activity, dataList);


            //rootView = inflater.inflate(R.layout.fragment_section_list_swipe, container, false);
            gridView = (GridView) findViewById(R.id.image_gridView);
            //listView = (SwipeMenuListView) rootView.findViewById(R.id.listView);
            gridView.setAdapter(adapter);

//            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                @Override
//                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
//                    DataItem dataItem = (DataItem) adapter.getItem(position);
//
//                    Intent showDetailIntent = new Intent(activity, DetailActivity.class);
//                    showDetailIntent.putExtra("itemPath", dataItem.getFileUrl());
//                    startActivity(showDetailIntent);
//                }
//
//            });
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
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
