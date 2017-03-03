package de.mpg.mpdl.labcam.code.activity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.activeandroid.query.Select;

import de.mpg.mpdl.labcam.Model.LocalModel.Settings;
import de.mpg.mpdl.labcam.Model.LocalModel.Task;
import de.mpg.mpdl.labcam.R;
import de.mpg.mpdl.labcam.code.common.adapter.RecentTaskAdapter;
import de.mpg.mpdl.labcam.Utils.DBConnector;
import de.mpg.mpdl.labcam.code.base.BaseCompatActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

public class RecentProcessActivity extends BaseCompatActivity {

    Activity activity = this;
    RecentTaskAdapter recentTaskAdapter = null;

    //ui elements
    @BindView(R.id.listView_recent_task)
    ListView recentTaskListView;
    @BindView(R.id.tv_no_recent_task)
    TextView noRecentTaskView;
    List<Task> taskList =new ArrayList<>();

    //user info
    private String userId;
    private SharedPreferences mPrefs;
    private String serverName;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_recent_process;
    }

    @Override
    protected void initContentView(Bundle savedInstanceState) {
        mPrefs = activity.getSharedPreferences("myPref", 0);
        userId =  mPrefs.getString("userId", "");
        serverName = mPrefs.getString("serverName","");

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar_recent_task);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //taskManager listView
        taskList = DBConnector.getRecentTasks(userId, serverName);
        Settings settings = new Select().from(Settings.class).where("userId = ?", userId).executeSingle();

        recentTaskAdapter = new RecentTaskAdapter(activity, taskList);
        recentTaskAdapter.notifyDataSetChanged();
        recentTaskListView.setAdapter(recentTaskAdapter);

        //Display either "no recent upload" or the listview of uploaded tasks
        if(taskList.size() == 0) {
            noRecentTaskView.setVisibility(View.VISIBLE);
            recentTaskListView.setVisibility(View.GONE);
        }else {
            noRecentTaskView.setVisibility(View.GONE);
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
