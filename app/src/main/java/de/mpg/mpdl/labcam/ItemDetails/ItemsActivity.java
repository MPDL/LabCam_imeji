package de.mpg.mpdl.labcam.ItemDetails;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.mpg.mpdl.labcam.Model.DataItem;
import de.mpg.mpdl.labcam.Model.MessageModel.ItemMessage;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Retrofit.RetrofitClient;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by allen on 03/09/15.
 *
 * itemsActivity is used for display items in imeji folder
 */
public class ItemsActivity extends AppCompatActivity {

    private final String LOG_TAG = ItemsActivity.class.getSimpleName();

    private List<DataItem> dataList = new ArrayList<DataItem>();
    private ArrayList<String> imagePathList = new ArrayList<>();

    private ServerFolderItemsAdapter serverFolderItemsAdapter;

    private View rootView;
    private String dataCollectionId;
    private Activity activity = this;

    SharedPreferences mPrefs;
    private String username;
    private String APIkey;

    //pagination
    private int paging = 0;


    //UI element
    static RecyclerView recyclerView;
    Toolbar toolbar;

    Callback<ItemMessage> callback_Items = new Callback<ItemMessage>() {
        @Override
        public void success(ItemMessage itemMessage, Response response) {
            List<DataItem> dataList = new ArrayList<>();

            //load all data from imeji
            List<DataItem> dataListLocal = new ArrayList<DataItem>();
            dataListLocal = itemMessage.getResults();

            if(paging == 0){
            imagePathList.clear();
            }
            // Deserialize API response and then construct new objects to append to the adapter
            // Add the new objects to the data source for the adapter
            for (DataItem item : dataListLocal) {
                imagePathList.add(item.getWebResolutionUrlUrl());
            }

            if(paging == 0){
                serverFolderItemsAdapter.notifyDataSetChanged();
            }else {
                // For efficiency purposes, notify the adapter of only the elements that got changed
                // curSize will equal to the index of the first element inserted because the list is 0-indexed
                int curSize = serverFolderItemsAdapter.getItemCount();
                serverFolderItemsAdapter.notifyItemRangeInserted(curSize, imagePathList.size() - 1);

            }
        }

        @Override
        public void failure(RetrofitError error) {
            Log.v(LOG_TAG, "get DataItem failed");
            Log.v(LOG_TAG, error.toString());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_grid_view);
        rootView = getWindow().getDecorView().findViewById(android.R.id.content);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView titleView = (TextView) findViewById(R.id.title);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        username = mPrefs.getString("username", "");
        APIkey = mPrefs.getString("apiKey", "");

        Intent intent = activity.getIntent();
        if (intent != null && intent.hasExtra(Intent.EXTRA_TEXT)) {
            dataCollectionId = intent.getStringExtra(Intent.EXTRA_TEXT);
            String title = intent.getStringExtra("folderTitle");

            titleView.setText(title);

            recyclerView = (RecyclerView)findViewById(R.id.server_folder_detail_recycle_view);
            serverFolderItemsAdapter = new ServerFolderItemsAdapter(activity,imagePathList);
            recyclerView.setHasFixedSize(true);
            GridLayoutManager gridLayoutManager = new GridLayoutManager(activity,2);
            recyclerView.setLayoutManager(gridLayoutManager);
            recyclerView.setAdapter(serverFolderItemsAdapter);
            recyclerView.setOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount) {
                    paging = 1;
                    customLoadMoreDataFromApi(page*10);
                }
            });

            getFolderItems(dataCollectionId, 0);

        }
    }

    // Append more data into the adapter
    // This method probably sends out a network request and appends new data items to your adapter.
    public void customLoadMoreDataFromApi(int offset) {
        // Send an API request to retrieve appropriate data using the offset value as a parameter.
        getFolderItems(dataCollectionId,offset);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getFolderItems(String collectionId,int offset){
        RetrofitClient.getCollectionItems(collectionId, offset, callback_Items, APIkey);
    }

}
