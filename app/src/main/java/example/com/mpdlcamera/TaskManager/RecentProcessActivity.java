package example.com.mpdlcamera.TaskManager;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;

import com.activeandroid.query.Select;

import java.util.ArrayList;
import java.util.List;

import example.com.mpdlcamera.Model.LocalModel.Settings;
import example.com.mpdlcamera.Model.LocalModel.Task;
import example.com.mpdlcamera.R;
import example.com.mpdlcamera.Utils.DeviceStatus;

public class RecentProcessActivity extends AppCompatActivity {

    Activity activity = this;
    RecentTaskAdapter recentTaskAdapter = null;

    //ui elements
    private static ListView recentTaskListView;
    List<Task> taskList =new ArrayList<>();

    //user info
    private String userId;
    private SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_process);

        mPrefs = activity.getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //taskManager listview
        recentTaskListView = (ListView) findViewById(R.id.listView_recent_task);
        taskList = DeviceStatus.getRecentTasks(userId);
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();

        recentTaskAdapter = new RecentTaskAdapter(activity,taskList);
        recentTaskAdapter.notifyDataSetChanged();
        recentTaskListView.setAdapter(recentTaskAdapter);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(menuItem);
    }
}
