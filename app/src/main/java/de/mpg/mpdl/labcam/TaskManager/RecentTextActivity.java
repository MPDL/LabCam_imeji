package de.mpg.mpdl.labcam.TaskManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.util.List;
import android.util.Log;

import de.mpg.mpdl.labcam.Model.LocalModel.Note;
import de.mpg.mpdl.labcam.R;

/**
 * Created by Yunqing on 19.12.16.
 */


public class RecentTextActivity extends AppCompatActivity {

    Activity activity = this;
    RecentTextAdapter recentTextAdapter = null;
    private static final String TAG = "RecentTextActivity";

    //ui elements
    private static ListView recentTextListView;
    private static TextView norecentTextView;
    List<Note> noteList =new Select().from(Note.class)
                        .execute();

    //user info
    private String userId;
    private SharedPreferences mPrefs;
    private String serverName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_text_notes);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");
        serverName = mPrefs.getString("server","");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        norecentTextView = (TextView) findViewById(R.id.tv_no_recent_text);
        recentTextListView = (ListView) findViewById(R.id.listView_recent_text_notes);
        recentTextAdapter = new RecentTextAdapter(activity, noteList);
        recentTextAdapter.notifyDataSetChanged();
        recentTextListView.setAdapter(recentTextAdapter);

        //Display either "no recent upload" or the listview of uploaded tasks
        if(noteList.size() == 0) {
            norecentTextView.setVisibility(View.VISIBLE);
            recentTextListView.setVisibility(View.GONE);
        }else {
            norecentTextView.setVisibility(View.GONE);
            recentTextListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
