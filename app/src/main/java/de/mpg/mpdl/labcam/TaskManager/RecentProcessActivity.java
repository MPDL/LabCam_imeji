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

import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.Utils.DBConnector;

import java.util.ArrayList;
import java.util.List;

public class RecentProcessActivity extends AppCompatActivity {

    Activity activity = this;
    RecentTaskAdapter recentTaskAdapter = null;

    //ui elements
    private static ListView recentTaskListView;
    private static TextView norecentTaskView;
    List<Task> taskList =new ArrayList<>();

    //user info
    private String userId;
    private SharedPreferences mPrefs;
    private String serverName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_process);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");
        serverName = mPrefs.getString("server","");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //taskManager listView
        taskList = DBConnector.getRecentTasks(userId, serverName);
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();

        norecentTaskView = (TextView) findViewById(R.id.tv_no_recent_task);
        recentTaskListView = (ListView) findViewById(R.id.listView_recent_task);
        recentTaskAdapter = new RecentTaskAdapter(activity, taskList);
        recentTaskAdapter.notifyDataSetChanged();
        recentTaskListView.setAdapter(recentTaskAdapter);

        //Display either "no recent upload" or the listview of uploaded tasks
        if(taskList.size() == 0) {
            norecentTaskView.setVisibility(View.VISIBLE);
            recentTaskListView.setVisibility(View.GONE);
        }else {
            norecentTaskView.setVisibility(View.GONE);
            recentTaskListView.setVisibility(View.VISIBLE);
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
