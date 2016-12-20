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

import de.mpg.mpdl.labcam.Model.LocalModel.Voice;
import de.mpg.mpdl.labcam.R;

/**
 * Created by Yunqing on 20.12.16.
 */



public class RecentVoiceActivity extends AppCompatActivity {

    Activity activity = this;
    RecentVoiceAdapter recentVoiceAdapter = null;
    private static final String TAG = "RecentVoiceActivity";

    //ui elements
    private static ListView recentVoiceListView;
    private static TextView norecentVoiceView;
    List<Voice> voiceList =new Select().from(Voice.class)
            .execute();

    //user info
    private String userId;
    private SharedPreferences mPrefs;
    private String serverName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_voice_notes);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");
        serverName = mPrefs.getString("server","");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        norecentVoiceView = (TextView) findViewById(R.id.tv_no_recent_voice);
        recentVoiceListView = (ListView) findViewById(R.id.listView_recent_voice);
        recentVoiceAdapter = new RecentVoiceAdapter(activity, voiceList);
        recentVoiceAdapter.notifyDataSetChanged();
        recentVoiceListView.setAdapter(recentVoiceAdapter);

        //Display either "no recent upload" or the listview of uploaded tasks
        if(voiceList.size() == 0) {
            norecentVoiceView.setVisibility(View.VISIBLE);
            recentVoiceListView.setVisibility(View.GONE);
        }else {
            norecentVoiceView.setVisibility(View.GONE);
            recentVoiceListView.setVisibility(View.VISIBLE);
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
