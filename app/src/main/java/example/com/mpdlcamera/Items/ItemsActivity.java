package example.com.mpdlcamera.Items;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.activeandroid.ActiveAndroid;

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.DataItem;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Retrofit.RetrofitClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by allen on 03/09/15.
 */
public class ItemsActivity extends Activity {


    private List<DataItem> dataList = new ArrayList<DataItem>();
    public  ItemsGridAdapter adapter;
    private GridView gridView;
    private View rootView;
    private String dataCollectionId;
    private Activity activity = this;
    private final String LOG_TAG = ItemsActivity.class.getSimpleName();
    SharedPreferences mPrefs;
    private String username;
    private String password;

    Callback<List<DataItem>> callbackItems = new Callback<List<DataItem>>() {
        @Override
        public void success(List<DataItem> dataList, Response response) {
            //load all data from imeji
            //adapter =  new CustomListAdapter(getActivity(), dataList);
            List<DataItem> dataListLocal = new ArrayList<DataItem>();

            ActiveAndroid.beginTransaction();
            try {
                // here get the string of Metadata Json
                for (DataItem item : dataList) {
                    dataListLocal.add(item);
                    //item.save();
                }
                ActiveAndroid.setTransactionSuccessful();
            } finally{
                ActiveAndroid.endTransaction();

                adapter =  new ItemsGridAdapter(activity, dataListLocal);
                gridView.setAdapter(adapter);

                adapter.notifyDataSetChanged();

            }

        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, error.toString());
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_grid_view);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        password = mPrefs.getString("password", "");

        Intent intent = activity.getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            dataCollectionId = intent.getStringExtra(Intent.EXTRA_TEXT);
            getFolderItems(dataCollectionId);


            //adapter =  new CustomListAdapter(getActivity(), dataList);
            adapter = new ItemsGridAdapter(activity, dataList);


            //rootView = inflater.inflate(R.layout.fragment_section_list_swipe, container, false);
            gridView = (GridView) findViewById(R.id.item_gridView);
            //listView = (SwipeMenuListView) rootView.findViewById(R.id.listView);
            gridView.setAdapter(adapter);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                    DataItem dataItem = (DataItem) adapter.getItem(position);
//                    int duration = Toast.LENGTH_SHORT;
//                    Toast toast = Toast.makeText(activity, dataItem.getCollectionId(), duration);
//                    toast.show();

                    Intent showDetailIntent = new Intent(activity, DetailActivity.class);
                    showDetailIntent.putExtra("itemPath", dataItem.getFileUrl());
                    startActivity(showDetailIntent);
                }
            });
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

    private void getFolderItems(String collectionId){
        RetrofitClient.getCollectionItems(collectionId, callbackItems, username, password);
    }


}
